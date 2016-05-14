package com.anor.roar.whenzint.actions;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import com.anor.roar.whenzint.Action;
import com.anor.roar.whenzint.Event;
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
		frame.setLayout(new BorderLayout());
		frame.setPreferredSize(new Dimension(320,240));
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		program.setObject(frame);
		program.trigger("window_launched");
	}

	
}
