package java.lang.invoke

// scalafmt: { maxColumn = 160 }

import scala.scalanative.annotation._
import scala.scalanative.libc.stdatomic._
import scala.scalanative.libc.stdatomic.memory_order._
import scala.scalanative.meta.LinktimeInfo.isMultithreadingEnabled

/** Native class definition, renamed during NIR generation.
 *
 *  Keep the JDK VarHandle visible to the typer: only Java-defined native varargs methods are recognized as signature-polymorphic by the Scala compilers.
 *  Supported access calls are lowered to NativeVarHandle before NIR generation.
 */
abstract class _VarHandle extends java.lang.constant.Constable {
  def varType(): Class[_]
  def coordinateTypes(): java.util.List[Class[_]]
  def hasInvokeExactBehavior(): Boolean
  def withInvokeBehavior(): _VarHandle
  def withInvokeExactBehavior(): _VarHandle
  def isAccessModeSupported(mode: _VarHandle.AccessMode): Boolean

  // MethodHandle types and constant descriptors are not implemented on Native.
  @stub def accessModeType(mode: _VarHandle.AccessMode): MethodType = ???
  @stub def toMethodHandle(mode: _VarHandle.AccessMode): MethodHandle = ???
  @stub def describeConstable(): java.util.Optional[VarHandle.VarHandleDesc] = ???
}

object _VarHandle {
  final class AccessMode private (
      name: String,
      ordinal: Int,
      private val accessMethod: String
  ) extends java.lang._Enum[AccessMode](name, ordinal) {
    def methodName(): String = accessMethod
  }

  object AccessMode {
    final val GET: AccessMode = new AccessMode("GET", 0, "get")
    final val SET: AccessMode = new AccessMode("SET", 1, "set")
    final val GET_VOLATILE: AccessMode = new AccessMode("GET_VOLATILE", 2, "getVolatile")
    final val SET_VOLATILE: AccessMode = new AccessMode("SET_VOLATILE", 3, "setVolatile")
    final val GET_ACQUIRE: AccessMode = new AccessMode("GET_ACQUIRE", 4, "getAcquire")
    final val SET_RELEASE: AccessMode = new AccessMode("SET_RELEASE", 5, "setRelease")
    final val GET_OPAQUE: AccessMode = new AccessMode("GET_OPAQUE", 6, "getOpaque")
    final val SET_OPAQUE: AccessMode = new AccessMode("SET_OPAQUE", 7, "setOpaque")
    final val COMPARE_AND_SET: AccessMode = new AccessMode("COMPARE_AND_SET", 8, "compareAndSet")
    final val COMPARE_AND_EXCHANGE: AccessMode = new AccessMode("COMPARE_AND_EXCHANGE", 9, "compareAndExchange")
    final val COMPARE_AND_EXCHANGE_ACQUIRE: AccessMode = new AccessMode("COMPARE_AND_EXCHANGE_ACQUIRE", 10, "compareAndExchangeAcquire")
    final val COMPARE_AND_EXCHANGE_RELEASE: AccessMode = new AccessMode("COMPARE_AND_EXCHANGE_RELEASE", 11, "compareAndExchangeRelease")
    final val WEAK_COMPARE_AND_SET_PLAIN: AccessMode = new AccessMode("WEAK_COMPARE_AND_SET_PLAIN", 12, "weakCompareAndSetPlain")
    final val WEAK_COMPARE_AND_SET: AccessMode = new AccessMode("WEAK_COMPARE_AND_SET", 13, "weakCompareAndSet")
    final val WEAK_COMPARE_AND_SET_ACQUIRE: AccessMode = new AccessMode("WEAK_COMPARE_AND_SET_ACQUIRE", 14, "weakCompareAndSetAcquire")
    final val WEAK_COMPARE_AND_SET_RELEASE: AccessMode = new AccessMode("WEAK_COMPARE_AND_SET_RELEASE", 15, "weakCompareAndSetRelease")
    final val GET_AND_SET: AccessMode = new AccessMode("GET_AND_SET", 16, "getAndSet")
    final val GET_AND_SET_ACQUIRE: AccessMode = new AccessMode("GET_AND_SET_ACQUIRE", 17, "getAndSetAcquire")
    final val GET_AND_SET_RELEASE: AccessMode = new AccessMode("GET_AND_SET_RELEASE", 18, "getAndSetRelease")
    final val GET_AND_ADD: AccessMode = new AccessMode("GET_AND_ADD", 19, "getAndAdd")
    final val GET_AND_ADD_ACQUIRE: AccessMode = new AccessMode("GET_AND_ADD_ACQUIRE", 20, "getAndAddAcquire")
    final val GET_AND_ADD_RELEASE: AccessMode = new AccessMode("GET_AND_ADD_RELEASE", 21, "getAndAddRelease")
    final val GET_AND_BITWISE_OR: AccessMode = new AccessMode("GET_AND_BITWISE_OR", 22, "getAndBitwiseOr")
    final val GET_AND_BITWISE_OR_RELEASE: AccessMode = new AccessMode("GET_AND_BITWISE_OR_RELEASE", 23, "getAndBitwiseOrRelease")
    final val GET_AND_BITWISE_OR_ACQUIRE: AccessMode = new AccessMode("GET_AND_BITWISE_OR_ACQUIRE", 24, "getAndBitwiseOrAcquire")
    final val GET_AND_BITWISE_AND: AccessMode = new AccessMode("GET_AND_BITWISE_AND", 25, "getAndBitwiseAnd")
    final val GET_AND_BITWISE_AND_RELEASE: AccessMode = new AccessMode("GET_AND_BITWISE_AND_RELEASE", 26, "getAndBitwiseAndRelease")
    final val GET_AND_BITWISE_AND_ACQUIRE: AccessMode = new AccessMode("GET_AND_BITWISE_AND_ACQUIRE", 27, "getAndBitwiseAndAcquire")
    final val GET_AND_BITWISE_XOR: AccessMode = new AccessMode("GET_AND_BITWISE_XOR", 28, "getAndBitwiseXor")
    final val GET_AND_BITWISE_XOR_RELEASE: AccessMode = new AccessMode("GET_AND_BITWISE_XOR_RELEASE", 29, "getAndBitwiseXorRelease")
    final val GET_AND_BITWISE_XOR_ACQUIRE: AccessMode = new AccessMode("GET_AND_BITWISE_XOR_ACQUIRE", 30, "getAndBitwiseXorAcquire")

