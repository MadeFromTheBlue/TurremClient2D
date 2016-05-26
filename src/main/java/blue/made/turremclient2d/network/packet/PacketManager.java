package blue.made.turremclient2d.network.packet;

import blue.made.turremclient2d.network.packet.in.ServerInfo;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.ReplayingDecoder;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Created by Sam Sartor on 5/25/2016.
 */
public class PacketManager {
	public static final IPacket.Loader[] ipackets = new IPacket.Loader[256];
	public static final Charset charset = StandardCharsets.UTF_8;

	public static MessageToByteEncoder<OPacket> makeEncoder() {
		return new MessageToByteEncoder<OPacket>() {
			ByteBuf data = Unpooled.buffer();

			@Override
			protected void encode(ChannelHandlerContext ctx, OPacket msg, ByteBuf out) throws Exception {
				data.clear();
				int id = msg.save(data);
				if (id < 0 || id > 255) throw new IllegalArgumentException("Packet id " + id + " is not a byte");
				out.writeInt(data.readableBytes() + 1);
				out.writeByte(id);
				out.writeBytes(data);
			}
		};
	}

	public static ByteToMessageDecoder makeDecoder() {
		return new ReplayingDecoder() {
			@Override
			protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
				int len = in.readInt();
				int type = in.readByte() & 0xFF;
				IPacket.Loader loader = ipackets[type];
				//System.out.printf("Packet: %d (%db)%n", type, len);
				out.add(loader.load(in.readSlice(len - 1)));
			}
		};
	}

	static {
		ipackets[1] = (ByteBuf data) -> {
			String name = data.readSlice(data.readByte() & 0xFF).toString(charset);
			String desc = data.readSlice(data.readShort() & 0xFFFF).toString(charset);
			int w = data.readByte() & 0xFF;
			int h = data.readByte() & 0xFF;
			BufferedImage ico = null;
			if (w != 0 && h != 0) {
				ico = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
				for (int x = 0; x < w; x++) {
					for (int y = 0; y < h; y++) {
						ico.setRGB(x, y, data.readInt());
					}
				}
			}
			return new ServerInfo(name, desc, ico, data.readShort(), data.readShort(), data.readShort());
		};
	}
}
