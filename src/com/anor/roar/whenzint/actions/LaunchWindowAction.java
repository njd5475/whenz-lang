package com.anor.roar.whenzint.actions;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.anor.roar.whenzint.Action;
import com.anor.roar.whenzint.Program;

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

}
