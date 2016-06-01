package blue.made.turremclient2d.ui;

import blue.made.turremclient2d.Game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Created by Sam Sartor on 5/31/2016.
 */
public class UI {
	public static void show(Game game) {
		JFrame frame = new JFrame("Test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				game.net.disconnect();
				super.windowClosing(e);
			}
		});

		Container pane = frame.getContentPane();
		pane.setPreferredSize(new Dimension(500, 500));
		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));

		JPanel extra = new JPanel();
		extra.setLayout(new FlowLayout());

		game.onWorldLoad((g) -> {
			final WorldInterface wi = new WorldInterface(g, extra, frame);
			frame.addComponentListener(new ComponentAdapter() {
				@Override
				public void componentResized(ComponentEvent e) {
					Dimension size = new Dimension(frame.getContentPane().getWidth(), (int) (frame.getContentPane().getHeight() * .5));
					wi.panel.setPreferredSize(size);
				}
			});
			pane.add(wi.panel);
			pane.add(extra);

			frame.pack();
			frame.setVisible(true);

			SwingUtilities.invokeLater(() -> wi.panel.requestFocus());
		});
	}
}
