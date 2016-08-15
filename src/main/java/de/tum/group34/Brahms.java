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
import org.apache.commons.lang3.tuple.Pair;
import rx.Observable;
import rx.subjects.BehaviorSubject;

public class Brahms {

  private static final long SLEEP_TIME = 5000; //Time between iterations of the algorithm
  private Peer ownIdentity;

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

  public Brahms(Peer ownIdentity, List<Peer> list, NseClient nseClient, PullClient pullClient,
                PushReceiver pushReceiver, PushSender pushSender, TcpClientFactory tcpClientFactory){

    this.ownIdentity = ownIdentity;
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

  /**
   * Method which start the Brahms algorithm, it do infinte iterations updating the localView
   */
  public void start() {

    List<Peer> tempList;

    int firstTime = 0; //Just to know it's the first time we enter

    while (true) { // every iteration to be executed periodically

      System.out.println("-- new algorithm round -- ");

      setSizeEstimation();
      System.out.println("\nSampl: " + samplSize + " sizeEst " + sizeEst);

      int nmbPushes = ((int) Math.round(alfa * viewSize));
      int nmbPulls = (int) Math.round(beta * viewSize);
      int nmbSamples = (int) Math.round(gamma * viewSize);

      // Push to Peers from local View -
      List<Peer> peersToPushMyId = rand(getLocalView(), nmbPushes);
      System.out.println("Pushing" + peersToPushMyId);
      pushSender.sendMyId(peersToPushMyId)
          .toBlocking()
          .firstOrDefault(Collections.emptyList());

      // Send pull requests and save incoming lists in pullList
      ArrayList<Peer> pullList = new ArrayList<>();
      List<Peer> randomList = rand(getLocalView(), nmbPulls);
      pullList.addAll(pullClient.makePullRequests(randomList)
          .toBlocking()
          .firstOrDefault(new ArrayList<>()));

      deleteOwnIdentityFromList(pullList);
      System.out.println("\nPulled peers: " + pullList.size());//todo
      pullList.forEach(peer -> System.out.println(peer.getIpAddress().toString()));//todo

      // Save all push receive in pushList
      ArrayList<Peer> pushList = new ArrayList<>();
      pushList.addAll(pushReceiver.getPushList().toBlocking().first());

      System.out.println("\nPushReceived: " + pushList.size());//todo
      pushList.forEach(peer -> System.out.println(peer.getIpAddress().toString()));//todo

      if ((pushList.size() <= nmbPushes && pushList.size() != 0 && pullList.size() != 0)
          || (pushList.size() == 0 && firstTime == 0 && pullList.size() != 0)) {

        if(firstTime == 0){
          pushList.addAll(getLocalView()); // We have to get the first pushed peers from the Gossip in the PushList
          firstTime = 1;
        }
        System.out.println("\nModifing stuff\n");//todo
        tempList = new ArrayList<>();
        tempList.addAll(rand(pushList, nmbPushes));
        tempList.addAll(rand(pullList, nmbPulls));
        tempList.addAll(randSamples(samplList, nmbSamples));
        setLocalView(tempList);
        viewListSubject.onNext(getLocalView()); // Inform any waiting
      }

      // validate samplers
      validateSamples();
      pushList.addAll(pullList); // pushList + pullList to be added at sample
      updateSample(pushList);

      System.out.println("Local");
      getLocalView().forEach(peer -> System.out.println(peer));

      System.out.println("Sampler");
      samplList.forEach(sampler -> System.out.println(sampler.sample()));

    }
  }

  /**
   * Checks if a Sampler is still valid by checking the underlying peers TCP connection
   *
   * @param samplersToCheck The list of Samplers we want to check if they are still alive
   * @return A list of indexes from the passed in samplersToCheck list with Samplers that are not
   * valid anymore
   */
  private Observable<List<Integer>> probeSamplers(List<Sampler> samplersToCheck) {

    List<Pair<Integer, Sampler>> indexedSamples = new ArrayList<>(samplersToCheck.size());
    for (int i = 0; i < samplersToCheck.size(); i++) {
      Sampler s = samplersToCheck.get(i);
      if (s.sample() != null) {
        indexedSamples.add(Pair.of(i, s));
      }
    }

    return Observable.from(indexedSamples)
        .flatMap(indexedSampler -> pullClient.executePullRequest(indexedSampler.getRight().sample())
            .map(peers -> -1) // Peer is alive / Sampler is valid indicated by -1
            .onErrorReturn(e -> indexedSampler.getLeft()) // Peer not alive anymore
        )
        .filter(index -> index >= 0)
        .toList();
  }

  /**
   * Validate the Samplers and replace invalid Samplers with new ones
   */
  private void validateSamples() {

    List<Integer> invalidSamplerIndexes =
        probeSamplers(samplList).toBlocking().firstOrDefault(Collections.emptyList());

    // Replace invalid Samplers with new Samplers
    for (int index : invalidSamplerIndexes) {
      System.out.println("INVALID " + samplList.get(index).sample());
      samplList.set(index, new Sampler());
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
  public static List<Peer> rand(List<Peer> list, int n) {

    Collections.shuffle(list);

    if (n >= list.size()) {
      return list;
    } else {
      return list.subList(0, n);
    }
  }

  /**
   * Method similar with the one above, but gets and returns a list of samplers instead of peers.
   */

  public static List<Peer> randSamples(List<Sampler> list, int n) {

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
  private void setSizeEstimation() {

    sizeEst = nseClient.getNetworkSize().toBlocking().first().doubleValue();
    Double temp = Math.cbrt(sizeEst);

    this.samplSize = temp.intValue();
    this.viewSize = temp.intValue();
  }

  /**
   * Method which returns a random Peer used by the QueryServer to answer the requests
   */

  public Observable<Peer> getRandomPeerObservable() {

    return viewListSubject
        .flatMap(viewList -> {
          Integer i = secureRandom.nextInt(viewList.size());
          //System.out.println("Getting Peer\n");
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

  private void deleteOwnIdentityFromList(List<Peer> list){

    List<Peer> tempList = new ArrayList<>(list);
    tempList.forEach(peer -> {
      if(ownIdentity.getIpAddress().toString().equals(peer.getIpAddress().toString())){
        list.remove(peer);
      }
    });
  }
}
