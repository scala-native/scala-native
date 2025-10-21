/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent
import java.security.{PrivilegedAction, PrivilegedExceptionAction}
import java.util.concurrent.atomic.AtomicInteger
import java.util.{Collection, List}

import scala.scalanative.annotation.{alwaysinline, safePublish}

object Executors {
  def newWorkStealingPool(parallelism: Int): ExecutorService =
    new ForkJoinPool(
      parallelism,
      ForkJoinPool.defaultForkJoinWorkerThreadFactory,
      null,
      true
    )

  def newWorkStealingPool(): ExecutorService =
    newWorkStealingPool(Runtime.getRuntime().availableProcessors())

  def newFixedThreadPool(
      nThreads: Int,
      threadFactory: ThreadFactory
  ): ExecutorService = {
    new ThreadPoolExecutor(
      nThreads,
      nThreads,
      0L,
      TimeUnit.MILLISECONDS,
      new LinkedBlockingQueue[Runnable],
      threadFactory
    )
  }

  def newFixedThreadPool(nThreads: Int): ExecutorService =
    new ThreadPoolExecutor(
      nThreads,
      nThreads,
      0L,
      TimeUnit.MILLISECONDS,
      new LinkedBlockingQueue[Runnable]
    )

  def newSingleThreadExecutor(): ExecutorService = {
    new Executors.FinalizableDelegatedExecutorService(
      newFixedThreadPool(1)
    )
  }

  def newSingleThreadExecutor(threadFactory: ThreadFactory): ExecutorService = {
    new Executors.FinalizableDelegatedExecutorService(
      newFixedThreadPool(1, threadFactory)
    )
  }

  def newCachedThreadPool(): ExecutorService = {
    new ThreadPoolExecutor(
      0,
      Integer.MAX_VALUE,
      60L,
      TimeUnit.SECONDS,
      new SynchronousQueue[Runnable]
    )
  }

  def newCachedThreadPool(threadFactory: ThreadFactory): ExecutorService = {
    new ThreadPoolExecutor(
      0,
      Integer.MAX_VALUE,
      60L,
      TimeUnit.SECONDS,
      new SynchronousQueue[Runnable],
      threadFactory
    )
  }

  def newSingleThreadScheduledExecutor(): ScheduledExecutorService = {
    new Executors.DelegatedScheduledExecutorService(
      new ScheduledThreadPoolExecutor(1)
    )
  }

  def newSingleThreadScheduledExecutor(
      threadFactory: ThreadFactory
  ): ScheduledExecutorService = {
    new Executors.DelegatedScheduledExecutorService(
      new ScheduledThreadPoolExecutor(1, threadFactory)
    )
  }

  def newScheduledThreadPool(corePoolSize: Int): ScheduledExecutorService = {
    new ScheduledThreadPoolExecutor(corePoolSize)
  }

  def newScheduledThreadPool(
      corePoolSize: Int,
      threadFactory: ThreadFactory
  ): ScheduledExecutorService = {
    new ScheduledThreadPoolExecutor(corePoolSize, threadFactory)
  }

  def unconfigurableExecutorService(
      executor: ExecutorService
  ): ExecutorService = {
    if (executor == null) throw new NullPointerException
    new Executors.DelegatedExecutorService(executor)
  }

  def unconfigurableScheduledExecutorService(
      executor: ScheduledExecutorService
  ): ScheduledExecutorService = {
    if (executor == null) throw new NullPointerException
    new Executors.DelegatedScheduledExecutorService(executor)
  }

  def defaultThreadFactory(): ThreadFactory = {
    return new Executors.DefaultThreadFactory
  }

  def privilegedThreadFactory(): ThreadFactory = {
    new Executors.PrivilegedThreadFactory
  }

  def callable[T](task: Runnable, result: T): Callable[T] = {
    if (task == null) throw new NullPointerException
    new Executors.RunnableAdapter[T](task, result)
  }

  def callable(task: Runnable): Callable[AnyRef] = {
    if (task == null) throw new NullPointerException
    new Executors.RunnableAdapter[AnyRef](task, null)
  }

  def callable(action: PrivilegedAction[_]): Callable[Any] = {
    if (action == null) throw new NullPointerException
    new Callable[Any]() {
      override def call(): Any = { action.run() }
    }
  }

  def callable(action: PrivilegedExceptionAction[_]): Callable[Any] = {
    if (action == null) { throw new NullPointerException }
    new Callable[Any]() {
      @throws[Exception]
      override def call(): Any = { action.run() }
    }
  }

  def privilegedCallable[T](callable: Callable[T]): Callable[T] = {
    if (callable == null) { throw new NullPointerException }
    new Executors.PrivilegedCallable[T](callable)
  }

  def privilegedCallableUsingCurrentClassLoader[T](
      callable: Callable[T]
  ): Callable[T] = {
    if (callable == null) { throw new NullPointerException }
    new Executors.PrivilegedCallableUsingCurrentClassLoader[T](callable)
  }

  private final class RunnableAdapter[T](
      @safePublish val task: Runnable,
      @safePublish val result: T
  ) extends Callable[T] {
    override def call(): T = {
      task.run()
      result
    }
    override def toString(): String = {
      super.toString + "[Wrapped task = " + task + "]"
    }
  }

  private final class PrivilegedCallable[T](
      @safePublish val task: Callable[T]
  ) extends Callable[T] {
    @throws[Exception]
    override def call(): T = task.call()

    override def toString(): String = {
      super.toString + "[Wrapped task = " + task + "]"
    }
  }

