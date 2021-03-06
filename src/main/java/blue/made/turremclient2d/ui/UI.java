package blue.made.turremclient2d.ui;

import blue.made.bcf.BCFWriter;
import blue.made.turremclient2d.Game;
import blue.made.turremclient2d.actors.structure.Structure;
import blue.made.turremclient2d.actors.structure.StructureRegistry;
import blue.made.turremclient2d.actors.structure.StructureSpec;
import blue.made.turremclient2d.world.Chunk;
import blue.made.turremclient2d.world.Tags;
import blue.made.turremclient2d.world.World;
import org.apache.commons.math3.ml.neuralnet.Network;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * Created by Sam Sartor on 5/31/2016.
 */
public class UI {
	public double camx = 50;
	public double camy = 50;
	public double cams = 100;
	int lastMouseX = -1;
	int lastMouseY = -1;
	SwingGame gu;
	JPanel ui;
	JLabel mousepos = new JLabel("Test");
	JButton addStruct = new JButton();
	StructureSpec structToPlace = null;

	public void show(Game game) {
		JFrame frame = new JFrame("UI");
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		Container pane = frame.getContentPane();
		pane.setPreferredSize(new Dimension(500, 500));

		ui = new JPanel();
		ui.setLayout(new FlowLayout());

		ui.add(mousepos);
		ui.add(addStruct);
		addStruct.setText("Place Building");
		addStruct.setAction(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ArrayList<String> list = new ArrayList<>();
				list.add("[NONE]");
				list.addAll(StructureRegistry.specs.keySet());
				String in = (String) JOptionPane.showInputDialog(frame, "What type?", "Select", JOptionPane.QUESTION_MESSAGE, null, list.toArray(), null);
				structToPlace = StructureRegistry.specs.get(in);
			}
		});

		gu = new SwingGame();

		MouseAdapter mouse = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (gu.isMouseIn()) {
					int but = e.getButton();
					if (but == 1) {
						if (structToPlace!= null) {
							Game.INSTANCE.net.send(data -> {
								BCFWriter.Map map = new BCFWriter(data).startMap();
								map.writeName("spec");
								map.write(structToPlace.id);
								map.writeName("x");
								map.write((int) gu.getMouseX());
								map.writeName("y");
								map.write((int) gu.getMouseY());
								map.end();
								return 0x40;
							});
						}
					}
				}
			}

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				int r = e.getWheelRotation();
				double s = Math.pow(1.1, r);
				cams *= s;
				updateCam();
			}
		};

		game.onWorldLoad((g) -> {
			World world = g.world;
			world.onChange = w -> {
				gu.notifyRedraw();
			};

			gu.addPainter(draw -> {
				draw.clearRect((int) (gu.minx * gu.drawScale), (int) (gu.miny * gu.drawScale), (int) ((gu.maxx - gu.minx) * gu.drawScale), (int) ((gu.maxy - gu.miny) * gu.drawScale));
			});
			gu.addPainter(draw -> {
				int ds = (int) gu.drawScale + 1;

				int cx0 = (int) (gu.minx / world.chunkSize);
				if (cx0 < 0) cx0 = 0;
				int cy0 = (int) (gu.miny / world.chunkSize);
				if (cy0 < 0) cy0 = 0;
				int cx1 = (int) ((gu.maxx - 1) / world.chunkSize) + 1;
				if (cx1 > world.chunksx) cx1 = world.chunksx;
				int cy1 = (int) ((gu.maxy - 1) / world.chunkSize) + 1;
				if (cy1 > world.chunksx) cy1 = world.chunksx;

				for (int i = cx0; i < cx1; i++) {
					for (int j = cy0; j < cy1; j++) {
						Chunk chunk = world.chunks[i][j];
						if (chunk != null) {
							int ind = 0;
							for (int y = 0; y < world.chunkSize; y++) {
								for (int x = 0; x < world.chunkSize; x++) {
									int px = i * world.chunkSize + x;
									int py = j * world.chunkSize + y;
									int u = (int) (px * gu.drawScale);
									int v = (int) (py * gu.drawScale);
									short[] tags = chunk.tags[ind++];
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
									float grey = 0;
									grey -= world.getHeight(px - 1, py);
									grey -= world.getHeight(px, py - 1);
									grey += world.getHeight(px + 1, py);
									grey += world.getHeight(px, py + 1);
									grey *= 0.4f;
									grey += 0.5f;
									if (grey < 0) grey = 0;
									if (grey > 1) grey = 1;
									draw.setColor(new Color(R * grey, G * grey, B * grey));
									draw.fillRect(u, v, ds, ds);
								}
							}
						}
					}
				}

			});
			gu.addPainter(draw -> {
				int ds = (int) gu.drawScale;
				for (Structure struct : world.structures.valueCollection()) {
					draw.setColor(struct.color);
					struct.bounds.forEach(l -> {
						draw.fillRect(l.x * ds, l.y * ds, ds, ds);
					});
				}
			});
			gu.addPainter(draw -> {
				int ds = (int) gu.drawScale;
				if (structToPlace != null && gu.isMouseIn()) {
					draw.translate((int) gu.getMouseX() * gu.drawScale, (int) gu.getMouseY() * gu.drawScale);
					draw.setColor(Color.RED);
					structToPlace.bounds.forEach(l -> {
						draw.drawRect(l.x * ds, l.y * ds, ds, ds);
					});
				}
			});

			gu.openUI();

			gu.setKeyEvent("moveup", KeyStroke.getKeyStroke("UP"), (ActionEvent) -> {
				camy -= cams / 10;
				updateCam();
			});

			gu.setKeyEvent("movedown", KeyStroke.getKeyStroke("DOWN"), (ActionEvent) -> {
				camy += cams / 10;
				updateCam();
			});

			gu.setKeyEvent("moveright", KeyStroke.getKeyStroke("RIGHT"), (ActionEvent) -> {
				camx += cams / 10;
				updateCam();
			});

			gu.setKeyEvent("moveleft", KeyStroke.getKeyStroke("LEFT"), (ActionEvent) -> {
				camx -= cams / 10;
				updateCam();
			});

			gu.onMouseMove = o -> {
				mousepos.setText(String.format("Mouse: (%.1f, %.1f)", gu.getMouseX(), gu.getMouseY()));
				int mouseX = (int) gu.getMouseX();
				int mouseY = (int) gu.getMouseY();
				if (mouseX != lastMouseX || mouseY != lastMouseY) {
					lastMouseX = mouseX;
					lastMouseY = mouseY;
					if (structToPlace != null) {
						gu.notifyRedraw();
					}
				}
			};

			gu.addMouse(mouse);

			updateCam();

			frame.add(ui);
			frame.pack();
			frame.setVisible(true);

			addStruct.setText("Place Building");
		});
	}

	private void updateCam() {
		gu.setCamera(camx, camy, cams, true, 10);
	}
}
