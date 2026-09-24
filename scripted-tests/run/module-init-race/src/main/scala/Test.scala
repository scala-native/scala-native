import java.util.concurrent.atomic.AtomicIntegerArray
import java.util.concurrent.{CountDownLatch, TimeUnit}

// Regression guard for stack-backed module initialization contexts in
// nativelib/src/main/resources/scala-native/module_load.c.
//
// A thread may load a tagged context pointer immediately before its owner
// publishes the initialized module and returns. The reader must not dereference
// that pointer: the owner's stack can be reused before the reader inspects it.
// Owners recognize recursive initialization by matching the pointer against
// their thread-local chain of live contexts; competing threads wait through the
// class monitor without inspecting another thread's stack.
//
// The plain runs pin the observable contract (every thread that first-touches a
// module gets the one shared instance with correct field values, and each
// constructor runs exactly once) and act as a crash canary under heavy
// concurrent first-touch. The ThreadSanitizer phase (see build.sbt) is the
// actual race detector.

object Config {
  final val K = 32 // distinct modules, each first-touched under contention
  final val T = 8 // threads racing on each module's first touch
  final val MarkerBase = 0x5ca1ab1eL
}

// Records how many times each module constructor ran. Initialized from main
// before any worker starts, so touching a module under test never triggers this
// holder's own initialization inside the raced window.
object Counters {
  private val ctorRuns = new AtomicIntegerArray(Config.K)
  def bump(index: Int): Unit = { ctorRuns.incrementAndGet(index); () }
  def runs(index: Int): Int = ctorRuns.get(index)
}

// Base of the modules under test. Each object below is a distinct module, so it
// exercises a distinct module-init slot; they share this constructor but not
// their initialization. `marker` is read back by every racing thread and
// `Counters.bump` records the single construction.
abstract class Mod(index: Int) {
  Counters.bump(index)
  val marker: Long = Config.MarkerBase + index
}

object M00 extends Mod(0)
object M01 extends Mod(1)
object M02 extends Mod(2)
object M03 extends Mod(3)
object M04 extends Mod(4)
object M05 extends Mod(5)
object M06 extends Mod(6)
object M07 extends Mod(7)
object M08 extends Mod(8)
object M09 extends Mod(9)
object M10 extends Mod(10)
object M11 extends Mod(11)
object M12 extends Mod(12)
object M13 extends Mod(13)
object M14 extends Mod(14)
object M15 extends Mod(15)
object M16 extends Mod(16)
object M17 extends Mod(17)
object M18 extends Mod(18)
object M19 extends Mod(19)
object M20 extends Mod(20)
object M21 extends Mod(21)
object M22 extends Mod(22)
object M23 extends Mod(23)
object M24 extends Mod(24)
object M25 extends Mod(25)
object M26 extends Mod(26)
object M27 extends Mod(27)
object M28 extends Mod(28)
object M29 extends Mod(29)
object M30 extends Mod(30)
object M31 extends Mod(31)

// The initializer must be able to see its own partially constructed module.
// Hold its initialization open so competing threads exercise the waiting path
// while the owner takes the reentrant path through InitializationContext.
object ReentrantControl {
  val entered = new CountDownLatch(1)
  val proceed = new CountDownLatch(1)
}

object Reentrant {
  ReentrantControl.entered.countDown()
  ReentrantControl.proceed.await()
  val self: Reentrant.type = Reentrant
}

object Test {
  // Deferred first-touch thunks: referencing Mxx here rather than eagerly is
  // what makes each module's initialization happen on a worker thread once the
  // latch opens, instead of serially from main.
  private val modules: Array[() => Mod] = Array(
    () => M00,
    () => M01,
    () => M02,
    () => M03,
    () => M04,
    () => M05,
    () => M06,
    () => M07,
    () => M08,
    () => M09,
    () => M10,
    () => M11,
    () => M12,
    () => M13,
    () => M14,
    () => M15,
    () => M16,
    () => M17,
    () => M18,
    () => M19,
    () => M20,
    () => M21,
    () => M22,
    () => M23,
    () => M24,
    () => M25,
    () => M26,
    () => M27,
    () => M28,
    () => M29,
    () => M30,
    () => M31
  )

