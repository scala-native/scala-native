package java.lang.ref

import java.util.concurrent._

import scala.scalanative.meta.LinktimeInfo

final class Cleaner private {

  /** Registers a destructor after `ref` has been garbage collected.
   *
   *  NB: the destructor may not refer to `ref`, or GC will never happen.
   */
  def register(ref: AnyRef, action: Runnable): Cleaner.Cleanable = {
    val cleanable = new Cleaner.CleanableImpl(action)
    // registers itself in WeakReferenceRegistry
    new Cleaner.CleanableReference(ref, cleanable)
    cleanable
  }

}

object Cleaner {

  trait Cleanable {
    def clean(): Unit
  }

  def create(): Cleaner = {
    if (!LinktimeInfo.isWeakReferenceSupported)
      throw new UnsupportedOperationException(
        "Can't create Cleaner, not supported by this GC"
      )
    new Cleaner
  }

  def create(factory: ThreadFactory): Cleaner = {
    if (null ne factory)
      throw new UnsupportedOperationException(
        "Can't create Cleaner with a ThreadFactory"
      )
    create()
  }

  private class CleanableReference(ref: AnyRef, cleanable: Cleanable)
      extends WeakReference[AnyRef](ref) {
    override def clear(): Unit = {
      super.clear()
      cleanable.clean()
    }
  }

  private class CleanableImpl(action: Runnable) extends Cleanable {
    private val done = new atomic.AtomicBoolean(false)

    override def clean(): Unit = {
      if (done.compareAndSet(expectedValue = false, newValue = true))
        if (LinktimeInfo.isMultithreadingEnabled)
          CompletableFuture.runAsync(action)
        else
          try action.run()
          catch {
            case ex: Throwable if !ex.isInstanceOf[InterruptedException] =>
          }
    }
  }

}
