package de.tum.group34.pull;

import de.tum.group34.model.Peer;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Random;

public class RandomData {

  private static Random random = new Random();

  private RandomData() {
  }

  public static Peer getPeer() {
    return getPeerList(1).get(0);
  }

  public static ArrayList<Peer> getPeerList(int n) {
    ArrayList<Peer> peers = new ArrayList<>();

    for (int i = 0; i < n; i++) {
      int val1 = random.nextInt(255);
      int val2 = random.nextInt(255);
      Peer peer = new Peer(new InetSocketAddress("127.0." + val1 + "." + val2, val1 + 5000));
      peers.add(peer);
    }

    return peers;
  }

  public static byte[] getHostKey() {
    byte[] bytes = new byte[512];
    random.nextBytes(bytes);
    return bytes;
  }
}
