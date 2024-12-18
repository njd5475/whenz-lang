package com.anor.roar.whenzint.actions;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Map;

import com.anor.roar.whenzint.Action;
import com.anor.roar.whenzint.Program;
import com.anor.roar.whenzint.VariablePath;
import com.anor.roar.whenzint.parser.*;

public class RunShellCommand extends AbstractAction {

	private String commandString;
	private VariablePath ref;

	static {
		ProgramBuilder.registerActionBuilder(new RunShellCommand(CodeLocation.fake, null));
	}

	public RunShellCommand(CodeLocation location, String varName) {
		super(location);
		this.commandString = varName;
	}

	public RunShellCommand(CodeLocation location, VariablePath ref, String varName) {
		super(location);
		this.commandString = varName;
		this.ref = ref;
	}

	@Override
	public Node buildNode(WhenzParser parser, TokenBuffer tokens) throws WhenzSyntaxError, IOException {
		parser.consumeWhitespace(tokens);
		if (tokens.peek().is("execute")) {
			Node exec = new Node(getActionNodeName());
			tokens.take();
			return executableCommand(exec, parser, tokens);
		} else if (tokens.peek().is("monitor")) {
			Node monitor = new Node(getActionNodeName());
			tokens.take();
			parser.consumeWhitespace(tokens);
			if (tokens.peek().is("as")) {
				tokens.take();
				parser.consumeWhitespace(tokens);
				parser.globalReference(monitor, tokens);
				parser.consumeWhitespace(tokens);
				if (tokens.peek().is("exec")) {
					tokens.take();
					return this.executableCommand(monitor, parser, tokens);
				}
			}
		}
		parser.unexpectedToken(tokens);
		return null;
	}

	private Node executableCommand(Node node, WhenzParser parser, TokenBuffer tokens) throws IOException {
		parser.consumeWhitespace(tokens);
		if (!tokens.peek().isNewline()) { // can be anything after exec
			while (!tokens.peek().isNewline()) {
				Node arg = new Node("Arg");
				parser.consumeWhitespace(tokens);
				while (!tokens.peek().isWhitespace() && !tokens.peek().isNewline()) {
					Node argParts = new Node("ArgPart", tokens.take());
					arg.add(argParts);
				}
				node.add(arg);
			}
			tokens.take();

			return node;
		}
		return null;
	}

	@Override
	public void perform(Program program, Map<String, Object> context) {
		try {
			if (ref == null) {
				Runtime.getRuntime().exec(new String[] {"sh", "-c", commandString});
			} else {
				Process proc = Runtime.getRuntime().exec(new String[] {"sh", "-c", commandString});
				(new Monitor(proc, program, ref)).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Action buildAction(ProgramBuilder builder, Node node) throws WhenzSyntaxTreeError {
		StringBuilder sb = new StringBuilder("");
		for (Node ch : node.children()) {
			if ("Arg".equals(ch.name())) {
				for (Node arg : ch.children()) {
					sb.append(arg.getToken());
				}
				sb.append(' ');
			}
		}
		Node refNode = node.getChildNamed("Reference");
		VariablePath ref = builder.getPath(refNode);
		if (ref != null) {
			return new RunShellCommand(CodeLocation.toLocation(node), ref, sb.toString().trim());
		}
		return new RunShellCommand(CodeLocation.toLocation(node), sb.toString().trim());
	}

	@Override
	public String getActionNodeName() {
		return "RunShellCommand";
	}

	public class Monitor extends Thread {

		private Process proc;
		private Program program;
		private VariablePath ref;

		public Monitor(Process proc, Program program, VariablePath ref) {
			this.proc = proc;
			this.program = program;
			this.ref = ref;
		}

		public void run() {
			byte[] errBuf = new byte[1024];
			byte[] outBuf = new byte[1024];
			StringBuilder output = new StringBuilder("");
			StringBuilder errorOutput = new StringBuilder("");
			program.changeState(ref.getFullyQualifiedName(), "running");
			program.setObject(ref.getFullyQualifiedName() + ".buffers.err", ByteBuffer.wrap(errBuf));
			program.setObject(ref.getFullyQualifiedName() + ".buffers.err.lastread", -1);
			program.setObject(ref.getFullyQualifiedName() + ".buffers.out", ByteBuffer.wrap(outBuf));
			program.setObject(ref.getFullyQualifiedName() + ".buffers.out.lastread", -1);
			program.setObject(ref.getFullyQualifiedName() + ".output", output);
			program.setObject(ref.getFullyQualifiedName() + ".errorOutput", errorOutput);
			InputStream isErr = proc.getErrorStream();
			InputStream isOut = proc.getInputStream();
			boolean errStreamOpen = true;
			boolean outStreamOpen = true;
			boolean errStreamUnavailable = false;
			boolean outStreamUnavailable = false;
			while(proc.isAlive() || errStreamOpen || outStreamOpen) {
				try {
					if(isErr.available() > 0) {
						errStreamOpen = this.fillBuffer(errBuf, errorOutput, isErr, ref.getFullyQualifiedName() + ".buffers.err", program);
					}else {
					  errStreamUnavailable = true;
					  errStreamOpen = false;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				try {
					if(isOut.available() > 0) {
						outStreamOpen = this.fillBuffer(outBuf, output, isOut, ref.getFullyQualifiedName() + ".buffers.out", program);
					}else {
					  outStreamUnavailable = true;
					  outStreamOpen = false;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			// drain streams
			program.setObject(ref.getFullyQualifiedName() + ".exitValue", proc.exitValue());
			program.changeState(ref.getFullyQualifiedName(), "done");
		}
		
		/**
		 * @param buf
		 * @param output
		 * @param stream
		 * @param var
		 * @param p
		 * @return True if the stream is still open, false if we reached the end of the stream
		 * @throws IOException
		 */
		public boolean fillBuffer(byte[] buf, StringBuilder output, InputStream stream, String var, Program p) throws IOException {
		  int read = stream.read(buf, 0, buf.length);
      if(read > 0) {
        output.append(new String(buf, 0, read));
        p.setObject(var + ".lastread", read);
        p.changeState(var, "bufferRead");
      }
      if(read < 0) {
        return false;
      }
      return true;
		}
	}
}
