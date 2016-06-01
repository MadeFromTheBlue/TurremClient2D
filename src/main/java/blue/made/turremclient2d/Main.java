package blue.made.turremclient2d;

import blue.made.bcf.BCFWriter;
import blue.made.turremclient2d.network.Network;
import blue.made.turremclient2d.network.packet.OPacket;
import blue.made.turremclient2d.ui.UI;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * Created by Sam Sartor on 5/25/2016.
 */
public class Main {
	static final String HOST = System.getProperty("host", "127.0.0.1");
	static final int PORT = Integer.parseInt(System.getProperty("port", "16127"));

	public static void main(String[] args) throws Exception {
		String name = "test2d";
		Network net = new Network(HOST, PORT);
		net.connect();
		Game.INSTANCE.net = net;
		UI.show(Game.INSTANCE);
		net.send(data -> {
			BCFWriter bcf = new BCFWriter(data);
			BCFWriter.Map map = bcf.startMap();
			map.writeName("name");
			map.write(name);
			map.end();
			return 0x01;
		});
		net.send(data -> {
			BCFWriter bcf = new BCFWriter(data);
			BCFWriter.Map map = bcf.startMap();
			map.writeName("what");
			map.write("terrain_chunk");
			map.writeName("range");
			BCFWriter.Map range = map.startMap();
			range.writeName("x0");
			range.write(Integer.MIN_VALUE);
			range.writeName("x1");
			range.write(Integer.MAX_VALUE);
			range.writeName("y0");
			range.write(Integer.MIN_VALUE);
			range.writeName("y1");
			range.write(Integer.MAX_VALUE);
			range.end();
			map.end();
			return 0x02;
		});
	}
}
