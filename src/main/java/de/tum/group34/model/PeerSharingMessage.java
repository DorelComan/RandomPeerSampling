package de.tum.group34.model;

/**
 * This message represents an incoming message sent over gossip to share the a Peer
 *
 * @author Hannes Dorfmann
 */
public class PeerSharingMessage {
  private int messageId;
  private Peer peer;

  public PeerSharingMessage(int messageId, Peer peer) {
    this.messageId = messageId;
    this.peer = peer;
  }

  public int getMessageId() {
    return messageId;
  }

  public Peer getPeer() {
    return peer;
  }
}
