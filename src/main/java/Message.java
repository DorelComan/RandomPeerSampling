import io.netty.buffer.ByteBuf;

public class Message {

    public static final int MAX_LENGTH = 64000;
    public static final int TYPE_RPS_QUERY = 540;
    public static final int TYPE_RPS_PEER = 541;

    private int type;


}
