package de.tum.group34.brahms;

import de.tum.group34.model.Peer;
import de.tum.group34.pull.RandomData;
import de.tum.group34.pull.PullClient;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;

public class MockPullClient extends PullClient {

    private Integer size;


    @Override
    public Observable<ArrayList<Peer>> makePullRequests(List<Peer> peers){

        ArrayList<Peer> list = new ArrayList<>();

        list.addAll(RandomData.getPeerList(peers.size() * getSize()));

        return Observable.just(list);
    }

    public synchronized void setSize(Integer size) {
        this.size = size;
    }

    private synchronized Integer getSize(){
        return size;
    }
}
