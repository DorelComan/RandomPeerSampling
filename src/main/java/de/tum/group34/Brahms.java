package de.tum.group34;

import de.tum.group34.nse.NseClient;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import module.Peer;
import module.Sampler;

public class Brahms {

  List<Sampler> samplList; // Sample list
  List<Peer> viewList;     // View List
  Integer samplSize; // l2
  Integer viewSize; // l1

  PullClient pullClient;
  NseClient nseClient;

  Float alfa;
  Float beta;
  Float gamma;
  Integer sizeEst;

  /**
   * Initialization of the algorithm
   */
  public Brahms(ArrayList<Peer> list, NseClient nseClient, PullClient pullClient) {

    viewList = list;
    samplList = new ArrayList<>();
    this.nseClient = nseClient;
    this.pullClient = pullClient;

    this.samplSize = nseClient.getNetworkSize().toBlocking().first();
    this.viewSize = this.samplSize;

    for (int i = 0; i < samplSize; i++)
      samplList.add(new Sampler());

    updateSample(list);
  }

  public void start() {

    List<Peer> pullList;
    List<Peer> pushList;

    while (true) {            // every iteration to be executed periodically
      pullList = new ArrayList<>();
      pushList = new ArrayList<>();

      Integer nmbPushes = Math.round(alfa * viewSize);
      Integer nmbPulls = Math.round(beta * viewSize);
      Integer nmbSamples = Math.round(gamma * viewSize);

      for (int i = 0; i < nmbPushes; i++)
        PushSender.pushRequest(rand(viewList, 1)); // method to be implemented by the Push Sender

      // Send pull requests and save incoming lists in pullList

      pullList = pullClient.makePullRequests(rand(viewList, nmbPulls)).toBlocking().first();

      // Save all push receive in pushList

      pushList = GossipReceiver.getPushList();

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

  public List<Peer> getLocalView() {

    return viewList;
  }
}
