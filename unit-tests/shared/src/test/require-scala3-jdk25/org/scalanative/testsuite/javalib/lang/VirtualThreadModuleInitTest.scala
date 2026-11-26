package org.scalanative.testsuite.javalib.lang

import java.util.concurrent.{ConcurrentLinkedQueue, CountDownLatch, TimeUnit}

import org.junit.Assert._
import org.junit._

import scala.scalanative.junit.utils.AssumesHelper

// Every module is initialized once per process, so each scenario has its own
// objects. Their constructors unmount the initializing VT, which then usually
// resumes on another carrier (#5413).
private object ModuleInitDelay {
  def apply(): Unit = Thread.sleep(20)
}

private object SlowModule0 { val v = { ModuleInitDelay(); 0 } }
private object SlowModule1 { val v = { ModuleInitDelay(); 1 } }
private object SlowModule2 { val v = { ModuleInitDelay(); 2 } }
private object SlowModule3 { val v = { ModuleInitDelay(); 3 } }
private object SlowModule4 { val v = { ModuleInitDelay(); 4 } }
private object SlowModule5 { val v = { ModuleInitDelay(); 5 } }
private object SlowModule6 { val v = { ModuleInitDelay(); 6 } }
private object SlowModule7 { val v = { ModuleInitDelay(); 7 } }

// Reads its own module after unmounting, through another object so the access
// goes through the module load path rather than `this`.
private object SelfReadingModule0 {
  val a = 10
  val b = { ModuleInitDelay(); ReadSelfReading.a0() + 1 }
}
private object SelfReadingModule1 {
  val a = 20
  val b = { ModuleInitDelay(); ReadSelfReading.a1() + 1 }
}
private object SelfReadingModule2 {
  val a = 30
  val b = { ModuleInitDelay(); ReadSelfReading.a2() + 1 }
}
private object SelfReadingModule3 {
  val a = 40
  val b = { ModuleInitDelay(); ReadSelfReading.a3() + 1 }
}
private object ReadSelfReading {
  def a0(): Int = SelfReadingModule0.a
  def a1(): Int = SelfReadingModule1.a
  def a2(): Int = SelfReadingModule2.a
  def a3(): Int = SelfReadingModule3.a
}

// Nested: each outer constructor unmounts, then loads a slow inner module
// shared by all of them, and unmounts again inside it.
private object InnerModule { val v = { ModuleInitDelay(); 100 } }
private object OuterModule0 { val v = { ModuleInitDelay(); InnerModule.v } }
private object OuterModule1 { val v = { ModuleInitDelay(); InnerModule.v + 1 } }
private object OuterModule2 { val v = { ModuleInitDelay(); InnerModule.v + 2 } }
private object OuterModule3 { val v = { ModuleInitDelay(); InnerModule.v + 3 } }

object VirtualThreadModuleInitTest {
  @BeforeClass def checkRuntime(): Unit =
    AssumesHelper.assumeSupportsVirtualThreads()
}

class VirtualThreadModuleInitTest {
  private val Timeout = 30000L

  /** Each platform thread starts a VT that runs `read` once all are ready, so
   *  the VTs race on the first load and land on different carriers.
   */
  private def raceOnFirstLoad(reads: Seq[() => Int]): Seq[Int] = {
    ModuleInitDelay() // warm up the helper and the VT machinery
    Thread.ofVirtual().start(() => ()).join()

    val results = new ConcurrentLinkedQueue[Int]()
    val errors = new ConcurrentLinkedQueue[Throwable]()
    val start = new CountDownLatch(1)
    val virtualThreads = new ConcurrentLinkedQueue[Thread]()
    val launchers = for {
      _ <- 0 until 4
      read <- reads
    } yield Thread.ofPlatform().start { () =>
      val vt = Thread
        .ofVirtual()
        .uncaughtExceptionHandler((_, ex) => errors.add(ex))
        .start { () =>
          start.await()
          results.add(read())
        }
      virtualThreads.add(vt)
    }
    launchers.foreach(_.join(Timeout))
    start.countDown()
    virtualThreads.forEach { vt =>
      vt.join(Timeout)
      assertFalse(s"$vt did not terminate", vt.isAlive())
    }
    errors.forEach(throw _)
    val all = Seq.newBuilder[Int]
    results.forEach(all += _)
    all.result().sorted
  }

  private def expected(values: Seq[Int]): Seq[Int] =
    values.flatMap(Seq.fill(4)(_)).sorted

  @Test def moduleConstructorUnmounts(): Unit = {
    val reads = Seq[() => Int](
      () => SlowModule0.v,
      () => SlowModule1.v,
      () => SlowModule2.v,
      () => SlowModule3.v,
      () => SlowModule4.v,
      () => SlowModule5.v,
      () => SlowModule6.v,
      () => SlowModule7.v
    )
    assertEquals(expected(0 until 8), raceOnFirstLoad(reads))
  }

  @Test def moduleReadsItselfAfterUnmount(): Unit = {
    val reads = Seq[() => Int](
      () => SelfReadingModule0.b,
      () => SelfReadingModule1.b,
      () => SelfReadingModule2.b,
      () => SelfReadingModule3.b
    )
    assertEquals(expected(Seq(11, 21, 31, 41)), raceOnFirstLoad(reads))
  }

  @Test def nestedModuleConstructorsUnmount(): Unit = {
    val reads = Seq[() => Int](
      () => OuterModule0.v,
      () => OuterModule1.v,
      () => OuterModule2.v,
      () => OuterModule3.v
    )
    assertEquals(expected(Seq(100, 101, 102, 103)), raceOnFirstLoad(reads))
  }
}
