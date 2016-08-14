package de.tum.group34.test;


import de.tum.group34.Brahms;
import de.tum.group34.model.Peer;
import de.tum.group34.pull.PullServer;
import de.tum.group34.serialization.MessageParser;
import de.tum.group34.serialization.SerializationUtils;
import io.netty.handler.logging.LogLevel;
import io.reactivex.netty.protocol.tcp.server.TcpServer;
import org.mockito.Mockito;
import rx.Observable;

import java.util.logging.Logger;

/**
 * This help to create a PullServer on the fly for testing of reliabily of the PullClient
 */
public class MockPullServer {
    private static final String LOG_TAG = "PullServer";
    private static final Logger log = Logger.getLogger(PullServer.class.getName());


    public MockPullServer(Peer peer){

        Brahms brahms = Mockito.mock(Brahms.class);
        Mockito.when(brahms.getLocalView()).thenReturn(RandomData.getPeerListBound(5));

        new Thread(() -> {

            TcpServer.newServer(peer.getPullServerAdress()).enableWireLogging(LOG_TAG, LogLevel.DEBUG)
                    .start(
                            connection ->
                                    connection.getInput().doOnNext((byteBuf)-> System.out.println("Someone" + byteBuf)).
                                            doOnNext(MessageParser::isPullLocalViewMessage).flatMap(
                                            byteBuf -> connection.writeBytesAndFlushOnEach(
                                                    Observable.just(SerializationUtils.toBytes(brahms.getLocalView()))
                                                            .doOnNext(bytes -> log.info(
                                                                    LOG_TAG
                                                                            + " sending query response with "
                                                                            + bytes.length
                                                                            + " bytes"))
                                            ).take(1).doOnError(t -> {
                                                log.info("Error");
                                                t.printStackTrace();
                                            })
                                    )
                    ).awaitShutdown();
        }).start();
    }
}
