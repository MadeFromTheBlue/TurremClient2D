package blue.made.turremclient2d.actors.structure;

import blue.made.bcf.BCFMap;

import java.awt.*;
import java.util.Random;

/**
 * Created by Sam Sartor on 6/8/2016.
 */
public class Structure {
	public final long uuid;
	public Bounds bounds;
	public Color color;

	public Structure(long uuid) {
		Random rand = new Random(uuid);
		this.uuid = uuid;
		this.color = new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
	}

	public void loadFromSpawn(BCFMap map) {
		bounds = Bounds.load(map.get("bounds"));
	}
}
