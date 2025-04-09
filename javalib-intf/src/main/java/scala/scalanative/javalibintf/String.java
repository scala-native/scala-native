package scala.scalanative.javalibintf;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Utilities to build {@link java.lang.String}s from lower level
 * primitives.
 */
public final class String {
  private String() {}

  /**
   * Builds a string from a {@link java.nio.ByteBuffer}.
   *
   * @param data the ByteBuffer with the data
   * @param encoding the charset to use to decode the data
   * @return String built from the decoded @param data
   */
  public static final java.lang.String fromByteBuffer(ByteBuffer data, Charset encoding){
    throw new AssertionError("stub");
  }
}
