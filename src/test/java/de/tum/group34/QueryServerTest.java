package de.tum.group34;

import de.tum.group34.serialization.Message;
import de.tum.group34.serialization.MessageParser;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.ByteBuffer;

public class QueryServerTest {

    ByteBuf queryBuf;

    public QueryServerTest(){
        ByteBuf byteBuf = Unpooled.buffer();
        ByteBuffer byteBuffer = ByteBuffer.allocate(256);

        byteBuffer.putShort(16, (short) 36021);
        byteBuf.setShort(16, (short) 36021);

        int integer = MessageParser.unsignedIntFromShort(byteBuf.getShort(16)); //= (Integer)byteBuf.getShort(16);
        for(int i=0; i < 2; i++)
            System.out.print(" " + (int)byteBuf.getByte(16+i));
        System.out.print("\n" + integer);

    }
    public static void main(String args[]){

        new QueryServerTest();

    }

}
