package de.tum.group34.brahms;

import de.tum.group34.Brahms;
import de.tum.group34.model.Peer;
import de.tum.group34.pull.MockPeers;
import de.tum.group34.pull.PullClient;
import de.tum.group34.pull.PullServer;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.tcp.server.TcpServer;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;

public class MockPullClient extends PullClient {

    private Integer size;


    @Override
    public Observable<ArrayList<Peer>> makePullRequests(List<Peer> peers){

        ArrayList<Peer> list = new ArrayList<>();

        list.addAll(MockPeers.getPeerList(peers.size() * getSize()));

        return Observable.just(list);
    }

    public synchronized void setSize(Integer size) {
        this.size = size;
    }

    private synchronized Integer getSize(){
        return size;
    }
}
