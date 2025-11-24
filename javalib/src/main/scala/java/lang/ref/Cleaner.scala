package java.lang.ref

import java.util.concurrent._

import scala.scalanative.meta.LinktimeInfo

final class Cleaner private (executor: Executor) {

  /** Registers a destructor after `ref` has been garbage collected.
   *
   *  NB: the destructor may not refer to `ref`, or GC will never happen.
   */
  def register(ref: AnyRef, action: Runnable): Cleaner.Cleanable =
    Cleaner.register(ref, action, executor)

}

object Cleaner {

  private val defaultFactory =
    if (LinktimeInfo.isMultithreadingEnabled && LinktimeInfo.isWeakReferenceSupported)
      Thread
        .ofPlatform()
        .daemon()
        .group(ThreadGroup.System)
        .name("GC-WeakReferenceCleaner", 0)
        .factory()
    else null

  private val parasiticExecutor = new Executor {
    override def execute(action: Runnable): Unit =
      try action.run()
      catch {
        case ex: Throwable if !ex.isInstanceOf[InterruptedException] =>
      }
  }

  trait Cleanable {
    def clean(): Unit
  }

  def create(factory: ThreadFactory): Cleaner = {
    if (!LinktimeInfo.isWeakReferenceSupported)
      throw new UnsupportedOperationException(
        "Can't create Cleaner, not supported by this GC"
      )

    val executor: Executor =
      if (factory eq null) parasiticExecutor
      else {
        val executor = Executors.newSingleThreadExecutor(factory)
        // clean the executor as well
        register(executor, () => executor.shutdown(), parasiticExecutor)
        executor
      }

    new Cleaner(executor)
  }

  def create(): Cleaner = create(defaultFactory)

  private def register(ref: AnyRef, action: Runnable, executor: Executor) = {
    val cleanable = new CleanableImpl(action, executor)
    // registers itself in WeakReferenceRegistry
    new CleanableReference(ref, cleanable)
    cleanable
  }

  private class CleanableReference(ref: AnyRef, cleanable: Cleanable)
      extends WeakReference[AnyRef](ref) {
    override def clear(): Unit = {
      super.clear()
      cleanable.clean()
    }
  }

  private class CleanableImpl(action: Runnable, executor: Executor)
      extends Cleanable {
    private val done = new atomic.AtomicBoolean(false)

    override def clean(): Unit = {
      if (done.compareAndSet(expectedValue = false, newValue = true))
        executor.execute(action)
    }
  }

}
