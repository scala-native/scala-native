package scala.scalanative

import scalanative.annotation.alwaysinline
import scalanative.unsafe._
import scalanative.runtime.Intrinsics._

package object runtime {

  @deprecated("Internal API, deprecated for removal", "0.4.1")
  def toClass(rtti: RawPtr): _Class[_] = {
    castRawPtrToObject(rtti).getClass().asInstanceOf[_Class[_]]
  }

  @deprecated("Internal API, deprecated for removal", "0.4.1")
  @alwaysinline def toRawType(cls: Class[_]): RawPtr = {
    castObjectToRawPtr(cls)
  }

  /** Read type information of given object. */
  @deprecated("Internal API, deprecated for removal", "0.4.1")
  @alwaysinline def getRawType(obj: Object): RawPtr = {
    Intrinsics.castObjectToRawPtr(obj.getClass())
  }

  /** Used as a stub right hand of intrinsified methods. */
  def intrinsic: Nothing = throwUndefined()

  /** Get monitor for given object. */
  @alwaysinline def getMonitor(obj: Object): Monitor = Monitor.dummy

  /** Initialize runtime with given arguments and return the rest as Java-style
   *  array.
   */
  def init(argc: Int, rawargv: RawPtr): scala.Array[String] = {
    val argv = fromRawPtr[CString](rawargv)
    val args = new scala.Array[String](argc - 1)

    // skip the executable name in argv(0)
    var c = 0
    while (c < argc - 1) {
      // use the default Charset (UTF_8 atm)
      args(c) = fromCString(argv(c + 1))
      c += 1
    }

    args
  }

  @alwaysinline def fromRawPtr[T](rawptr: RawPtr): Ptr[T] =
    Boxes.boxToPtr(rawptr)

  @alwaysinline def toRawPtr[T](ptr: Ptr[T]): RawPtr =
    Boxes.unboxToPtr(ptr)

  /** Run the runtime's event loop. The method is called from the generated
   *  C-style after the application's main method terminates.
   */
  @noinline def loop(): Unit =
    ExecutionContext.loop()

  /** Called by the generated code in case of division by zero. */
  @noinline def throwDivisionByZero(): Nothing =
    throw new java.lang.ArithmeticException("/ by zero")

  /** Called by the generated code in case of incorrect class cast. */
  @noinline def throwClassCast(from: RawPtr, to: RawPtr): Nothing = {
    val fromName = loadObject(elemRawPtr(from, MemoryLayout.Rtti.NameOffset))
    val toName = loadObject(elemRawPtr(to, MemoryLayout.Rtti.NameOffset))
    throw new java.lang.ClassCastException(
      s"$fromName cannot be cast to $toName"
    )
  }

  /** Called by the generated code in case of operations on null. */
  @noinline def throwNullPointer(): Nothing =
    throw new NullPointerException()

  /** Called by the generated code in case of unexpected condition. */
  @noinline def throwUndefined(): Nothing =
    throw new UndefinedBehaviorError

  /** Called by the generated code in case of out of bounds on array access. */
  @noinline def throwOutOfBounds(i: Int): Nothing =
    throw new ArrayIndexOutOfBoundsException(i.toString)

  /** Called by the generated code in case of missing method on reflective call.
   */
  @noinline def throwNoSuchMethod(sig: String): Nothing =
    throw new NoSuchMethodException(sig)
}
