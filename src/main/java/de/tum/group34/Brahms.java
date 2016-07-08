package de.tum.group34;

import de.tum.group34.nse.NseClient;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import de.tum.group34.model.Peer;
import de.tum.group34.model.Sampler;
import rx.Observable;
import rx.subjects.BehaviorSubject;

public class Brahms {

  private BehaviorSubject<List<Peer>> viewListSubject = BehaviorSubject.create();
  private SecureRandom secureRandom = new SecureRandom();

  private List<Sampler> samplList; // Sample list
  private List<Peer> viewList;     // View List
  private int samplSize; // l2
  private int viewSize; // l1

  private PullClient pullClient;
  private PushReceiver pushReceiver;
  private PushSender pushSender;

  private float alfa;
  private float beta;
  private float gamma;
  private int sizeEst;

  /**
   * Initialization of the algorithm
   */
  public Brahms(List<Peer> list, NseClient nseClient, PullClient pullClient,
      PushReceiver pushReceiver, PushSender pushSender) {

    viewList = list;
    samplList = new ArrayList<>();
    this.pullClient = pullClient;
    this.pushReceiver = pushReceiver;
    this.pushSender = pushSender;

    this.samplSize = nseClient.getNetworkSize().toBlocking().first();
    this.viewSize = this.samplSize;

    for (int i = 0; i < samplSize; i++)
      samplList.add(new Sampler());

    updateSample(list);
  }

  public void start() {

    while (true) {            // every iteration to be executed periodically
      int nmbPushes = Math.round(alfa * viewSize);
      int nmbPulls = Math.round(beta * viewSize);
      int nmbSamples = Math.round(gamma * viewSize);

      for (int i = 0; i < nmbPushes; i++)
        pushSender.sendMyIdTo(rand(viewList, 1).get(0));

      // Send pull requests and save incoming lists in pullList
      ArrayList<Peer> pullList = pullClient.makePullRequests(rand(viewList, nmbPulls))
          .toBlocking().first();

      // Save all push receive in pushList
      ArrayList<Peer> pushList = pushReceiver.getPushList()
          .toBlocking().first();

      if (pushList.size() <= nmbPushes &&
          pushList.size() != 0 &&
          pullList.size() != 0) {
        viewList = rand(pushList, nmbPushes);
        viewList.addAll(rand(pullList, nmbPulls));
        viewList.addAll(randSamples(samplList, nmbSamples));
        viewListSubject.onNext(viewList); // Inform any waiting
      }
    }
  }

  public void validateSamples() {

    for (int i = 0; i < samplSize; i++) {
      if (!samplList.get(i).validate()) {
        samplList.set(i, new Sampler());
      }
    }
  }

  public void updateSample(List<Peer> list) {

    for (Sampler s : samplList)
      for (Peer p : list)
        s.next(p);
  }

  public static List<Peer> rand(List<Peer> list, Integer n) {

    Collections.shuffle(list);

    return list.subList(0, n - 1);
  }

  public static List<Peer> randSamples(List<Sampler> list, Integer n) {

    Collections.shuffle(list);

    List<Peer> randList = new ArrayList<>();

    for (int i = 0; i < n; i++)
      randList.add(list.get(i).sample());

    return randList;
  }

  public synchronized void setSizeEstimation(Integer size) {

    sizeEst = size;
  }

  private synchronized Integer getSizeEstim() {

    return sizeEst;
  }

  public Observable<Peer> getRandomPeerObservable() {
    return viewListSubject
        .flatMap(viewList -> {
          Integer i = secureRandom.nextInt(viewList.size());
          return Observable.just(viewList.get(i));
        });
  }

  public List<Peer> getLocalView() {

    return viewList;
  }
}
