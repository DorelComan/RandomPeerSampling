package de.tum.group34.serialization;

public class Message {

  public static final int MAX_LENGTH = 64000;
  public static final int TYPE_RPS_QUERY = 540;
  public static final int TYPE_RPS_PEER = 541;
  public static final int LOCAL_VIEW_QUERY = 542;
  public static final int LOCAL_VIEW_RESPONSE = 543;
  public static final int GOSSIP_PUSH = 544;
  public static final int GOSSIP_ANNUNCE = 500;
  public static final int GOSSIP_NOTIFY = 501;
  public static final int GOSSIP_NOTIFICATION = 502;
  public static final int GOSSIP_VALIDATION = 503;
  public static final int NSE_QUERY = 520;
  public static final int NSE_ESTIMATE = 521;
  public static final int PULL_LOCAL_VIEW = 550;
}
