package blue.made.turremclient2d.actors.structure;

import blue.made.bcf.BCFMap;
import blue.made.turremclient2d.Game;

/**
 * Created by Sam Sartor on 6/8/2016.
 */
public class StructureSpec {
	public Bounds bounds;
	public String id;

	public Structure spawn(BCFMap map) {
		Structure s = new Structure(map.get("id").asNumeric().longValue());
		s.loadFromSpawn(map);
		Game.INSTANCE.world.structures.put(s.uuid, s);
		return s;
	}
}
