/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent

import java.util.concurrent.TimeUnit._
import java.{lang, util}

import scala.annotation.tailrec

abstract class AbstractExecutorService() extends ExecutorService {

  protected[concurrent] def newTaskFor[T <: AnyRef](
      runnable: Runnable,
      value: T
  ): RunnableFuture[T] =
    new FutureTask[T](runnable, value)

  protected[concurrent] def newTaskFor[T <: AnyRef](
      callable: Callable[T]
  ): RunnableFuture[T] =
    new FutureTask[T](callable)

  @throws[NullPointerException]
  @throws[java.lang.RejectedExecutionException]
  override def submit(task: Runnable): Future[_] = {
    if (task == null) throw new NullPointerException()
    val ftask: RunnableFuture[Object] = newTaskFor(task, null)
    execute(ftask)
    ftask
  }

  @throws[NullPointerException]
  @throws[java.lang.RejectedExecutionException]
  override def submit[T <: AnyRef](task: Runnable, result: T): Future[T] = {
    if (task == null) throw new NullPointerException()
    val ftask: RunnableFuture[T] = newTaskFor(task, result)
    execute(ftask)
    ftask
  }

  @throws[NullPointerException]
  @throws[java.lang.RejectedExecutionException]
  override def submit[T <: AnyRef](task: Callable[T]): Future[T] = {
    if (task == null) throw new NullPointerException()
    val ftask: RunnableFuture[T] = newTaskFor(task)
    execute(ftask)
    ftask
  }

  @throws[InterruptedException]
  @throws[TimeoutException]
  @throws[ExecutionException]
  private def doInvokeAny[T <: AnyRef](
      tasks: util.Collection[_ <: Callable[T]],
      timed: Boolean,
      n: Long
  ): T = {
    var nanos: Long = n
    if (tasks == null)
      throw new NullPointerException()

    var ntasks: Int = tasks.size()
    if (ntasks == 0)
      throw new IllegalArgumentException()

    val futures = new util.ArrayList[Future[T]](ntasks)
    val ecs = new ExecutorCompletionService[T](this)

    // For efficiency, especially in executors with limited
    // parallelism, check to see if previously submitted tasks are
    // done before submitting more of them. This interleaving
    // plus the exception mechanics account for messiness of main
    // loop.

    try {
      // Record exceptions so that if we fail to obtain any
      // result, we can throw the last exception we got.
      var ee: ExecutionException = null
      val deadline = if (timed) System.nanoTime() + nanos else 0L
      val it = tasks.iterator()

      // Start one task for sure; the rest incrementally
      futures.add(ecs.submit(it.next()))
      ntasks -= 1
      var active: Int = 1

      var break: Boolean = false
      while (!break) {
        val f: Future[T] = ecs.poll() match {
          case null =>
            if (ntasks > 0) {
              ntasks -= 1
              futures.add(ecs.submit(it.next()))
              active += 1
              null
            } else if (active == 0) {
              break = true
              null
            } else if (timed)
              ecs.poll(nanos, TimeUnit.NANOSECONDS) match {
                case null => throw new TimeoutException()
                case f    =>
                  nanos = deadline - System.nanoTime()
                  f
              }
            else ecs.take()

          case f => f
        }
        if (!break && f != null) {
          active -= 1
          try {
            return f.get()
          } catch {
            case eex: ExecutionException => ee = eex
            case rex: RuntimeException   => ee = new ExecutionException(rex)
          }
        }
      }
      if (ee == null) ee = new ExecutionException(null: Throwable)
      throw ee
    } finally cancelAll(futures)
  }

  @throws[InterruptedException]
  @throws[ExecutionException]
  override def invokeAny[T <: AnyRef](
      tasks: util.Collection[_ <: Callable[T]]
  ): T =
    doInvokeAny(tasks, false, 0)

  @throws[InterruptedException]
  @throws[ExecutionException]
  @throws[TimeoutException]
  override def invokeAny[T <: AnyRef](
      tasks: java.util.Collection[_ <: Callable[T]],
      timeout: Long,
      unit: TimeUnit
  ): T = doInvokeAny(tasks, true, unit.toNanos(timeout))

  @throws[InterruptedException]
  override def invokeAll[T <: AnyRef](
      tasks: java.util.Collection[_ <: Callable[T]]
  ): java.util.List[Future[T]] = {
    if (tasks == null) throw new NullPointerException()
    val futures: util.List[Future[T]] =
      new util.ArrayList[Future[T]](tasks.size())
    var done: Boolean = false
    try {
      val it = tasks.iterator()
      while (it.hasNext()) {
        val f: RunnableFuture[T] = newTaskFor(it.next())
        futures.add(f)
        execute(f)
      }

      val it1 = futures.iterator()
      while (it1.hasNext()) {
        val f = it1.next()
        if (!f.isDone()) {
          try {
            f.get()
          } catch {
            case ignore: CancellationException =>
            case ignore: ExecutionException    =>
          }
        }
      }
      done = true
      futures
    } finally if (!done) cancelAll(futures)
  }

  @throws[InterruptedException]
  override def invokeAll[T <: AnyRef](
      tasks: util.Collection[_ <: Callable[T]],
      timeout: Long,
      unit: TimeUnit
  ): util.List[Future[T]] = {
    if (tasks == null || unit == null)
      throw new NullPointerException()
    val nanos: Long = unit.toNanos(timeout)
    val deadline = System.nanoTime() + nanos
    val futures = new util.ArrayList[Future[T]](tasks.size())
    var lastIdx = 0

    try {
      val it = tasks.iterator()
      while (it.hasNext()) {
        futures.add(newTaskFor(it.next()))
      }
      val size = futures.size()

      // Interleave time checks and calls to execute in case
      // executor doesn't have any/much parallelism.
      @tailrec def executeLoop(i: Int): Boolean =
        if (i >= size) false
        else {
          val remainingTime =
            if (i == 0) nanos
            else deadline - System.nanoTime()
          if (remainingTime <= 0) true // timeout
          else {
            execute(futures.get(i).asInstanceOf[Runnable])
            executeLoop(i + 1)
          }
        }

      @tailrec def awaitLoop(i: Int): Boolean =
        if (i >= size) false
        else {
          val f = futures.get(i)
          val timedOut =
            if (f.isDone()) false
            else
              try {
                f.get(deadline - System.nanoTime(), NANOSECONDS)
                false
              } catch {
                case _: CancellationException | _: ExecutionException => false
                case _: TimeoutException                              =>
                  lastIdx = i
                  true
              }
          if (timedOut) timedOut
          else awaitLoop(i + 1)
        }

      val timedOut = executeLoop(0) || awaitLoop(0)
      if (timedOut) cancelAll(futures, lastIdx)
    } catch {
      case t: Throwable =>
        cancelAll(futures)
        throw t
    }
    futures
  }

  private def cancelAll[T](futures: util.List[Future[T]], from: Int = 0) =
    for (i <- from until futures.size()) {
      futures.get(i).cancel(true)
    }

}
