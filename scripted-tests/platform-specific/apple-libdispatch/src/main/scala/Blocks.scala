import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

import scala.scalanative.libc.stdlib
import scala.scalanative.runtime.{Intrinsics, fromRawPtr}
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

import dispatch._

/** Blocks, as defined by the Clang block ABI, invoking Scala closures.
 *
 *  A block cannot hold a reference the GC would trace, it captures an id of the
 *  closure retained here until the last copy of the block is disposed.
 */
object Blocks {
  // struct Block_literal { isa; flags; reserved; invoke; descriptor; id }
  private type Literal =
    CStruct6[Ptr[Byte], CInt, CInt, Ptr[Byte], Ptr[Byte], CSSize]
  // struct Block_descriptor { reserved; size; copy_helper; dispose_helper }
  private type Descriptor = CStruct4[CSize, CSize, Ptr[Byte], Ptr[Byte]]
  private final val HasCopyDispose = 1 << 25

  private val closures = new ConcurrentHashMap[java.lang.Long, AnyRef]()
  private val ids = new AtomicLong()
  val disposed = new AtomicLong()
  def retained: Int = closures.size()

  def symbol(name: CString): Ptr[Byte] = {
    val defaultHandle = fromRawPtr[Byte](Intrinsics.castLongToRawPtr(-2L))
    val address = dlsym(defaultHandle, name)
    assert(address != null, s"missing symbol ${fromCString(name)}")
    address
  }

  // The heap copy shares the id, bitwise copied by the runtime.
  private val copyHelper: CFuncPtr2[Ptr[Literal], Ptr[Literal], Unit] =
    CFuncPtr2.fromScalaFunction((_: Ptr[Literal], _: Ptr[Literal]) => ())
  private val disposeHelper: CFuncPtr1[Ptr[Literal], Unit] =
    CFuncPtr1.fromScalaFunction { (block: Ptr[Literal]) =>
      closures.remove(block._6.toLong)
      disposed.incrementAndGet()
      ()
    }
  private val invoke0: CFuncPtr1[Ptr[Literal], Unit] =
    CFuncPtr1.fromScalaFunction { (block: Ptr[Literal]) =>
      closures.get(block._6.toLong).asInstanceOf[() => Unit].apply()
    }
  private val invoke1: CFuncPtr2[Ptr[Literal], Ptr[Byte], Unit] =
    CFuncPtr2.fromScalaFunction { (block: Ptr[Literal], arg: Ptr[Byte]) =>
      closures.get(block._6.toLong).asInstanceOf[Ptr[Byte] => Unit].apply(arg)
    }
  private val invoke2: CFuncPtr3[Ptr[Literal], CInt, Ptr[Byte], Unit] =
    CFuncPtr3.fromScalaFunction {
      (block: Ptr[Literal], arg1: CInt, arg2: Ptr[Byte]) =>
        closures
          .get(block._6.toLong)
          .asInstanceOf[(Int, Ptr[Byte]) => Unit]
          .apply(arg1, arg2)
    }

  private lazy val descriptor: Ptr[Descriptor] = {
    val descriptor =
      stdlib.malloc(sizeof[Descriptor]).asInstanceOf[Ptr[Descriptor]]
    descriptor._1 = 0.toCSize
    descriptor._2 = sizeof[Literal]
    descriptor._3 = CFuncPtr.toPtr(copyHelper).asInstanceOf[Ptr[Byte]]
    descriptor._4 = CFuncPtr.toPtr(disposeHelper).asInstanceOf[Ptr[Byte]]
    descriptor
  }
  private lazy val stackBlockClass = symbol(c"_NSConcreteStackBlock")

  /** A heap block owned by the caller, to be passed to `_Block_release`. */
  private def make(closure: AnyRef, invoke: CVoidPtr): Ptr[Byte] = {
    val id = ids.incrementAndGet()
    closures.put(id, closure)
    val literal = stackalloc[Literal]()
    literal._1 = stackBlockClass
    literal._2 = HasCopyDispose
    literal._3 = 0
    literal._4 = invoke.asInstanceOf[Ptr[Byte]]
    literal._5 = descriptor.asInstanceOf[Ptr[Byte]]
    literal._6 = id.toCSSize
    _Block_copy(literal.asInstanceOf[Ptr[Byte]])
  }

  /** `void (^)(void)` */
  def apply(closure: () => Unit): Ptr[Byte] =
    make(closure, CFuncPtr.toPtr(invoke0))

  /** `void (^)(void *)` */
  def apply(closure: Ptr[Byte] => Unit): Ptr[Byte] =
    make(closure, CFuncPtr.toPtr(invoke1))

  /** `void (^)(int, void *)` */
  def apply(closure: (Int, Ptr[Byte]) => Unit): Ptr[Byte] =
    make(closure, CFuncPtr.toPtr(invoke2))
}
