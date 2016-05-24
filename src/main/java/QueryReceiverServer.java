import io.netty.buffer.ByteBuf;
import rx.Observable;

/**
 * @author Hannes Dorfmann
 */
public class QueryReceiverServer {

  private MessageParser parser;
  private PeerList peerList;
  private PeerConverter peerConverter;

  public Observable<byte[]> handleIncommingMessage(ByteBuf incommingMessage) {

    return Observable.fromCallable(() -> {
      parser.isRpsQuery(incommingMessage); // Will throw an exception otherwise
      return peerList.getRandomPeer();
    }).map(peerConverter::toByteArray);
  }
}
