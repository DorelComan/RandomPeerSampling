import de.tum.group34.Brahms;
import de.tum.group34.pull.MockPeers;
import de.tum.group34.pull.PullServer;
import de.tum.group34.serialization.SerializationUtils;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.tcp.client.TcpClient;
import io.reactivex.netty.protocol.tcp.server.TcpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import org.mockito.Mockito;
import rx.Observable;

public class Pull_simple_test {

    public static void main(String[] args) throws IOException {

        Integer modifiedSentence;

        Brahms brahms = Mockito.mock(Brahms.class);
        Mockito.when(brahms.getLocalView()).thenReturn(MockPeers.getPeerList());

        PullServer pullServer = new PullServer(brahms, TcpServer.newServer(1102));

        /*Socket clientSocket = new Socket("127.0.0.1", 1102);
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        byte[] hello = MessageParser.getPullLocalView().array();

        outToServer.write(hello);

        modifiedSentence = inFromServer.read();
        System.out.println("FROM SERVER: " + modifiedSentence);
        clientSocket.close(); */

        TcpClient.newClient(new InetSocketAddress("127.0.0.1", 1102))
                .createConnectionRequest()
                .flatMap(connection ->
                                connection.writeString(Observable.just("Hello"))
                                        .cast(ByteBuf.class)
                                        .concatWith(connection.getInput())
                )
                .take(1)
                .map(SerializationUtils::fromByteBuf)
                ;
    }
}
