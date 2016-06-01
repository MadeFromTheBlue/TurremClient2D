package blue.made.turremclient2d.ui;

import blue.made.turremclient2d.Game;
import blue.made.turremclient2d.world.Chunk;
import blue.made.turremclient2d.world.Tags;
import blue.made.turremclient2d.world.World;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.function.Consumer;

/**
 * Created by Sam Sartor on 5/31/2016.
 */
public class WorldInterface {
	public class DrawPanel extends JPanel {
		@Override
		public Dimension getMinimumSize() {
			return getPreferredSize();
		}

		/*
		@Override
		public Dimension getPreferredSize() {
			return new Dimension(width, height);
		}
		*/

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D d = (Graphics2D) g;

			Point corner0 = toPanelSpace(0, 0);
			Point corner1 = toPanelSpace(world.xwidth, world.ywidth);

			int cx0 = (int) (minx / world.chunkSize);
			if (cx0 < 0) cx0 = 0;
			int cy0 = (int) (miny / world.chunkSize);
			if (cy0 < 0) cy0 = 0;
			int cx1 = (int) ((maxx - 1) / world.chunkSize) + 1;
			if (cx1 > world.chunksx) cx1 = world.chunksx;
			int cy1 = (int) ((maxy - 1) / world.chunkSize) + 1;
			if (cy1 > world.chunksx) cy1 = world.chunksx;

			for (int i = cx0; i < cx1; i++) {
				for (int j = cy0; j < cy1; j++) {
					if (world.isInWorld(i, j)) {
						int x = i * world.chunkSize;
						int y = j * world.chunkSize;

						int px0 = x;
						int py0 = y;
						int px1 = x + world.chunkSize;
						int py1 = y + world.chunkSize;
						Point p0 = toPanelSpace(px0, py0);
						Point p1 = toPanelSpace(px1, py1);

						Chunk c = world.chunks[i][j];
						if (c != null) {
							float wx = (p1.x - p0.x) / (float) world.chunkSize;
							float wy = (p1.y - p0.y) / (float) world.chunkSize;
							int ind = 0;
							for (int py = py0; py < py1; py++) {
								for (int px = px0; px < px1; px++) {
									short[] tags = c.tags[ind++];
									float R = 0;
									float G = 0;
									float B = 0;
									if (tags.length != 0) {
										for (short s : tags) {
											Tags.Tag t = world.tags.tags[s & 0xFFFF];
											R += t.color.getRed();
											G += t.color.getGreen();
											B += t.color.getBlue();
										}
										R /= tags.length * 255;
										G /= tags.length * 255;
										B /= tags.length * 255;
									} else {
										R = 1;
										G = 1;
										B = 1;
									}
									float v = 0;
									v -= world.getHeight(px - 1, py);
									v -= world.getHeight(px, py - 1);
									v += world.getHeight(px + 1, py);
									v += world.getHeight(px, py + 1);
									v *= 0.4f;
									v += 0.5f;
									if (v < 0) v = 0;
									if (v > 1) v = 1;
									d.setColor(new Color(R * v, G * v, B * v));
									Point p = toPanelSpace(px, py);
									d.fillRect(p.x, p.y, (int) wx + 1, (int) wy + 1);
								}
							}
						} else {

							if ((i + j) % 2 != 0) d.setColor(new Color(.8f, .8f, .8f));
							else d.setColor(new Color(.6f, .6f, .6f));

							d.fillRect(p0.x, p0.y, p1.x - p0.x, p1.y - p0.y);
						}
					}
				}
			}

