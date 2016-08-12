package de.tum.group34;

import de.tum.group34.nse.NseClient;
import de.tum.group34.pull.PullClient;
import de.tum.group34.push.PushReceiver;
import de.tum.group34.push.PushSender;
import java.security.SecureRandom;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;

import de.tum.group34.model.Peer;
import de.tum.group34.model.Sampler;
import rx.Observable;
import rx.subjects.BehaviorSubject;

public class Brahms {

  private static final long SLEEP_TIME  = 2000;

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

  /**
   * Initialization of the algorithm
   */
  public Brahms(List<Peer> list, NseClient nseClient, PullClient pullClient,
      PushReceiver pushReceiver, PushSender pushSender) {

    alfa = beta = 0.45;
    gamma = 0.1;

    setLocalView(list);
    samplList = new ArrayList<>();
    this.nseClient = nseClient;
    this.pullClient = pullClient;
    this.pushReceiver = pushReceiver;
    this.pushSender = pushSender;

    System.out.println("Initial list:");//todo
    list.forEach(peer -> System.out.println(peer.getIpAddress().toString()));//todo

    setSizeEstimation(); // Setting the size estimation for the network thanks to NSE
    System.out.println("\nSampl: " + samplSize + "\nsizeEst " + sizeEst);

    for (int i = 0; i < samplSize; i++) // Setting the list of samplers
      samplList.add(new Sampler());

    updateSample(list);
  }

  public void start() throws InterruptedException {

    List<Peer> tempList;

    Integer iter = 0;

    while (true) { // every iteration to be executed periodically

      System.out.println("Iter: " + iter++);

      setSizeEstimation();
      Integer nmbPushes = ((int) Math.round(alfa * viewSize));
      Integer nmbPulls = (int) Math.round(beta * viewSize);
      Integer nmbSamples = (int) Math.round(gamma * viewSize);

      // Push to Peers from local View - TODO: problem if have a small list and what about re-sending to the same
      for (int i = 0; i < nmbPushes; i++)
        pushSender.sendMyIdTo(rand(getLocalView(), 1).get(0));

      // Send pull requests and save incoming lists in pullList
      ArrayList<Peer> pullList = pullClient.makePullRequests(rand(getLocalView(), nmbPulls))
          .toBlocking().first();

      // Save all push receive in pushList
      ArrayList<Peer> pushList = pushReceiver.getPushList()
          .toBlocking().first();

      if (pushList.size() <= nmbPushes &&
          pushList.size() != 0 &&
          pullList.size() != 0) {
        tempList = rand(pushList, nmbPushes);
        tempList.addAll(rand(pullList, nmbPulls));
        tempList.addAll(randSamples(samplList, nmbSamples));
        setLocalView(tempList);
        viewListSubject.onNext(getLocalView()); // Inform any waiting
      }

      Thread.sleep(SLEEP_TIME);
    }
  }

  /*
  public void validateSamples() {

    for (int i = 0; i < samplSize; i++) {
      if (!samplList.get(i).validate()) {
        samplList.set(i, new Sampler());
      }
    }
  }
  */

  /**
   * Method used for updating the Sampler peers given a list using the probabilistic method next()
   */
  public void updateSample(List<Peer> list) {

    for (Sampler s : samplList)
      for (Peer p : list)
        s.next(p);
  }

  public static List<Peer> rand(List<Peer> list, Integer n) {

    Collections.shuffle(list);

    if(n > list.size())
      return list;
    else
      return list.subList(0, n - 1);
  }

  public static List<Peer> randSamples(List<Sampler> list, Integer n) {

    Collections.shuffle(list);

    List<Peer> randList = new ArrayList<>();

    if( n < list.size())
      for (int i = 0; i < n; i++)
        randList.add(list.get(i).sample());

    else
      for (int i = 0; i < list.size(); i++)
        randList.add(list.get(i).sample());

    return randList;
  }

  /**
   * It invokes the NSE module to recover the network size estimation
   */
  public synchronized void setSizeEstimation() {

    sizeEst = nseClient.getNetworkSize().toBlocking().first().doubleValue();
    Double temp = Math.cbrt(sizeEst);

    this.samplSize = temp.intValue();
    this.viewSize = temp.intValue();
  }

  private synchronized Double getSizeEstim() {

    return sizeEst;
  }

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

  private synchronized void setLocalView(List<Peer> list){

    this.viewList = list;
  }

}
