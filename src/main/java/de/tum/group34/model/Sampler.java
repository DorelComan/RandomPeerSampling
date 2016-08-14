package de.tum.group34.model;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Sampler {

  private MessageDigest randomPRF; // Hash function
  private Peer q;
  private double randNumber;
  // Random number intialized when the sampler is created, this makes the hashing unique

  public Sampler() {
    q = null;

    try {
      randomPRF = MessageDigest.getInstance("SHA-1");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }

    randNumber = Math.random();
  }

  /**
   * Method that evaluate the hash values of the candidate peer and the peer of Sampler and keep in
   * the Sampler the one with the lowest value.
   *
   * @param elem candidate to substitute the element in the Sampler
   */
  public void next(Peer elem) {

    String strElem = elem.getIpAddress().toString() + randNumber;

    String hashQ = "";
    if (q != null) {
      String strQ = q.getIpAddress().toString() + randNumber;
      hashQ =
          convertByteArrayToHexString(randomPRF.digest(strQ.getBytes(Charset.forName("UTF-8"))));
    }

    String hashElem =
        convertByteArrayToHexString(randomPRF.digest(strElem.getBytes(Charset.forName("UTF-8"))));

    // System.out.println(hashQ);
    // System.out.println(hashElem);

    if (q == null || hashElem.compareTo(hashQ) < 0) {
      q = elem;
    }
  }

  public Peer sample() {
    return q;
  }

  /**
   * Used for internal conversion from byte[] to String
   */
  private String convertByteArrayToHexString(byte[] arrayBytes) {
    StringBuilder stringBuffer = new StringBuilder();
    for (int i = 0; i < arrayBytes.length; i++) {
      stringBuffer.append(Integer.toString((arrayBytes[i] & 0xff) + 0x100, 16)
          .substring(1));
    }
    return stringBuffer.toString();
  }
}