  private def check(cond: Boolean, message: => String): Unit =
    if (!cond) throw new AssertionError(message)

  private final class ReentrantWorker(
      slot: Int,
      start: CountDownLatch,
      instances: Array[AnyRef]
  ) extends Thread {
    @volatile var failure: Throwable = null
    override def run(): Unit =
      try {
        start.await()
        instances(slot) = Reentrant
      } catch {
        case t: Throwable => failure = t
      }
  }

  private final class Worker(
      moduleIndex: Int,
      slot: Int,
      latch: CountDownLatch,
      instances: Array[AnyRef],
      markers: Array[Long]
  ) extends Thread {
    @volatile var failure: Throwable = null
    override def run(): Unit =
      try {
        latch.await()
        val module = modules(moduleIndex)()
        instances(slot) = module
        markers(slot) = module.marker
      } catch {
        case t: Throwable => failure = t
      }
  }

  def main(args: Array[String]): Unit = {
    val k = Config.K
    val t = Config.T

    // Force the support holders to initialize before any worker starts.
    Counters.runs(0)
    check(
      ReentrantControl.entered.getCount() == 1L,
      "recursive module control initialized unexpectedly"
    )

    val reentrantStart = new CountDownLatch(1)
    val reentrantInstances = new Array[AnyRef](t)
    val reentrantWorkers = Array.tabulate(t) { slot =>
      val worker =
        new ReentrantWorker(slot, reentrantStart, reentrantInstances)
      worker.start()
      worker
    }
    reentrantStart.countDown()
    check(
      ReentrantControl.entered.await(60, TimeUnit.SECONDS),
      "recursive module initializer did not start"
    )
    // Give competitors time to observe the published context while its owner
    // remains inside the constructor.
    Thread.sleep(50)
    ReentrantControl.proceed.countDown()
    reentrantWorkers.zipWithIndex.foreach {
      case (worker, slot) =>
        worker.join(TimeUnit.SECONDS.toMillis(60))
        check(!worker.isAlive(), s"recursive module worker $slot timed out")
        if (worker.failure != null) throw worker.failure
        check(
          reentrantInstances(slot) eq Reentrant,
          s"recursive module worker $slot observed a different instance"
        )
    }
    check(Reentrant.self eq Reentrant, "recursive module initialization failed")

    val latch = new CountDownLatch(1)
    val instances = new Array[AnyRef](k * t)
    val markers = new Array[Long](k * t)
    val workers = new Array[Worker](k * t)

    var i = 0
    while (i < k) {
      var j = 0
      while (j < t) {
        val slot = i * t + j
        val worker = new Worker(i, slot, latch, instances, markers)
        workers(slot) = worker
        worker.start()
        j += 1
      }
      i += 1
    }

    // Release every worker at once to maximize concurrent first-touch.
    latch.countDown()

    var w = 0
    while (w < workers.length) {
      workers(w).join(TimeUnit.SECONDS.toMillis(60))
      check(!workers(w).isAlive(), s"worker $w did not finish in time")
      if (workers(w).failure != null) throw workers(w).failure
      w += 1
    }

    i = 0
    while (i < k) {
      val expectedMarker = Config.MarkerBase + i
      val first = instances(i * t)
      check(first != null, s"module $i produced a null instance")
      var j = 0
      while (j < t) {
        val slot = i * t + j
        check(
          instances(slot) eq first,
          s"module $i: worker $j observed a different instance"
        )
        check(
          markers(slot) == expectedMarker,
          s"module $i: worker $j observed marker ${markers(slot)}, " +
            s"expected $expectedMarker"
        )
        j += 1
      }
      check(
        Counters.runs(i) == 1,
        s"module $i constructor ran ${Counters.runs(i)} times, expected 1"
      )
      i += 1
    }

    println(
      s"module-init-race: OK ($k modules x $t threads, one shared instance " +
        "with correct marker and a single construction each)"
    )
  }
}
