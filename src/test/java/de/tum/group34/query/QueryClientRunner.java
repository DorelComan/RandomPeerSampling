package de.tum.group34.query;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author Hannes Dorfmann
 */
public class QueryClientRunner {

  public static void main(String args[]) {

    QueryClient.query("127.0.0.1", 3558)
        .take(1)
        .map(byteBuf -> {

          ByteBuf addBuf = Unpooled.buffer(4);
          byteBuf.getBytes(8, addBuf, 4);
          InetAddress address = null;

          try {
            address = InetAddress.getByAddress(addBuf.array());
          } catch (UnknownHostException e) {
            e.printStackTrace();
          }

          System.out.println("address: " + address.toString() + " port: " + byteBuf.getShort(4));

          return byteBuf.getShort(4);
        })
        .toBlocking()
        .forEach(System.out::println);
  }
}
