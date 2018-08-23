package com.anor.roar.whenzint.parser;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.anor.roar.whenzint.Action;
import com.anor.roar.whenzint.Condition;
import com.anor.roar.whenzint.Program;
import com.anor.roar.whenzint.VariablePath;
import com.anor.roar.whenzint.Whenz;
import com.anor.roar.whenzint.actions.ChainAction;
import com.anor.roar.whenzint.conditions.BoolCondition;
import com.anor.roar.whenzint.conditions.EventCondition;
import com.anor.roar.whenzint.mapping.ByteBufferMapping;

public class ProgramBuilder implements NodeVisitor {

  private Node                              root;
  private Program                           program;

  private Map<String, VariablePath>         paths   = new HashMap<>();
  private Map<String, ByteBufferMapping>    mappings = new HashMap<>();
  private static Map<String, ActionBuilder> actions = new HashMap<>();

  public ProgramBuilder(Node root) {
    this.root = root;
    this.program = new Program();
  }

  public ProgramBuilder(Node root2, Program prog) {
    this.root = root2;
    this.program = prog;
  }

  public Program build() {
    convertTree();
    return program;
  }

  private void convertTree() {
    root.traverse(this);
  }

  public static void registerActionBuilder(ActionBuilder builder) {
    actions.put(builder.getActionNodeName(), builder);
  }

  @Override
  public void visit(Node node) {
    if ("whenz".equals(node.name())) {
      Condition cond = null;
      Action lastAction = null;
      String defining = null;
      for(Node child : node.children()) {
        if ("conditions".equals(child.name())) {
          if (child.children()[0].is("define")) {
            Node defChild = child.children()[0].children()[0];
            defining = defChild.getTokenOrValue();
          } else if (child.children()[0].is("event")) {
            cond = new EventCondition(child.children()[0].children()[0].getTokenOrValue());
          } else if ("Reference".equals(child.children()[0].name())) {
            Node referenceNode = child.children()[0];
            String ref = referenceString(referenceNode.children());
            String op = child.children()[1].children()[0].getTokenOrValue();
            Node rightVal = child.children()[2];
            boolean repeats = !child.hasChildNamed("once");
            if (rightVal.isNamed("Number")) {
              Node rightValChild = rightVal.children()[0];
              if (rightValChild.getRawToken().isNumber()) {
                int num = Integer.parseInt(rightValChild.name());
                cond = new BoolCondition(op, ref, num, repeats);
              }
            } else if (rightVal.isNamed("Literals")) {
              Node rightValChild = rightVal.children()[0];
              cond = new BoolCondition(op, ref, rightValChild.getTokenOrValue(), repeats);
            }
            program.setListener(ref, cond);
          } else {
            System.out.println("Unhandled condition: " + child);
          }
        } else if ("action".equals(child.name())) {
          Node actionNode = child.children()[0];
          if ("defined action".equals(actionNode.name())) {
            // TODO: defer processing these to each action class
            Node definedActionNode = actionNode.children()[0];
            ActionBuilder builder = actions.get(definedActionNode.name());
            if (builder == null) {
              System.out.println("NO Action Builder: " + definedActionNode);
            }
            Action a = builder.buildAction(this, definedActionNode);

            if (a != null) {
              if (lastAction == null) {
                lastAction = a;
              } else {
                lastAction = new ChainAction(lastAction, a);
              }
            }else {
              System.out.format("WARNING: Action builder '%s' failed to build action\n");
            }
          } else if ("GlobalReference".equals(actionNode.name())) {
            ActionBuilder builder = actions.get(actionNode.name());
            Action a = builder.buildAction(this, actionNode);

            if (a != null) {
              if (lastAction == null) {
                lastAction = a;
              } else {
                lastAction = new ChainAction(lastAction, a);
              }
            }
          } else if ("Class & Method".equals(actionNode.name())) {
            String className = actionNode.children()[0].children()[0].name();
            String methodName = actionNode.children()[1].children()[0].name();
            List<String> params = new LinkedList<String>();
            int first = 0;
            for(Node param : actionNode.children()[1].children()) {
              if (first == 0) {
                ++first;
                continue;
              }

              params.add(param.name());
            }
            String parms[] = params.toArray(new String[params.size()]);
            Object objectToSet = instanceObject(className, methodName, parms);
            program.setObject(defining, objectToSet);
          }

          defining = null; // clear what is being defined
        }
      }

      if (cond != null && lastAction != null) {
        cond.setAction(lastAction);
        program.add(cond);
      }
    }

  }

  public VariablePath getPath(Node node) {
    String ref = referenceString(node.children());
    if (ref != null) {
      VariablePath path = paths.get(ref);
      if (("Reference".equals(node.name()) || "GlobalPath".equals(node.name())) && path == null) {
        paths.put(ref, path = VariablePath.create(ref));
      }
      return path;
    }
    return null;
  }

  public String referenceString(Node[] children) {
    String quickRef = "";
    Node parts[] = children;
    for(Node n : children) {
      if (n.hasToken()) {
        quickRef += n.getToken();
      } else {
        quickRef += n.getValue();
      }
      if (n != parts[parts.length - 1]) {
        quickRef += ".";
      }
    }
    return quickRef;
  }

  public Object instanceObject(String className, String methodName, String params[]) {
    try {
      Class<?> loadClass = Whenz.class.getClassLoader().loadClass(className);
      List<Class<?>> paramTypes = new LinkedList<Class<?>>();
      List<Object> args = new LinkedList<Object>();
      for(String param : params) {
        try {
          args.add(Integer.parseInt(param));
          paramTypes.add(int.class);
        } catch (NumberFormatException nfe) {
          try {
            args.add(Double.parseDouble(param));
            paramTypes.add(double.class);
          } catch (NumberFormatException nfe1) {
            if ("true".equals(param) || "false".equals(param)) {
              paramTypes.add(Boolean.class);
              args.add(Boolean.parseBoolean(param));
            } else {
              paramTypes.add(String.class);
              args.add(param);
            }
          }
        }
      }
      Class<?> types[] = paramTypes.toArray(new Class<?>[paramTypes.size()]);
      if (methodName.equals(loadClass.getSimpleName())) {
        // ctor
        Constructor<?> constructor = loadClass.getConstructor(types);
        return constructor.newInstance(args.toArray());
      }
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (SecurityException e) {
      e.printStackTrace();
    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }

    return null;
  }

  public Object getObject(String token) {
    return program.getObject(token);
  }

  public void setObject(String name, Object obj) {
    program.setObject(name, obj);
  }

  public void registerMapping(ByteBufferMapping bmm, VariablePath path) {
    this.mappings.put(path.getFullyQualifiedName(), bmm);
  }

  public ByteBufferMapping getMapping(String path) {
    return this.mappings.get(path);
  }
}
