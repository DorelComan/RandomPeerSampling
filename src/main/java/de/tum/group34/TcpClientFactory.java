package de.tum.group34;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.tcp.client.TcpClient;
import java.net.InetSocketAddress;

/**
 * @author Hannes Dorfmann
 */
public interface TcpClientFactory {
  TcpClient<ByteBuf, ByteBuf> newClient(InetSocketAddress address);
}
