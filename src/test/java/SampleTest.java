import java.net.InetSocketAddress;
import java.security.NoSuchAlgorithmException;
import de.tum.group34.model.Peer;
import de.tum.group34.model.Sampler;

public class SampleTest {

  // TODO write Unit tests
  private InetSocketAddress address1 = new InetSocketAddress("127.0.0.1", 5004);
  private InetSocketAddress address2 = new InetSocketAddress("127.0.150.20", 5004);

  Sampler sampler = new Sampler();

  public SampleTest() throws NoSuchAlgorithmException {

    Peer peer = new Peer();
    peer.setIpAddress(address1);

    sampler.next(peer);

    System.out.println("After init: " + sampler.sample().getIpAddress().toString());

    Peer peer2 = new Peer();
    peer2.setIpAddress(address2);

    sampler.next(peer2);

    System.out.println("After change: " + sampler.sample().getIpAddress().toString());
  }

  public static void main(String args[]) throws NoSuchAlgorithmException {

    new SampleTest();
  }
}
