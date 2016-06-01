package blue.made.turremclient2d;

import blue.made.turremclient2d.network.Network;
import blue.made.turremclient2d.world.World;

import java.util.function.Consumer;

/**
 * Created by Sam Sartor on 5/31/2016.
 */
public class Game {
	public static Game INSTANCE = new Game();

	public World world;
	public Network net;

	public Consumer<Game> onWorldLoad;
	public void onWorldLoad(Consumer<Game> adapter) {
		onWorldLoad = adapter;
	}
}
