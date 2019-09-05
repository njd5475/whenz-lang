package com.anor.roar.whenzint.actions;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.anor.roar.whenzint.Action;
import com.anor.roar.whenzint.Program;
import com.anor.roar.whenzint.Whenz;
import com.anor.roar.whenzint.parser.Node;
import com.anor.roar.whenzint.parser.ProgramBuilder;
import com.anor.roar.whenzint.parser.TokenBuffer;
import com.anor.roar.whenzint.parser.WhenzParser;
import com.anor.roar.whenzint.parser.WhenzSyntaxError;

public class LaunchModuleAction extends Action {

	static {
		ProgramBuilder.registerActionBuilder(new LaunchModuleAction());
	}

	private String moduleName;

	public LaunchModuleAction() {

	}

	public LaunchModuleAction(String modName) {
		this.moduleName = modName;
	}

	@Override
	public Node buildNode(WhenzParser parser, TokenBuffer tokens) throws WhenzSyntaxError, IOException {
		Node launchAction = new Node("LaunchModule");
		if (tokens.peek().is("launch")) {
			tokens.take();
			parser.consumeWhitespace(tokens);
			if (tokens.peek().isWord()) {
				launchAction.addChild("ModName", tokens.take());
			} else {
				parser.unexpectedToken(tokens.take());
			}

			parser.consumeWhitespace(tokens);

			if (tokens.peek().isNewline()) {
				tokens.take();
				return launchAction;
			} else {
				parser.unexpectedToken(tokens.take());
			}
		} else {
			parser.unexpectedToken(tokens.take());
		}
		return null;
	}

	@Override
	public Action buildAction(ProgramBuilder builder, Node node) {
		Node modNameNode = node.getChildNamed("ModName");
		return new LaunchModuleAction(modNameNode.getToken());
	}

	@Override
	public String getActionNodeName() {
		return "LaunchModule";
	}

	@Override
	public void perform(Program program, Map<String, Object> context) {
		File module = new File("./" + this.moduleName);
		if(module.isDirectory()) {
			File files[] = module.listFiles();
			List<File> toLoad = new LinkedList<>();
			List<File> actionFiles = new LinkedList<>();
			File file = null;
			for(int i = 0; i < files.length; ++i) {
				file = files[i];
				if(file.getName().endsWith(".whenz")) {
					toLoad.add(file);
				} else if(file.getName().endsWith(".action")) {
					actionFiles.add(file);
				}
			}
			
			//WhenzParser parser = new WhenzParser(actionFiles.toArray(new String[actionFiles.size()]));
			Program loaded = Whenz.loadFromFiles(toLoad.toArray(new File[toLoad.size()]));
			
			loaded.trigger("app_starts");
			
			new Thread() {
				public void run() {
					try {
						loaded.run();
					}catch(Exception e) {
						System.err.format("Module thread '%s' errored with:\n", moduleName);
						e.printStackTrace();
					}
				}
				
			}.start();
		}
	}

}