    private val all = Array(
      GET,
      SET,
      GET_VOLATILE,
      SET_VOLATILE,
      GET_ACQUIRE,
      SET_RELEASE,
      GET_OPAQUE,
      SET_OPAQUE,
      COMPARE_AND_SET,
      COMPARE_AND_EXCHANGE,
      COMPARE_AND_EXCHANGE_ACQUIRE,
      COMPARE_AND_EXCHANGE_RELEASE,
      WEAK_COMPARE_AND_SET_PLAIN,
      WEAK_COMPARE_AND_SET,
      WEAK_COMPARE_AND_SET_ACQUIRE,
      WEAK_COMPARE_AND_SET_RELEASE,
      GET_AND_SET,
      GET_AND_SET_ACQUIRE,
      GET_AND_SET_RELEASE,
      GET_AND_ADD,
      GET_AND_ADD_ACQUIRE,
      GET_AND_ADD_RELEASE,
      GET_AND_BITWISE_OR,
      GET_AND_BITWISE_OR_RELEASE,
      GET_AND_BITWISE_OR_ACQUIRE,
      GET_AND_BITWISE_AND,
      GET_AND_BITWISE_AND_RELEASE,
      GET_AND_BITWISE_AND_ACQUIRE,
      GET_AND_BITWISE_XOR,
      GET_AND_BITWISE_XOR_RELEASE,
      GET_AND_BITWISE_XOR_ACQUIRE
    )

    def values(): Array[AccessMode] = all.clone()

    def valueOf(name: String): AccessMode = {
      if (name == null) throw new NullPointerException
      all
        .find(_.name() == name)
        .getOrElse(throw new IllegalArgumentException(name))
    }

    def valueFromMethodName(methodName: String): AccessMode = {
      if (methodName == null) throw new NullPointerException
      all
        .find(_.methodName() == methodName)
        .getOrElse(throw new IllegalArgumentException(methodName))
    }
  }

  @alwaysinline
  private def loadFence(): Unit =
    if (isMultithreadingEnabled) atomic_thread_fence(memory_order_acquire)

  @alwaysinline
  private def storeFence(): Unit =
    if (isMultithreadingEnabled) atomic_thread_fence(memory_order_release)

  /** Ensures that loads before the fence will not be reordered with loads and stores after the fence.
   */
  @alwaysinline
  def acquireFence(): Unit = loadFence()

  /** Ensures that loads and stores before the fence will not be reordered with stores after the fence.
   */
  @alwaysinline
  def releaseFence(): Unit = storeFence()

  /** Ensures that loads and stores before the fence will not be reordered with loads and stores after the fence.
   */
  @alwaysinline
  def fullFence(): Unit =
    if (isMultithreadingEnabled) atomic_thread_fence(memory_order_seq_cst)

  /** Ensures that loads before the fence will not be reordered with loads after the fence.
   */
  @alwaysinline
  def loadLoadFence(): Unit = loadFence()

  /** Ensures that stores before the fence will not be reordered with stores after the fence.
   */
  @alwaysinline
  def storeStoreFence(): Unit = storeFence()
}
