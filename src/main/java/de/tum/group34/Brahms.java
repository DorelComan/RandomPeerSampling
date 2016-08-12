package de.tum.group34;

import de.tum.group34.model.Peer;
import de.tum.group34.model.Sampler;
import de.tum.group34.nse.NseClient;
import de.tum.group34.pull.PullClient;
import de.tum.group34.push.PushReceiver;
import de.tum.group34.push.PushSender;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import rx.Observable;
import rx.subjects.BehaviorSubject;

public class Brahms {

  private static final long SLEEP_TIME = 2000;

  private BehaviorSubject<List<Peer>> viewListSubject = BehaviorSubject.create();
  private SecureRandom secureRandom = new SecureRandom();

  private List<Sampler> samplList; // Sample list
  private List<Peer> viewList;     // View List
  private int samplSize; // l2 - size of the list of Samplers - cubic root of @sizeEst
  private int viewSize; // l1 - size of the local View - cubic root of @sizeEst

  private PullClient pullClient;
  private NseClient nseClient;
  private PushReceiver pushReceiver;
  private PushSender pushSender;

  private Double alfa;
  private Double beta;
  private Double gamma;
  private Double sizeEst; // Size estimation from NSE
  private TcpClientFactory tcpClientFactory;

  /**
   * Initialization of the algorithm
   */
  public Brahms(List<Peer> list, NseClient nseClient, PullClient pullClient,
      PushReceiver pushReceiver, PushSender pushSender, TcpClientFactory tcpClientFactory) {

    alfa = beta = 0.45;
    gamma = 0.1;

    setLocalView(list);
    samplList = new ArrayList<>();
    this.tcpClientFactory = tcpClientFactory;
    this.nseClient = nseClient;
    this.pullClient = pullClient;
    this.pushReceiver = pushReceiver;
    this.pushSender = pushSender;

    System.out.println("Initial list:");//todo
    list.forEach(peer -> System.out.println(peer.getIpAddress().toString()));//todo

    setSizeEstimation(); // Setting the size estimation for the network thanks to NSE

    for (int i = 0; i < samplSize; i++) // Setting the list of samplers
      samplList.add(new Sampler());

    updateSample(list);
  }

  public void start() throws InterruptedException {

    List<Peer> tempList;

    Integer iter = 0; //todo

    while (true) { // every iteration to be executed periodically

      System.out.println("\n -- ITER: " + iter++ + " -- ");

      setSizeEstimation();
      System.out.println("\nSampl: " + samplSize + " sizeEst " + sizeEst);

      Integer nmbPushes = ((int) Math.round(alfa * viewSize));
      Integer nmbPulls = (int) Math.round(beta * viewSize);
      Integer nmbSamples = (int) Math.round(gamma * viewSize);

      System.out.println("nmbPushes: " + nmbPushes + " nmbSamples " + nmbSamples);//todo

      // Push to Peers from local View - TODO: problem if have a small list and what about re-sending to the same

      for (int i = 0; i < nmbPushes; i++) {
        List<Peer> peer;
        peer = rand(getLocalView(), 1);
        //System.out.println("PUSH: " + peer.get(0).getIpAddress().toString()); //todo
        pushSender.sendMyIdTo(peer.get(0));
      }

      // Send pull requests and save incoming lists in pullList
      ArrayList<Peer> pullList = new ArrayList<>();
      pullList.addAll(pullClient.makePullRequests(rand(getLocalView(), nmbPulls))
          .toBlocking().first());

      // System.out.println("\nPulled peers: " + pullList.size());//todo
      // pullList.forEach(peer -> System.out.println(peer.getIpAddress().toString()));//todo

      // Save all push receive in pushList
      ArrayList<Peer> pushList = new ArrayList<>();
      pushList.addAll(pushReceiver.getPushList().toBlocking().first());

      // System.out.println("\nPushReceived: " + pushList.size());//todo
      // pushList.forEach(peer -> System.out.println(peer.getIpAddress().toString()));//todo

      if (pushList.size() <= nmbPushes &&
          pushList.size() != 0 &&
          pullList.size() != 0) {

        System.out.println("Modifing stuff");//todo
        tempList = new ArrayList<>();
        tempList.addAll(rand(pushList, nmbPushes));
        tempList.addAll(rand(pullList, nmbPulls));
        tempList.addAll(randSamples(samplList, nmbSamples));
        setLocalView(tempList);
        viewListSubject.onNext(getLocalView()); // Inform any waiting
      }

      pushList.addAll(pullList); // pushList + pullList to be added at sample
      updateSample(pushList);

      List<Peer> peersThatAreAlive = getPeersThatAreAlive(getLocalView()).toBlocking().first();
      setLocalView(peersThatAreAlive);

      // TODO
      // System.out.println("\nNew Sample");
      // samplList.forEach(sampler -> System.out.println(sampler.sample().getIpAddress().toString()));

      Thread.sleep(SLEEP_TIME);
    }
  }

