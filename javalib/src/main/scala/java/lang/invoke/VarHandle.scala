package java.lang.invoke

import scala.scalanative.libc.stdatomic._
import scala.scalanative.libc.stdatomic.memory_order._
import scala.scalanative.annotation._
import scala.scalanative.meta.LinktimeInfo.isMultithreadingEnabled

class VarHandle {}

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
