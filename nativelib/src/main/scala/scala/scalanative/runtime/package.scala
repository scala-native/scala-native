package scala.scalanative

import scala.reflect.ClassTag
import native._
import runtime.Intrinsics._

package object runtime {

  /** Used as a stub right hand of intrinsified methods. */
  def undefined: Nothing = throw new UndefinedBehaviorError

  /** Returns info pointer for given type. */
  def typeof[T](implicit ct: ClassTag[T]): Ptr[Type] = undefined

  /** Intrinsified unsigned devision on ints. */
  def divUInt(l: Int, r: Int): Int = undefined

  /** Intrinsified unsigned devision on longs. */
  def divULong(l: Long, r: Long): Int = undefined

  /** Intrinsified unsigned remainder on ints. */
  def remUInt(l: Int, r: Int): Int = undefined

  /** Intrinsified unsigned remainder on longs. */
  def remULong(l: Long, r: Long): Long = undefined

  /** Intrinsified byte to unsigned int converstion. */
  def byteToUInt(b: Byte): Int = {
    if (b > 0) 0 + b
    else Byte.MaxValue.toInt + (-b)
  }

  /** Intrinsified byte to unsigned long conversion. */
  def byteToULong(b: Byte): Long = undefined

  /** Intrinsified short to unsigned int conversion. */
  def shortToUInt(v: Short): Int = undefined

  /** Intrinsified short to unsigned long conversion. */
  def shortToULong(v: Short): Long = undefined

  /** Intrinsified int to unsigned long conversion. */
  def intToULong(v: Int): Long = undefined

  /** Select value without branching. */
  def select[T](cond: Boolean, thenp: T, elsep: T)(
      implicit ct: ClassTag[T]): T = undefined

  /** Allocate memory in gc heap using given info pointer. */
  def alloc(ty: Ptr[Type], size: CSize): Ptr[_] = {
    val ptr = GC.malloc(size).cast[Ptr[Ptr[Type]]]
    !ptr = ty
    ptr
  }

  /** Allocate memory in gc heap using given info pointer.
   *
   * The allocated memory cannot be used to store pointers.
   */
  def allocAtomic(ty: Ptr[Type], size: CSize): Ptr[_] = {
    val ptr = GC.malloc_atomic(size).cast[Ptr[Ptr[Type]]]
    // initialize to 0
    `llvm.memset.p0i8.i64`(ptr.cast[Ptr[Byte]], 0, size, 1, false)
    !ptr = ty
    ptr
  }

  /** Read type information of given object. */
  def getType(obj: Object): Ptr[Type] = !obj.cast[Ptr[Ptr[Type]]]

  /** Get monitor for given object. */
  def getMonitor(obj: Object): Monitor = Monitor.dummy

  /** Initialize runtime with given arguments and return the
   * rest as Java-style array.
   */
  def init(argc: Int, argv: Ptr[Ptr[Byte]]): ObjectArray = {
    GC.init()

    val args = new scala.Array[String](argc - 1)

    // skip the executable name in argv(0)
    var c = 0
    while (c < argc - 1) {
      // use the default Charset (UTF_8 atm)
      args(c) = fromCString(argv(c + 1))
      c += 1
    }

    args.asInstanceOf[ObjectArray]
  }
}
