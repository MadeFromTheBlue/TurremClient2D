package blue.made.turremclient2d;

import blue.made.turremclient2d.network.Network;
import blue.made.turremclient2d.network.packet.OPacket;
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
		TimeUnit.SECONDS.sleep(5);
		net.send(data -> {
			ByteBuf namebytes = Unpooled.copiedBuffer(name, StandardCharsets.UTF_8);
			data.writeByte(namebytes.readableBytes());
			data.writeBytes(namebytes);
			return 1;
		});
		TimeUnit.SECONDS.sleep(5);
		net.disconnect();
	}
}
