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
import com.anor.roar.whenzint.actions.CallSetterMethod;
import com.anor.roar.whenzint.actions.ChainAction;
import com.anor.roar.whenzint.actions.ExitAction;
import com.anor.roar.whenzint.actions.IncrementAction;
import com.anor.roar.whenzint.actions.LaunchWindowAction;
import com.anor.roar.whenzint.actions.PrintAction;
import com.anor.roar.whenzint.actions.PrintVarAction;
import com.anor.roar.whenzint.actions.RunShellCommand;
import com.anor.roar.whenzint.actions.SetToLiteral;
import com.anor.roar.whenzint.actions.TriggerEventAction;
import com.anor.roar.whenzint.conditions.BoolCondition;
import com.anor.roar.whenzint.conditions.EventCondition;

public class ProgramBuilder implements NodeVisitor {

  private Node                      root;
  private Program                   program;

  private Map<String, VariablePath> paths = new HashMap<>();

  public ProgramBuilder(Node root) {
    this.root = root;
    this.program = new Program();
  }

  public Program build() {
    convertTree();
    return program;
  }

  private void convertTree() {
    root.traverse(this);
  }

  @Override
  public void visit(Node node) {
    if ("whenz".equals(node.name())) {
      Condition cond = null;
      Action lastAction = null;
      String defining = null;
      for (Node child : node.children()) {
        if ("conditions".equals(child.name())) {
          if (child.children()[0].is("define")) {
            Node defChild = child.children()[0].children()[0];
            defining = defChild.getTokenOrValue();
          } else if (child.children()[0].is("event")) {
            cond = new EventCondition(
                child.children()[0].children()[0].getTokenOrValue());
          } else if ("Reference".equals(child.children()[0].name())) {
            Node referenceNode = child.children()[0];
            String ref = referenceString(referenceNode.children());
            String op = child.children()[1].children()[0].getToken();
            Node rightVal = child.children()[2];
            if (rightVal.isNamed("Number")) {
              Node rightValChild = rightVal.children()[0];
              if (rightValChild.getRawToken().isNumber()) {
                int num = Integer.parseInt(rightValChild.name());
                cond = new BoolCondition(op, ref, num);
              }
            } else if (rightVal.isNamed("Literals")) {
              Node rightValChild = rightVal.children()[0];
              cond = new BoolCondition(op, ref, rightValChild.name());
            }
            program.setListener(ref, cond);
          } else {
            System.out.println("Unhandled condition: " + child);
          }
        } else if ("action".equals(child.name())) {
          Node actionNode = child.children()[0];
          if ("defined action".equals(actionNode.name())) {
            // TODO: differ processing these to each action class
            Node definedActionNode = actionNode.children()[0];
            Action a = null;
            if ("Set".equals(definedActionNode.name())) {
              Node setNode = definedActionNode.children()[0];
              String set = setNode.getToken();
              String name = setNode.getToken();
              Object obj = "";
              if ("Reference".equals(setNode.name())) {
                Node val = setNode.children()[0];
                if (val.hasToken()) {
                  obj = program.getObject(val.getToken());
                } else if (val.hasValue()) {
                  obj = program.getObject(val.getValue());
                }
                if (obj == null) {
                  System.err.println("Link Error: Reference "
                      + val.getTokenOrValue() + " referes to a null object");
                }
                name = "window";
              } else if ("VariableIdentifier".equals(setNode.name())) {
                StringBuilder str = new StringBuilder("");
                for (Node v : setNode.children()) {
                  if (v.hasToken()) {
                    str.append(v.getToken());
                  } else if (v.hasValue()) {
                    str.append(v.getValue());
                  }
                }
                obj = str.toString();
                if (obj == null) {
                  System.err.println("Issues linking program");
                }
                name = "window";
                program.setObject(name, obj);
              }

              a = new CallSetterMethod(set, name, obj);
            } else if ("PrintAction".equals(definedActionNode.name())) {
              StringBuilder printStr = new StringBuilder("");
              for (Node part : definedActionNode.children()) {
                printStr.append(part.getToken());
              }
              a = new PrintAction(printStr.toString());
            } else if ("PrintVar".equals(definedActionNode.name())) {
              Node global = definedActionNode.children()[0];
              StringBuilder printStr = new StringBuilder("");
              Node children[] = global.children();
              for (Node part : children) {
                if (part != children[0]) {
                  printStr.append(".");
                }
                printStr.append(part.getToken());
              }
              a = new PrintVarAction(printStr.toString());
            } else if ("Exit".equals(definedActionNode.name())) {
              a = new ExitAction();
            } else if ("Trigger".equals(definedActionNode.name())) {
              a = new TriggerEventAction(
                  definedActionNode.children()[0].getValue());
            } else if ("LaunchWindow".equals(definedActionNode.name())) {
              a = new LaunchWindowAction();
            } else if ("RunShellCommand".equals(definedActionNode.name())) {
              StringBuilder sb = new StringBuilder("");
              for (Node ch : definedActionNode.children()) {
                if ("Arg".equals(ch.name())) {
                  for (Node arg : ch.children()) {
                    sb.append(arg.getToken());
                  }
                  sb.append(' ');
                }
              }
              a = new RunShellCommand(sb.toString().trim());
            } else if ("Increment".equals(definedActionNode.name())) {
              Node children[] = definedActionNode.children();
              VariablePath path = getPath(children[0]);
              a = new IncrementAction(path);
            }

            if (a != null) {
              if (lastAction == null) {
                lastAction = a;
              } else {
                lastAction = new ChainAction(lastAction, a);
              }
            }
          } else if ("GlobalReference".equals(actionNode.name())) {
            Node lval = actionNode.children()[0];
            Node rval = actionNode.children()[2];
            String quickRef = referenceString(lval.children());
            Action a = null;
            if ("Literals".equals(rval.name())) {
              a = new SetToLiteral(quickRef, rval.children()[0].name());
            } else if ("Number".equals(rval.name())) {
              a = new SetToLiteral(quickRef,
                  Integer.parseInt(rval.children()[0].getToken()));
            }

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
            for (Node param : actionNode.children()[1].children()) {
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

  private VariablePath getPath(Node node) {
    String ref = referenceString(node.children());
    if (ref != null) {
      VariablePath path = paths.get(ref);
      if ("Reference".equals(node.name()) && path == null) {
        paths.put(ref, path = VariablePath.create(ref));
      }
      return path;
    }
    return null;
  }

  private String referenceString(Node[] children) {
    String quickRef = "";
    Node parts[] = children;
    for (Node n : children) {
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

  public Object instanceObject(String className, String methodName,
      String params[]) {
    try {
      Class<?> loadClass = Whenz.class.getClassLoader().loadClass(className);
      List<Class<?>> paramTypes = new LinkedList<Class<?>>();
      List<Object> args = new LinkedList<Object>();
      for (String param : params) {
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

}
