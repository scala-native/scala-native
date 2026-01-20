/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package org.scalanative.testsuite.javalib.util.concurrent

import java.util._
import java.util.concurrent._

import org.junit.{Ignore, Test}

/** One publisher, many subscribers */
object SubmissionPublisherLoops1Test {
  val ITEMS: Int = 1 << 20
  val CONSUMERS = 64
  val CAP: Int = Flow.defaultBufferSize()
  val REPS = 9
  val phaser = new Phaser(CONSUMERS + 1)

  final class Sub extends Flow.Subscriber[Boolean] {
    var sn: Flow.Subscription = null
    var count = 0

    override def onSubscribe(s: Flow.Subscription): Unit = {
      sn = s
      s.request(CAP)
    }

    override def onNext(t: Boolean): Unit = {
      if (({
            count += 1
            count
          } & (CAP - 1)) == (CAP >>> 1))
        sn.request(CAP)
    }

    override def onError(t: Throwable): Unit = {
      t.printStackTrace()
    }

    override def onComplete(): Unit = {
      if (count != ITEMS)
        System.out.println("Error: remaining " + (ITEMS - count))
      phaser.arrive()
    }
  }

  val NPS: Long = 1000L * 1000 * 1000

  @throws[Exception]
  @Test def main(args: Array[String]): Unit = {
    var reps = REPS
    if (args.length > 0) reps = args(0).toInt
    System.out.println(
      "ITEMS: " + ITEMS + " CONSUMERS: " + CONSUMERS + " CAP: " + CAP
    )
    val exec = ForkJoinPool.commonPool()
    for (rep <- 0 until reps) {
      oneRun(exec)
      System.out.println(exec)
      Thread.sleep(1000)
    }
    if (exec ne ForkJoinPool.commonPool()) exec.shutdown()
  }

  @throws[Exception]
  def oneRun(exec: ExecutorService): Unit = {
    val startTime = System.nanoTime()
    val pub = new SubmissionPublisher[Boolean](exec, CAP)
    for (i <- 0 until CONSUMERS) {
      pub.subscribe(new Sub())
    }
    for (i <- 0 until ITEMS) {
      pub.submit(true)
    }
    pub.close()
    phaser.arriveAndAwaitAdvance()
    val elapsed = System.nanoTime() - startTime
    val secs = elapsed.toDouble / NPS
    System.out.printf("\tTime: %7.3f\n", secs)
  }
}

/** One FJ publisher, many subscribers */
object SubmissionPublisherLoops2Test {
  val ITEMS: Int = 1 << 20
  val CONSUMERS = 64
  val CAP: Int = Flow.defaultBufferSize()
  val REPS = 9
  val phaser = new Phaser(CONSUMERS + 1)

  final class Sub extends Flow.Subscriber[Boolean] {
    var sn: Flow.Subscription = null
    var count = 0

    def onSubscribe(s: Flow.Subscription): Unit = {
      sn = s
      s.request(CAP)
    }

    def onNext(t: Boolean): Unit = {
      if (({
            count += 1; count
          } & (CAP - 1)) == (CAP >>> 1)) sn.request(CAP)
    }

    def onError(t: Throwable): Unit = {
      t.printStackTrace()
    }

    def onComplete(): Unit = {
      if (count != ITEMS)
        System.out.println("Error: remaining " + (ITEMS - count))
      phaser.arrive()
    }
  }

  final class Pub extends RecursiveAction {
    final val pub =
      new SubmissionPublisher[Boolean](ForkJoinPool.commonPool(), CAP)

    def compute(): Unit = {
      val p = pub
      for (i <- 0 until CONSUMERS) {
        p.subscribe(new Sub())
      }
      for (i <- 0 until ITEMS) {
        p.submit(true)
      }
      p.close()
    }
  }

  val NPS: Long = 1000L * 1000 * 1000

  @throws[Exception]
  @Test def main(args: Array[String]): Unit = {
    var reps = REPS
    if (args.length > 0) reps = args(0).toInt
    System.out.println(
      "ITEMS: " + ITEMS + " CONSUMERS: " + CONSUMERS + " CAP: " + CAP
    )
    for (rep <- 0 until reps) {
      oneRun()
      Thread.sleep(1000)
    }
  }

  @throws[Exception]
  def oneRun(): Unit = {
    val startTime = System.nanoTime()
    new Pub().fork()
    phaser.arriveAndAwaitAdvance()
    val elapsed = System.nanoTime() - startTime
    val secs = elapsed.toDouble / NPS
    System.out.printf("\tTime: %7.3f\n", secs)
  }
}

/** Creates PRODUCERS publishers each with CONSUMERS subscribers, each sent
 *  ITEMS items, with CAP buffering; repeats REPS times
 */
object SubmissionPublisherLoops3Test {
  val ITEMS: Int = 1 << 20
  val PRODUCERS = 32
  val CONSUMERS = 32
  val CAP: Int = Flow.defaultBufferSize()
  val REPS = 9
  val phaser = new Phaser(PRODUCERS * CONSUMERS + 1)

  @throws[Exception]
  def main(args: Array[String]): Unit = {
    var reps = REPS
    if (args.length > 0) reps = args(0).toInt
    System.out.println(
      "ITEMS: " + ITEMS + " PRODUCERS: " + PRODUCERS + " CONSUMERS: " + CONSUMERS + " CAP: " + CAP
    )
    for (rep <- 0 until reps) {
      oneRun()
      Thread.sleep(1000)
    }
  }