  /**
   * Checks if the peers are still alive (TCP connection can be established)
   *
   * @param peersToCheck The list of peers we want to check if they are still alive
   * @return A list of peers that are alive. The peer that are not alive has been filtered out from
   * original input parameter.
   */
  Observable<List<Peer>> getPeersThatAreAlive(List<Peer> peersToCheck) {
    return Observable.from(peersToCheck)
        .flatMap(peer ->
            tcpClientFactory.newClient(peer.getIpAddress())
                .createConnectionRequest()
                .flatMap(connection -> connection
                    .ignoreInput().map(aVoid -> true)) // Peer alive -> return true
                .onErrorReturn(throwable -> false) // Peer no longer alive -> return false
                .filter(peerAlive -> peerAlive) // Only continues with peers that are alive
                .map(aVoid -> peer)
        ).toList();
  }

  public void validateSamples() {

    for (int i = 0; i < samplSize; i++) {
      if (!samplList.get(i).validate()) {
        samplList.set(i, new Sampler());
      }
    }
  }

  /**
   * Method used for updating the Sampler peers given a list using the probabilistic method next()
   */
  public void updateSample(List<Peer> list) {

    if (samplList.size() != samplSize) {

      if (samplList.size() < samplSize) {
        for (int i = samplList.size(); i < samplSize; i++)
          samplList.add(new Sampler());
      } else {
        samplList = samplList.subList(0, samplSize);
      }
    }

    for (Sampler s : samplList)
      for (Peer p : list)
        s.next(p);
  }

  /**
   * It will return a sublist of n elements shuffled of the original, it may return a list with less
   * than n elements if the list is not enough long
   *
   * @param list list from where to take @n random elements
   * @param n number of elements to be returned
   * @return shuffled list
   */
  public static List<Peer> rand(List<Peer> list, Integer n) {

    Collections.shuffle(list);

    if (n >= list.size()) {
      return list;
    } else {
      return list.subList(0, n);
    }
  }

  public static List<Peer> randSamples(List<Sampler> list, Integer n) {

    Collections.shuffle(list);
    List<Peer> randList = new ArrayList<>();

    if (n < list.size()) {
      for (int i = 0; i < n; i++)
        randList.add(list.get(i).sample());
    } else {
      for (Sampler aList : list) randList.add(aList.sample());
    }

    return randList;
  }

  /**
   * It invokes the NSE module to recover the network size estimation
   */
  public void setSizeEstimation() {

    sizeEst = nseClient.getNetworkSize().toBlocking().first().doubleValue();
    Double temp = Math.cbrt(sizeEst);

    this.samplSize = temp.intValue();
    this.viewSize = temp.intValue();
  }

 /* private Double getSizeEstim() {

    return sizeEst;
  } */

  public Observable<Peer> getRandomPeerObservable() {

    return viewListSubject
        .flatMap(viewList -> {
          Integer i = secureRandom.nextInt(viewList.size());
          return Observable.just(viewList.get(i));
        });
  }

  public synchronized List<Peer> getLocalView() {
    return viewList;
  }

  private synchronized void setLocalView(List<Peer> list) {

    viewList = new ArrayList<>();
    list.forEach(peer -> viewList.add(peer.clone()));
  }
}
