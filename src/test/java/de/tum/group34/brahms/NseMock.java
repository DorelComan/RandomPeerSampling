package de.tum.group34.brahms;

import de.tum.group34.TcpClientFactory;
import de.tum.group34.nse.NseClient;
import rx.Observable;

import java.util.concurrent.TimeUnit;

public class NseMock extends NseClient{

    private Integer size;

    /**
     * Creates a new instance
     *
     * @param clientFactory The Factory to create TcpClients on the fly
     * @param interval      The time interval when to query the network size
     * @param timeUnit      The intervals time unit
     */
    public NseMock(TcpClientFactory clientFactory, long interval, TimeUnit timeUnit) {
        super(clientFactory, interval, timeUnit);
    }

    private synchronized Integer getSize() {
        return size;
    }

    public synchronized void setSize(Integer size) {
        this.size = size;
    }

    @Override
    public Observable<Integer> getNetworkSize(){

        return Observable.just(getSize());
    }
}
