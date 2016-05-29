import io.netty.buffer.ByteBuf;
import io.netty.handler.logging.LogLevel;
import io.reactivex.netty.protocol.tcp.client.TcpClient;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import org.slf4j.Logger;
import rx.Observable;
import rxnetty.ExamplesEnvironment;

public final class EchoClient {

  public static void main(String[] args) {

    rxnetty.ExamplesEnvironment env = ExamplesEnvironment.newEnvironment(EchoClient.class);

    Logger logger = env.getLogger();
        /*
         * Retrieves the server address, using the following algorithm:
         * <ul>
             <li>If any arguments are passed, then use the first argument as the server port.</li>
             <li>If available, use the second argument as the server host, else default to localhost</li>
             <li>x, start the passed server class and use that address.</li>
         </ul>
         */
    SocketAddress serverAddress =   env.getServerAddress(Rps.class, args);

        /*Create a new client for the server address*/
    TcpClient.newClient(serverAddress)
        .enableWireLogging(LogLevel.DEBUG)
        .createConnectionRequest()
        .flatMap(connection ->
            connection.writeString(Observable.just("Hello World!"))
                .cast(ByteBuf.class)
                .concatWith(connection.getInput())
        )
        .take(1)
        .map(bb -> bb.toString(Charset.defaultCharset()))
        .toBlocking()
        .forEach(System.out::println);
  }
}