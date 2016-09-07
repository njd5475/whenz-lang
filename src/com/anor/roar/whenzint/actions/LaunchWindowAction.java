package com.anor.roar.whenzint.actions;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.anor.roar.whenzint.Action;
import com.anor.roar.whenzint.Program;
import com.anor.roar.whenzint.patterns.Node;
import com.anor.roar.whenzint.patterns.TokenBuffer;
import com.anor.roar.whenzint.patterns.WhenzParser;
import com.anor.roar.whenzint.patterns.WhenzSyntaxError;

public class LaunchWindowAction extends Action {

  @Override
  public void perform(final Program program) {
    JFrame frame = new JFrame();
    frame.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        program.trigger("window_closing");
      }
    });
    
    frame.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        System.out.println("Mouse Clicked");
      }
    });
    frame.addMouseMotionListener(new MouseMotionListener() {

      @Override
      public void mouseDragged(MouseEvent e) {
        // TODO Auto-generated method stub
        
      }

      @Override
      public void mouseMoved(MouseEvent e) {
        program.setObject("mouse", e.getPoint());
        program.trigger("mouse_moved");
      }
    });
    
    frame.setLayout(new BorderLayout());
    frame.setBackground(new Color(0, 0, 0));
    frame.setPreferredSize(new Dimension(320, 240));
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
    program.setObject("window", frame);
    program.trigger("window_launched");
  }

  @Override
  public Node buildNode(WhenzParser parser, TokenBuffer tokens)
      throws WhenzSyntaxError, IOException {
    Node launchNode = new Node("LaunchWindow");
    parser.consumeWhitespace(tokens);
    if(tokens.peek().is("launch")) {
      tokens.take();
      parser.consumeWhitespace(tokens);
      if(tokens.peek().is("window")) {
        tokens.take();
        if(tokens.peek().isNewline()) {
          tokens.take();
        }
      }else{
        parser.unexpectedToken(tokens.peek());
      }
    }else{
      parser.unexpectedToken(tokens.peek());
    }
    return launchNode;
  }

}
