// scalafmt: { maxColumn = 120 }

package java.lang

import java.util.concurrent.*
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory

import scala.scalanative.runtime.VirtualThreadScheduler

/** Default VT scheduler: {@link ForkJoinPool} carriers plus fan-out {@link ScheduledThreadPoolExecutor} for delayed
 *  tasks.
 */
private[java] object DefaultVirtualThreadScheduler extends VirtualThreadScheduler {
  private val pool: ForkJoinPool = {
    val factory: ForkJoinWorkerThreadFactory = new ForkJoinWorkerThreadFactory {
      override def newThread(p: ForkJoinPool): ForkJoinWorkerThread = new VirtualThreadCarrier(p)
    }
    val handler: Thread.UncaughtExceptionHandler = (_, _) => ()
    val paralellism = Runtime.getRuntime().availableProcessors()
    val maxPoolSize = paralellism max 256
    val minRunnable = (paralellism / 2).max(1)
    new ForkJoinPool(
      paralellism,
      factory,
      handler,
      true,
      0,
      maxPoolSize,
      minRunnable,
      _ => true,
      30,
      TimeUnit.SECONDS
    )
  }

  private val delayedSchedulers: Array[ScheduledThreadPoolExecutor] = {
    val paralellism = Runtime.getRuntime().availableProcessors()
    val queueCount = Integer.highestOneBit(paralellism * 4).max(1)
    Array.fill(queueCount) {
      val executor: ScheduledThreadPoolExecutor =
        new ScheduledThreadPoolExecutor(
          1,
          task => {
            val t = new Thread(task)
            t.setName("VirtualThread-unparker")
            t.setDaemon(true)
            t
          }
        )
      executor.setRemoveOnCancelPolicy(true)
      executor
    }
  }

  override def execute(task: Runnable): Unit = pool.execute(task)

  override def lazyExecute(task: Runnable): Unit =
    pool.lazySubmit(ForkJoinTask.adapt(task))

  override def schedule(
      task: Runnable,
      delay: scala.Long,
      unit: TimeUnit
  ): VirtualThreadScheduler.Cancellable = {
    val tid = Thread.currentThread().threadId()
    val idx = (tid.toInt & (delayedSchedulers.length - 1))
    val future = delayedSchedulers(idx).schedule(task, delay, unit)
    new VirtualThreadScheduler.Cancellable {
      override def cancel(): scala.Boolean = future.cancel(false)
      override def isDone(): scala.Boolean = future.isDone()
    }
  }

  override def isCarrierThread(thread: Thread): scala.Boolean =
    thread.isInstanceOf[VirtualThreadCarrier] &&
      thread.asInstanceOf[VirtualThreadCarrier].getPool().eq(pool)

  override def isCarrierIdle(thread: Thread): scala.Boolean =
    thread match {
      case c: VirtualThreadCarrier => c.getQueuedTaskCount() == 0
      case _                       => false
    }

  override def beginCarrierCompensatedBlock(carrier: Thread): scala.Boolean =
    carrier match {
      case c: VirtualThreadCarrier if c.getPool().eq(pool) => c.beginBlocking()
      case _                                               => false
    }

  override def endCarrierCompensatedBlock(
      carrier: Thread,
      attempted: scala.Boolean
  ): Unit =
    if (attempted) {
      carrier match {
        case c: VirtualThreadCarrier if c.getPool().eq(pool) => c.endBlocking()
        case _                                               => ()
      }
    }

  override def afterYieldOnCarrier(carrier: Thread): Unit =
    carrier match {
      case c: VirtualThreadCarrier if c.getPool().eq(pool) => c.endBlocking()
      case _                                               => ()
    }
}

class VirtualThreadCarrier(scheduler: ForkJoinPool)
    extends ForkJoinWorkerThread(Thread.VirtualThreadCarriersGroup, scheduler, true) {

  import VirtualThreadCarrier.*

  var mountedThread: VirtualThread = _

  private var compensation: CompensationState = CompensationState.NotCompensating
  private var compensationValue: scala.Long = 0L

  def beginBlocking(): scala.Boolean =
    compensation match {
      case CompensationState.NotCompensating =>
        try {
          compensation = CompensationState.CompensateInProgress
          compensationValue = getPool().beginCompensatedBlock()
          compensation = CompensationState.Compensating
          true
        } catch {
          case ex: Throwable =>
            compensation = CompensationState.NotCompensating
            compensationValue = 0L
            throw ex
        }
      case _ => false
    }

  def endBlocking(): Unit =
    compensation match {
      case CompensationState.Compensating =>
        getPool().endCompensatedBlock(compensationValue)
        compensation = CompensationState.NotCompensating
        compensationValue = 0L
      case _ =>
        ()
    }
}

object VirtualThreadCarrier {
  opaque type CompensationState = Int
  object CompensationState {
    final val NotCompensating: CompensationState = 0
    final val CompensateInProgress: CompensationState = 1
    final val Compensating: CompensationState = 2
  }
}
