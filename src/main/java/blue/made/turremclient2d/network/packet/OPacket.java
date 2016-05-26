package blue.made.turremclient2d.network.packet;

import io.netty.buffer.ByteBuf;

/**
 * Created by Sam Sartor on 5/25/2016.
 */
public interface OPacket {
	int save(ByteBuf data);
}
