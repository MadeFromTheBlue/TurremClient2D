package blue.made.turremclient2d.network.packet;

import io.netty.buffer.ByteBuf;

/**
 * Created by Sam Sartor on 5/25/2016.
 */
public interface IPacket {
	interface Loader {
		IPacket load(ByteBuf data);
	}

	void onProcess();
}
