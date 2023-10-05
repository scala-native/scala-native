package scala.scalanative.javalibintf;

import java.nio.ByteBuffer;
import java.nio.Buffer;

/**
 * Utilities to interface {@link java.nio.Buffer}s and Scala Native Ptr[_].
 *
 * <p>{@link java.nio.Buffer}s can be <em>direct</em> buffers or
 * <em>indirect</em> buffers. Indirect buffers use an underlying array (like
 * {@code int[]} in Java or {@code Array[Int]} in Scala). Direct buffers are
 * supposed to use off-heap memory.
 *
 * <p>In a Scala Native environment, the equivalent of off-heap memory for
 * buffers of primitive numeric types can be access via pointers.
 *
 * <p>This class provides methods to wrap Ptr[_] as direct Buffers, and
 * extract references to TypedArrays from direct Buffers.
 */
public final class PointerBuffer {
  private PointerBuffer() {}

  /**
   * Wraps a ScalaNative {@code Ptr[Byte]} as a direct
   * {@link java.nio.ByteBuffer}.
   *
   * <p>The provided {@code ptr} and ${@code size} parametesr must be a valid Scala Native
   * {@code Ptr[Byte]} and size of referenced memory expressed in bytes, 
   * otherwise the behavior of this method is not specified.
   *
   * <p>The returned {@link java.nio.ByteBuffer} has the following properties:
   *
   * <ul>
   *   <li>It has a {@code capacity()} equal to the {@code size}.</li>
   *   <li>Its initial {@code position()} is 0 and its {@code limit()} is its capacity.</li>
   *   <li>It is a direct buffer backed by the provided {@code Ptr[Byte]}:
   *     changes to one are reflected on the other.</li>
   * </ul>
   *
   * @param ptr a ScalaNative {@code Ptr[_]}
   * @param size size of memory chunk passed by @param ptr
   * @return direct ByteBuffer backed by @param ptr and capacity/limit of @param size
   */
  public static final ByteBuffer wrapPointerByte(Object ptr, int size) {
    throw new AssertionError("stub");
  }

  /**
   * Tests whether the given {@link java.nio.Buffer} is backed by an accessible
   * Scala Native {@code Ptr[_]}.
   *
   * <p>In particular, it is true for all {@link java.nio.Buffer}s created with
   * any of the {@code wrapPointerX} methods of this class.
   *
   * <p>If this method returns {@code true}, then {@code pointer(buffer)}
   * does not throw any {@link UnsupportedOperationException}.
   *
   * @param buffer Any valid {@link Buffer} instance
   * @return
   *   true if and only if the provided {@code buffer} is backed by an
   *   accessible ScalaNative {@code Ptr[_]}
   *
   * @see PointerBuffer#pointer(Buffer)
   */
  public static final boolean hasPointer(Buffer buffer) {
    throw new AssertionError("stub");
  }

   /**
   * Returns a ScalaNative {@code Ptr[_]} view of the provided
   * {@link java.nio.Buffer}.
   *
   * @param buffer Any valid {@link Buffer} instance
   * @return
   *   a ScalaNative {@code Ptr[_]} view of the provided {@code buffer}
   *
   * @throws UnsupportedOperationException
   *   if the provided {@code buffer} is read-only or is not backed by a
   *   ScalaNative {@code Ptr[_]}, i.e., if {@code hasPointer(buffer)}
   *   returns {@code false}
   *
   * @see PointerBuffer#hasPointer(Buffer)
   */
   public static final Object pointer(Buffer buffer) {
    throw new AssertionError("stub");
  }
}
