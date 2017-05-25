package scala.scalanative
package native

/** Typeclass that abstracts away memory allocator strategy. */
trait Alloc {

  /** Allocates memory of given size. */
  def alloc(size: CSize): Ptr[Byte]

  /** Reallocates previously allocated memory with a different size.
   *  Might not be supported by all allocators.
   */
  def realloc(ptr: Ptr[Byte], newSize: CSize): Ptr[Byte]

  /** Frees previously allocated memory.
   *  Might not be supported by all allocators.
   */
  def free(ptr: Ptr[Byte]): Unit
}

object Alloc {

  /** Standard system allocator behind malloc/free. */
  val system: Alloc = new Alloc {
    def alloc(size: CSize) =
      stdlib.malloc(size)
    def realloc(ptr: Ptr[Byte], newSize: CSize): Ptr[Byte] =
      stdlib.realloc(ptr, newSize)
    def free(ptr: Ptr[Byte]): Unit =
      stdlib.free(ptr)
  }
}