			if (selx != -1) {
				Point sel0 = toPanelSpace(selx, sely);
				Point sel1 = toPanelSpace(selx + 1, sely + 1);
				d.setColor(new Color(.8f, .2f, .2f));
				d.fillRect(sel0.x, sel0.y, sel1.x - sel0.x, sel1.y - sel0.y);
			}
		}
	}

	public int selx = -1;
	public int sely = -1;
	public int mousex = -1;
	public int mousey = -1;
	public boolean mouseInWorld = false;

	public double minx = 0;
	public double miny = 0;
	public double maxx = 16;
	public double maxy = 16;

	public int width = 300;
	public int height = 200;

	public World world;
	public DrawPanel panel;
	public JPanel extra;
	public JFrame frame;

	private Point mouseLocation;

	public MouseAdapter mouse = new MouseAdapter() {
		@Override
		public void mouseClicked(MouseEvent e) {
			if (mouseInWorld) {
				int but = e.getButton();
				if (but == 1) {
					selx = mousex;
					sely = mousey;
					updateSelected();
				}
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {

		}

		@Override
		public void mouseReleased(MouseEvent e) {
			//extra.removeAll();
			//extra.updateUI();
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
			mouseInWorld = false;
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			int r = e.getWheelRotation();
			double dx = r * (maxx - minx) / 10;
			double dy = r * (maxy - miny) / 10;
			minx -= dx;
			miny -= dy;
			maxx += dx;
			maxy += dy;
			relocateMouse();
			redraw();
		}

		@Override
		public void mouseDragged(MouseEvent e) {

		}

		@Override
		public void mouseMoved(MouseEvent e) {
			mouseLocation = e.getPoint();
			relocateMouse();
			redraw();
		}
	};

	public WorldInterface(Game game, JPanel extra, JFrame frame) {
		world = game.world;
		this.extra = extra;
		this.frame = frame;
		panel = new DrawPanel();

		panel.addMouseListener(mouse);
		panel.addMouseMotionListener(mouse);
		panel.addMouseWheelListener(mouse);

		panel.setBorder(BorderFactory.createLineBorder(new Color(.1f, .1f, .1f)));

		addKeyEvent("moveup", KeyStroke.getKeyStroke("UP"), (ActionEvent) -> {
			moveView(0, -1);
		});

		addKeyEvent("movedown", KeyStroke.getKeyStroke("DOWN"), (ActionEvent) -> {
			moveView(0, 1);
		});

		addKeyEvent("moveright", KeyStroke.getKeyStroke("RIGHT"), (ActionEvent) -> {
			moveView(1, 0);
		});

		addKeyEvent("moveleft", KeyStroke.getKeyStroke("LEFT"), (ActionEvent) -> {
			moveView(-1, 0);
		});

		extra.add(selpos);

		panel.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				updateSize(e.getComponent().getWidth(), e.getComponent().getHeight());
			}
		});
	}

	void addKeyEvent(String name, KeyStroke key, Consumer<ActionEvent> action) {
		panel.getInputMap().put(key, name);
		panel.getActionMap().put(name, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				action.accept(e);
			}
		});
	}

	void moveView(int dx, int dy) {
		double dvx = dx * (maxx - minx) / 20;
		double dvy = dy * (maxy - miny) / 20;
		minx += dvx;
		maxx += dvx;
		miny += dvy;
		maxy += dvy;
		relocateMouse();
		redraw();
	}

	public void redraw() {
		panel.repaint(new Rectangle(0, 0, width, height));
	}

	public void relocateMouse() {
		if (mouseLocation != null) {
			mousex = (int) ((mouseLocation.getX() / width) * (maxx - minx) + minx);
			mousey = (int) ((mouseLocation.getY() / height) * (maxy - miny) + miny);
			mouseInWorld = world.isInWorld(mousex, mousey);
		} else {
			mousex = -1;
			mousey = -1;
			mouseInWorld = false;
		}
		updatePointed();
	}

	public void updatePointed() {

	}

	JLabel selpos = new JLabel();
	public void updateSelected() {
		redraw();
		selpos.setText(String.format("Selection: (%d, %d)", selx, sely));
	}

	public Point toPanelSpace(double x, double y) {
		return new Point((int) (width * (x - minx) / (maxx - minx)), (int) (height * (y - miny) / (maxy - miny)));
	}

	public void updateSize(int width, int height) {
		this.width = width;
		this.height = height;
		double per = (maxx - minx) / width;
		maxy = miny;
		maxy += per * height;
		relocateMouse();
		redraw();
	}
}