  @throws[Exception]
  def oneRun(): Unit = {
    val nitems = ITEMS.toLong * PRODUCERS * CONSUMERS
    val startTime = System.nanoTime()
    for (i <- 0 until PRODUCERS) {
      new Pub().fork()
    }
    phaser.arriveAndAwaitAdvance()
    val elapsed = System.nanoTime() - startTime
    val secs = elapsed.toDouble / (1000L * 1000 * 1000)
    val ips = nitems / secs
    System.out.printf("Time: %7.2f", secs)
    System.out.printf(" items per sec: %14.2f\n", ips)
    System.out.println(ForkJoinPool.commonPool())
  }

  final class Sub extends Flow.Subscriber[Boolean] {
    var count = 0
    var subscription: Flow.Subscription = null

    def onSubscribe(s: Flow.Subscription): Unit = {
      subscription = s
      s.request(CAP)
    }

    def onNext(b: Boolean): Unit = {
      if (b && ({
            count += 1; count
          } & ((CAP >>> 1) - 1)) == 0) subscription.request(CAP >>> 1)
    }

    def onComplete(): Unit = {
      if (count != ITEMS)
        System.out.println("Error: remaining " + (ITEMS - count))
      phaser.arrive()
    }

    def onError(t: Throwable): Unit = {
      t.printStackTrace()
    }
  }

  final class Pub extends RecursiveAction {
    final val pub =
      new SubmissionPublisher[Boolean](ForkJoinPool.commonPool(), CAP)

    def compute(): Unit = {
      val p = pub
      for (i <- 0 until CONSUMERS) {
        p.subscribe(new Sub())
      }
      for (i <- 0 until ITEMS) {
        p.submit(true)
      }
      p.close()
    }
  }
}

/** Creates PRODUCERS publishers each with PROCESSORS processors each with
 *  CONSUMERS subscribers, each sent ITEMS items, with max CAP buffering;
 *  repeats REPS times
 */
object SubmissionPublisherLoops4Test {
  val ITEMS: Int = 1 << 20
  val PRODUCERS = 32
  val PROCESSORS = 32
  val CONSUMERS = 32
  val CAP: Int = Flow.defaultBufferSize()
  val REPS = 9
  val SINKS: Int = PRODUCERS * PROCESSORS * CONSUMERS
  val NEXTS: Long = ITEMS.toLong * SINKS
  val phaser = new Phaser(SINKS + 1)

  @throws[Exception]
  def main(args: Array[String]): Unit = {
    var reps = REPS
    if (args.length > 0) reps = args(0).toInt
    System.out.println(
      "ITEMS: " + ITEMS + " PRODUCERS: " + PRODUCERS + " PROCESSORS: " + PROCESSORS + " CONSUMERS: " + CONSUMERS + " CAP: " + CAP
    )
    for (rep <- 0 until reps) {
      oneRun()
      Thread.sleep(1000)
    }
  }

  @throws[Exception]
  def oneRun(): Unit = {
    val startTime = System.nanoTime()
    for (i <- 0 until PRODUCERS) {
      new Pub().fork()
    }
    phaser.arriveAndAwaitAdvance()
    val elapsed = System.nanoTime() - startTime
    val secs = elapsed.toDouble / (1000L * 1000 * 1000)
    val ips = NEXTS / secs
    System.out.printf("Time: %7.2f", secs)
    System.out.printf(" items per sec: %14.2f\n", ips)
    System.out.println(ForkJoinPool.commonPool())
  }

  final class Sub extends Flow.Subscriber[Boolean] {
    var count = 0
    var subscription: Flow.Subscription = null

    def onSubscribe(s: Flow.Subscription): Unit = {
      subscription = s
      s.request(CAP)
    }

    def onNext(b: Boolean): Unit = {
      if (b && ({
            count += 1; count
          } & ((CAP >>> 1) - 1)) == 0) subscription.request(CAP >>> 1)
    }

    def onComplete(): Unit = {
      if (count != ITEMS)
        System.out.println("Error: remaining " + (ITEMS - count))
      phaser.arrive()
    }

    def onError(t: Throwable): Unit = {
      t.printStackTrace()
    }
  }

  final class Proc(executor: Executor, maxBufferCapacity: Int)
      extends SubmissionPublisher[Boolean](executor, maxBufferCapacity)
      with Flow.Processor[Boolean, Boolean] {
    var subscription: Flow.Subscription = null
    var count = 0

    def onSubscribe(subscription: Flow.Subscription): Unit = {
      this.subscription = subscription
      subscription.request(CAP)
    }

    def onNext(item: Boolean): Unit = {
      if (({
            count += 1; count
          } & ((CAP >>> 1) - 1)) == 0) subscription.request(CAP >>> 1)
      submit(item)
    }

    def onError(ex: Throwable): Unit = {
      closeExceptionally(ex)
    }

    def onComplete(): Unit = {
      close()
    }
  }

  final class Pub extends RecursiveAction {
    final val pub =
      new SubmissionPublisher[Boolean](ForkJoinPool.commonPool(), CAP)

    def compute(): Unit = {
      val p = pub
      for (j <- 0 until PROCESSORS) {
        val t =
          new Proc(ForkJoinPool.commonPool(), CAP)
        for (i <- 0 until CONSUMERS) {
          t.subscribe(new Sub())
        }
        p.subscribe(t)
      }
      for (i <- 0 until ITEMS) {
        p.submit(true)
      }
      p.close()
    }
  }
}
