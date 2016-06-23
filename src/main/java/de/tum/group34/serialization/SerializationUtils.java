package de.tum.group34.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * @author Hannes Dorfmann
 */
public class SerializationUtils {

  private SerializationUtils() {
  }

  /**
   * Converts an Object into a byte array
   *
   * @param object The object you want to serialize
   * @return Byte array of the object to
   */
  public static byte[] toBytes(Serializable object) {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream outputStream = null;
    try {
      outputStream = new ObjectOutputStream(bos);
      outputStream.writeObject(object);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      if (outputStream != null) {
        try {
          outputStream.close();

          if (bos != null) {
            bos.close();
          }
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }
    return bos.toByteArray();
  }

  /**
   * Converts a byte array into an object
   *
   * @param bytes The bytes representation of an object
   * @param <T> The generic type (will automatically cast the object to the desired Type)
   * @return The object
   */
  public static <T> T fromBytes(byte[] bytes) {
    ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
    ObjectInput in = null;
    try {
      in = new ObjectInputStream(bis);
      T object = (T) in.readObject();
      return object;
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      try {
        bis.close();
        if (in != null) {
          in.close();
        }
      } catch (IOException ex) {
        throw new RuntimeException((ex));
      }
    }
  }
}
