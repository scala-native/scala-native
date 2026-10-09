package java.lang.invoke

import scala.scalanative.annotation._
import scala.scalanative.libc.stdatomic._
import scala.scalanative.libc.stdatomic.memory_order._
import scala.scalanative.meta.LinktimeInfo.isMultithreadingEnabled

class VarHandle {
  /*
   * VarHandle methods are signature-polymorphic on the JVM.  Keeping their
   * descriptor as Object[] is important: Scala compilers recognise the JDK
   * name and descriptor and use the type expected at the call site.  Native
   * compilation rewrites supported field handles before code generation.
   */
  @stub() final def get(args: Array[AnyRef]): AnyRef = ???
  @stub() final def set(args: Array[AnyRef]): Unit = ???
  @stub() final def getVolatile(args: Array[AnyRef]): AnyRef = ???
  @stub() final def setVolatile(args: Array[AnyRef]): Unit = ???
  @stub() final def getOpaque(args: Array[AnyRef]): AnyRef = ???
  @stub() final def setOpaque(args: Array[AnyRef]): Unit = ???
  @stub() final def getAcquire(args: Array[AnyRef]): AnyRef = ???
  @stub() final def setRelease(args: Array[AnyRef]): Unit = ???

  @stub() final def compareAndSet(args: Array[AnyRef]): Boolean = ???
  @stub() final def compareAndExchange(args: Array[AnyRef]): AnyRef = ???
  @stub() final def compareAndExchangeAcquire(args: Array[AnyRef]): AnyRef = ???
  @stub() final def compareAndExchangeRelease(args: Array[AnyRef]): AnyRef = ???
  @stub() final def weakCompareAndSetPlain(args: Array[AnyRef]): Boolean = ???
  @stub() final def weakCompareAndSet(args: Array[AnyRef]): Boolean = ???
  @stub() final def weakCompareAndSetAcquire(args: Array[AnyRef]): Boolean = ???
  @stub() final def weakCompareAndSetRelease(args: Array[AnyRef]): Boolean = ???
  @stub() final def getAndSet(args: Array[AnyRef]): AnyRef = ???
  @stub() final def getAndSetAcquire(args: Array[AnyRef]): AnyRef = ???
  @stub() final def getAndSetRelease(args: Array[AnyRef]): AnyRef = ???
  @stub() final def getAndAdd(args: Array[AnyRef]): AnyRef = ???
  @stub() final def getAndAddAcquire(args: Array[AnyRef]): AnyRef = ???
  @stub() final def getAndAddRelease(args: Array[AnyRef]): AnyRef = ???
  @stub() final def getAndBitwiseOr(args: Array[AnyRef]): AnyRef = ???
  @stub() final def getAndBitwiseOrAcquire(args: Array[AnyRef]): AnyRef = ???
  @stub() final def getAndBitwiseOrRelease(args: Array[AnyRef]): AnyRef = ???
  @stub() final def getAndBitwiseAnd(args: Array[AnyRef]): AnyRef = ???
  @stub() final def getAndBitwiseAndAcquire(args: Array[AnyRef]): AnyRef = ???
  @stub() final def getAndBitwiseAndRelease(args: Array[AnyRef]): AnyRef = ???
  @stub() final def getAndBitwiseXor(args: Array[AnyRef]): AnyRef = ???
  @stub() final def getAndBitwiseXorAcquire(args: Array[AnyRef]): AnyRef = ???
  @stub() final def getAndBitwiseXorRelease(args: Array[AnyRef]): AnyRef = ???
}

object VarHandle {
  @alwaysinline
  private def loadFence(): Unit =
    if (isMultithreadingEnabled) atomic_thread_fence(memory_order_acquire)

  @alwaysinline
  private def storeFence(): Unit =
    if (isMultithreadingEnabled) atomic_thread_fence(memory_order_release)

  /** Ensures that loads before the fence will not be reordered with loads and
   *  stores after the fence.
   */
  @alwaysinline
  def acquireFence(): Unit = loadFence()

  /** Ensures that loads and stores before the fence will not be reordered with
   *  stores after the fence.
   */
  @alwaysinline
  def releaseFence(): Unit = storeFence()

  /** Ensures that loads and stores before the fence will not be reordered with
   *  loads and stores after the fence.
   */
  @alwaysinline
  def fullFence(): Unit =
    if (isMultithreadingEnabled) atomic_thread_fence(memory_order_seq_cst)

  /** Ensures that loads before the fence will not be reordered with loads after
   *  the fence.
   */
  @alwaysinline
  def loadLoadFence(): Unit = loadFence()

  /** Ensures that stores before the fence will not be reordered with stores
   *  after the fence.
   */
  @alwaysinline
  def storeStoreFence(): Unit = storeFence()
}
