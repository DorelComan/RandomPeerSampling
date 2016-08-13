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
import java.util.List;

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
  public static byte[] toBytes(Object object) {
    try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
      try (ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream)) {
        outputStream.writeObject(object);
        outputStream.flush();
        return byteArrayOutputStream.toByteArray();
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

  public static <T> T fromBytes(ByteBuf bytes) {
    try (ByteBufInputStream bis = new ByteBufInputStream(bytes)) {
      return fromBytes(bis);
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
  private static <T> T fromBytes(InputStream inputStream) {
    try (ObjectInput in = new ObjectInputStream(inputStream)) {
      T object = (T) in.readObject();
      return object;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Reads a serializeable object from a {@link ByteBuf}
   *
   * @param buf The input
   * @param <T> The generic type of the object
   * @return The deserialized object
   */
  public static <T> T fromByteBuf(ByteBuf buf) {
    return fromBytes(buf);
  }

  public static <T> T fromByteBufs(List<ByteBuf> bufs) {

    try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
      for (ByteBuf buf : bufs) {
        bos.write(buf.nioBuffer().array());
      }
      try (ByteArrayInputStream in = new ByteArrayInputStream(bos.toByteArray())) {
        return fromBytes(in);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
