import io.netty.buffer.ByteBuf;
import io.netty.handler.logging.LogLevel;
import io.reactivex.netty.protocol.tcp.server.TcpServer;
import rxnetty.ExamplesEnvironment;

/**
 * @author Hannes Dorfmann
 */
public class Rps {

  public static void main(final String[] args) {

    setupQueryReceiver(args);
  }

  public static void setupQueryReceiver(final String[] args) {
    ExamplesEnvironment env = ExamplesEnvironment.newEnvironment(Rps.class);

    QueryReceiverServer queryReceiverServer = new QueryReceiverServer();

        /*Starts a new TCP server on an ephemeral port.*/
    TcpServer<ByteBuf, ByteBuf> server = TcpServer.newServer(0)
        .enableWireLogging(LogLevel.DEBUG)
        .start(
            connection ->
                connection.writeBytesAndFlushOnEach(connection.getInput()
                    .doOnNext(byteBuf -> System.out.println("New incomming message"))
                    .flatMap((byteBuf -> queryReceiverServer.handleIncommingMessage(byteBuf)))
                ));

        /*Wait for shutdown if not called from the client (passed an arg)*/
    if (env.shouldWaitForShutdown(args)) {
      server.awaitShutdown();
    }

        /*If not waiting for shutdown, assign the ephemeral port used to a field so that it can be read and used by
        the caller, if any.*/
    env.registerServerAddress(server.getServerAddress());
  }
}
