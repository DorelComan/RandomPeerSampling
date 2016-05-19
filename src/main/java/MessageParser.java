import io.netty.buffer.ByteBuf;

public class MessageParser {

    public MessageParser(){

    }

    public int getType(ByteBuf buf) throws MessageLengthException, UnknownMessage {

        int buf_size = buf.readableBytes();

        if(buf_size > Message.MAX_LENGTH)
            throw new MessageLengthException(); // TODO: pass message string

        int size = buf.getUnsignedShort(0);

        if(size != buf_size)
            throw new MessageLengthException();

        int type = buf.getUnsignedShort(16);

        //TODO: write an if to control type is one of our ones

        return type;
    }
}
