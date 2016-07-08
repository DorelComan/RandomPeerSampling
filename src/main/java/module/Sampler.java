package module;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Sampler {

  private MessageDigest randomPRF;
  private Peer q;
  private double randNumber;

  public Sampler() {
    q = null;

    try {
      randomPRF = MessageDigest.getInstance("SHA-1");
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }

    randNumber = Math.random();
  }

  public void next(Peer elem) {

    String strElem = elem.getIpAddress().toString() + randNumber;

    String hashQ = "";
    if (q != null) {
      String strQ = q.getIpAddress().toString() + randNumber;
      hashQ = convertByteArrayToHexString(randomPRF.digest(strQ.getBytes(Charset.forName("UTF-8"))));
    }

    String hashElem = convertByteArrayToHexString(randomPRF.digest(strElem.getBytes(Charset.forName("UTF-8"))));

    System.out.println(hashQ);
    System.out.println(hashElem);

    if (q == null || hashElem.compareTo(hashQ) < 0) {
      q = elem;
    }
  }

  public Peer sample() {

    return q;
  }

  private static String convertByteArrayToHexString(byte[] arrayBytes) {
    StringBuffer stringBuffer = new StringBuffer();
    for (int i = 0; i < arrayBytes.length; i++) {
      stringBuffer.append(Integer.toString((arrayBytes[i] & 0xff) + 0x100, 16)
          .substring(1));
    }
    return stringBuffer.toString();
  }

  /**
   * Used for validating if the peer in the sampler is still online
   */

  public boolean validate() {
    return true; // TODO wtf?
  }
}
