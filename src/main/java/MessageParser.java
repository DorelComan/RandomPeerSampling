import io.netty.buffer.ByteBuf;

public class MessageParser {

    public MessageParser(){

    }

    public int isRpsQuery(ByteBuf buf) throws MessageLengthException, UnknownMessage {

        int buf_size = buf.readableBytes();

        if(buf_size > Message.MAX_LENGTH)
            throw new MessageLengthException(); // TODO: pass message string

        int size = buf.getUnsignedShort(0);

        if(size != buf_size)
            throw new MessageLengthException();

        int type = buf.getUnsignedShort(16);

        //module.Peer Identity 256 bit


        //Port 16 bit

        int port = buf.getUnsignedShort(288);


        //Reserverd 16 bit

        // IPv4 32 bit / IPv6 128 bit

        buf.getUnsignedInt(320);

        //TODO: write an if to control type is one of our ones

        return type;
    }
}
