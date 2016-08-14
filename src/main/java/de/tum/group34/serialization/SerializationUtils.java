package de.tum.group34.serialization;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.IOUtils;

/**
 * Some helper / util methods for serialization / deserialization
 *
 * @author Hannes Dorfmann
 */
public class SerializationUtils {

  public static final char END_DELIMITER = '|';

  private SerializationUtils() {
  }

  /**
   * Converts an Object into a byte array
   *
   * @param object The object you want to serialize
   * @return Byte array of the object to
   */
  public static byte[] toBytes(Object object) {
    try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
      try (ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream)) {
        outputStream.writeObject(object);
        outputStream.flush();
        byteArrayOutputStream.write(END_DELIMITER);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return bytes;
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Converts a serializeable object ot a {@link ByteBuf}
   *
   * @param object The object to serialize
   * @return The ByteBuf representation of the passed object.
   */

  public static ByteBuf toByteBuf(Object object) {
    byte[] bytes = toBytes(object);

    try (ByteBufOutputStream out = new ByteBufOutputStream(
        Unpooled.buffer(bytes.length))) {
      out.write(bytes);
      return out.buffer();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static <T> T fromByteBuf(ByteBuf bytes) {
    try (ByteBufInputStream bis = new ByteBufInputStream(bytes)) {
      return fromInputStream(bis);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Converts a byte array into an object
   *
   * @param inputStream The inputstream to read the bytes from
   * @param <T> The generic type (will automatically cast the object to the desired Type)
   * @return The object
   */
  private static <T> T fromInputStream(InputStream inputStream) {
    try (ObjectInput in = new ObjectInputStream(inputStream)) {
      T object = (T) in.readObject();
      return object;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static <T> T fromByteArrays(List<byte[]> array) {

    try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
      for (byte[] a : array) {
        bos.write(a);
      }

      byte[] bytes = bos.toByteArray();
      byte last = bytes[bytes.length - 1];
      if (last != END_DELIMITER) {
        throw new MessageException(
            "END DELIMITER wasn't set. END DELIMITER should be: " + END_DELIMITER);
      }

      byte[] bytesWithoutDelimiter = Arrays.copyOfRange(bytes, 0, bytes.length - 1);

      try (ByteArrayInputStream in = new ByteArrayInputStream(bytesWithoutDelimiter)) {
        return fromInputStream(in);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Consumes a ByteBuf and convertes its content to byte array
   *
   * @param buf ByteBuf
   * @return byte array
   */
  public static byte[] byteBufToByteArray(ByteBuf buf) {
    try (ByteBufInputStream in = new ByteBufInputStream(buf)) {
      return IOUtils.toByteArray(in);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Converts a ByteArray to a ByteBuffer
   *
   * @param bytes the byte array
   * @return Byte Buffer

  public static ByteBuf byteArrayToByteBuf(byte[] bytes) {

  try (ByteBufOutputStream out = new ByteBufOutputStream(
  ByteBufAllocator.DEFAULT.buffer(bytes.length))) {
  out.write(bytes);
  return out.buffer();
  } catch (IOException e) {
  throw new RuntimeException(e);
  }
  }
   */
}