  private final class PrivilegedCallableUsingCurrentClassLoader[T](
      @safePublish val task: Callable[T]
  ) extends Callable[T] {

    @throws[Exception]
    override def call(): T = task.call()

    override def toString(): String = {
      return super.toString + "[Wrapped task = " + task + "]"
    }
  }

  private object DefaultThreadFactory {
    private val poolNumber: AtomicInteger = new AtomicInteger(1)
  }
  private class DefaultThreadFactory() extends ThreadFactory {
    // Originally SecurityManager threadGroup was tried first
    private final val group: ThreadGroup =
      Thread.currentThread().getThreadGroup()

    private final val threadNumber: AtomicInteger = new AtomicInteger(1)
    private final var namePrefix: String =
      "pool-" + DefaultThreadFactory.poolNumber.getAndIncrement() + "-thread-"

    override def newThread(r: Runnable): Thread = {
      val t: Thread =
        new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0)
      if (t.isDaemon()) { t.setDaemon(false) }
      if (t.getPriority() != Thread.NORM_PRIORITY) {
        t.setPriority(Thread.NORM_PRIORITY)
      }
      return t
    }
  }

  private class PrivilegedThreadFactory()
      extends Executors.DefaultThreadFactory {
    override def newThread(r: Runnable): Thread =
      super.newThread(new Runnable() {
        override def run(): Unit = r.run()
      })
  }

  private class DelegatedExecutorService(
      @safePublish val executor: ExecutorService
  ) extends ExecutorService {

    // Stub used in place of JVM intrinsic
    @alwaysinline
    private def reachabilityFence(target: Any): Unit = ()

    override def execute(command: Runnable): Unit = {
      try executor.execute(command)
      finally {
        reachabilityFence(this)
      }
    }
    override def shutdown(): Unit = { executor.shutdown() }
    override def shutdownNow(): List[Runnable] = {
      try return executor.shutdownNow()
      finally {
        reachabilityFence(this)
      }
    }
    override def isShutdown(): Boolean = {
      try return executor.isShutdown()
      finally {
        reachabilityFence(this)
      }
    }
    override def isTerminated(): Boolean = {
      try return executor.isTerminated()
      finally {
        reachabilityFence(this)
      }
    }
    @throws[InterruptedException]
    override def awaitTermination(timeout: Long, unit: TimeUnit): Boolean = {
      try return executor.awaitTermination(timeout, unit)
      finally {
        reachabilityFence(this)
      }
    }
    override def submit(task: Runnable): Future[_] = {
      try return executor.submit(task)
      finally {
        reachabilityFence(this)
      }
    }
    override def submit[T <: AnyRef](task: Callable[T]): Future[T] = {
      try return executor.submit(task)
      finally {
        reachabilityFence(this)
      }
    }
    override def submit[T <: AnyRef](task: Runnable, result: T): Future[T] = {
      try return executor.submit(task, result)
      finally {
        reachabilityFence(this)
      }
    }
    @throws[InterruptedException]
    override def invokeAll[T <: AnyRef](
        tasks: Collection[_ <: Callable[T]]
    ): List[Future[T]] = {
      try return executor.invokeAll(tasks)
      finally {
        reachabilityFence(this)
      }
    }
    @throws[InterruptedException]
    override def invokeAll[T <: AnyRef](
        tasks: Collection[_ <: Callable[T]],
        timeout: Long,
        unit: TimeUnit
    ): List[Future[T]] = {
      try return executor.invokeAll(tasks, timeout, unit)
      finally {
        reachabilityFence(this)
      }
    }
    @throws[InterruptedException]
    @throws[ExecutionException]
    override def invokeAny[T <: AnyRef](
        tasks: Collection[_ <: Callable[T]]
    ): T = {
      try return executor.invokeAny(tasks)
      finally {
        reachabilityFence(this)
      }
    }
    @throws[InterruptedException]
    @throws[ExecutionException]
    @throws[TimeoutException]
    override def invokeAny[T <: AnyRef](
        tasks: Collection[_ <: Callable[T]],
        timeout: Long,
        unit: TimeUnit
    ): T = {
      try return executor.invokeAny(tasks, timeout, unit)
      finally {
        reachabilityFence(this)
      }
    }
  }
  private class FinalizableDelegatedExecutorService(
      executor: ExecutorService
  ) extends Executors.DelegatedExecutorService(executor) {
    override protected def finalize(): Unit = { super.shutdown() }
  }

  private class DelegatedScheduledExecutorService(
      e: ScheduledExecutorService
  ) extends Executors.DelegatedExecutorService(e)
      with ScheduledExecutorService {
    override def schedule(
        command: Runnable,
        delay: Long,
        unit: TimeUnit
    ): ScheduledFuture[AnyRef] = {
      return e.schedule(command, delay, unit)
    }
    override def schedule[V <: AnyRef](
        callable: Callable[V],
        delay: Long,
        unit: TimeUnit
    ): ScheduledFuture[V] = {
      e.schedule(callable, delay, unit)
    }
    override def scheduleAtFixedRate(
        command: Runnable,
        initialDelay: Long,
        period: Long,
        unit: TimeUnit
    ): ScheduledFuture[AnyRef] = {
      e.scheduleAtFixedRate(command, initialDelay, period, unit)
    }
    override def scheduleWithFixedDelay(
        command: Runnable,
        initialDelay: Long,
        delay: Long,
        unit: TimeUnit
    ): ScheduledFuture[AnyRef] = {
      e.scheduleWithFixedDelay(command, initialDelay, delay, unit)
    }
  }
}

// Cannot instantiate.
class Executors private ()
