package de.tum.group34.serialization;

import de.tum.group34.model.Peer;
import de.tum.group34.pull.RandomData;
import io.netty.buffer.ByteBuf;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import org.junit.*;

/**
 * Tests for Serialization
 *
 * @author Hannes Dorfmann
 */
public class SerializationTest {

  @Test
  public void socketAddress() {

    InetSocketAddress address = new InetSocketAddress("127.0.0.1", 4242);

    ByteBuf buf = SerializationUtils.toByteBuf(address);
    InetSocketAddress address2 = SerializationUtils.fromByteBuf(buf);
    Assert.assertEquals(address, address2);
  }

  @Test
  public void peer() {
    InetSocketAddress address = new InetSocketAddress("127.0.0.1", 4242);
    Peer peer = new Peer(address);
    ByteBuf buf = SerializationUtils.toByteBuf(peer);
    Peer peer2 = SerializationUtils.fromByteBuf(buf);

    Assert.assertEquals(peer, peer2);
  }

  @Test
  public void peerList() {
    InetSocketAddress address = InetSocketAddress.createUnresolved("127.0.0.1", 4242);
    List<Peer> peers = new ArrayList<>();
    peers.add(new Peer(address));
    peers.add(new Peer(new InetSocketAddress("127.0.0.1", 2356)));

    ByteBuf buf = SerializationUtils.toByteBuf(peers);
    List<Peer> peers2 = SerializationUtils.fromByteBuf(buf);

    Assert.assertEquals(2, peers2.size());
    Assert.assertEquals(peers, peers2);
  }

  @Test
  public void xxlPeerList() {
    int elements = 1000;
    List<Peer> peers = RandomData.getPeerList(elements);
    ByteBuf buf = SerializationUtils.toByteBuf(peers);
    ByteBuf copy = buf.copy();
    List<Peer> peers2 = SerializationUtils.fromByteBuf(copy);

    Assert.assertEquals(elements, peers2.size());
    Assert.assertEquals(peers, peers2);
  }
}
