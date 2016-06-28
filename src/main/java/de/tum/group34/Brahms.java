package de.tum.group34;

import de.tum.group34.nse.NseClient;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import module.Peer;
import module.Sampler;

public class Brahms {

  ArrayList<Sampler> samplList; // Sample list
  ArrayList<Peer> viewList;     // View List
  Integer samplSize; // l2
  Integer viewSize; // l1

  PullClient pullClient;
  NseClient nseClient;
  PushReceiver pushReceiver;
  PushSender pushSender;

  Float alfa;
  Float beta;
  Float gamma;
  Integer sizeEst;

  /**
   * Initialization of the algorithm
   */
  public Brahms(ArrayList<Peer> list, NseClient nseClient, PullClient pullClient,
      PushReceiver pushReceiver, PushSender pushSender) {

    viewList = list;
    samplList = new ArrayList<>();
    this.nseClient = nseClient;
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
      Integer nmbPushes = Math.round(alfa * viewSize);
      Integer nmbPulls = Math.round(beta * viewSize);
      Integer nmbSamples = Math.round(gamma * viewSize);

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

  public void updateSample(ArrayList<Peer> list) {

    for (Sampler s : samplList)
      for (Peer p : list)
        s.next(p);
  }

  public static ArrayList<Peer> rand(List<Peer> list, Integer n) {

    Collections.shuffle(list);

    return (ArrayList<Peer>) list.subList(0, n - 1);
  }

  public static ArrayList<Peer> randSamples(List<Sampler> list, Integer n) {

    Collections.shuffle(list);

    ArrayList<Peer> randList = new ArrayList<>();

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

  public Peer getRandomPeer() {

    SecureRandom secureRandom = new SecureRandom();
    Integer i = secureRandom.nextInt(viewList.size());

    return viewList.get(i);
  }

  public ArrayList<Peer> getLocalView() {

    return viewList;
  }
}
