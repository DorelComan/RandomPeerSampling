package de.tum.group34.test;

import de.tum.group34.model.Peer;
import de.tum.group34.push.PushReceiver;
import de.tum.group34.serialization.SerializationUtils;
import io.netty.buffer.ByteBuf;
import io.netty.handler.logging.LogLevel;
import io.reactivex.netty.protocol.tcp.server.TcpServer;
import rx.Observable;
import rx.subjects.PublishSubject;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class MockPushReceiver {

    private static final String LOG_TAG = PushReceiver.class.getName();
    private static final Logger log = Logger.getLogger(LOG_TAG);

    private final PublishSubject<Peer> pushReceivingSocketBridge = PublishSubject.create();
    private final PublishSubject<Peer> gossipSocketBridge = PublishSubject.create();
    private final Observable<List<Peer>> gossipSocket;
    private final Observable<List<Peer>> pushReceivingSocket;

    public MockPushReceiver(Peer peerT){

        System.out.println("created pushReceiver: " + peerT.getPushServerAddress()+"\n");

        TcpServer<ByteBuf, ByteBuf> pushReceivingServerSocket = TcpServer.newServer(peerT.getPushServerAddress());

        gossipSocket = gossipSocketBridge.buffer(10, TimeUnit.SECONDS).onBackpressureDrop();
            pushReceivingSocket =
                    pushReceivingSocketBridge.buffer(10, TimeUnit.SECONDS).onBackpressureDrop();

            pushReceivingServerSocket.
                    enableWireLogging(LOG_TAG, LogLevel.DEBUG)
                    .start(
                            connection ->
                                    connection.getInput()
                                            .doOnNext(byteBuf -> log.info("\nMOCK: Push Responding Socket received a Message\n"))
                                            .map(SerializationUtils::<Peer>fromByteBuf)
                                            .map(peer -> null)
                    );

    }
}
