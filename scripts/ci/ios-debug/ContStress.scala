// Temporary experiment: cost of Thread.yield in the iOS simulator
import java.util.concurrent.CountDownLatch

import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

@extern object qos {
  def qos_class_self(): CUnsignedInt = extern
  def qos_class_main(): CUnsignedInt = extern
  def pthread_set_qos_class_self_np(qos: CUnsignedInt, relpri: CInt): CInt =
    extern
  def sched_yield(): CInt = extern
}

object Test {
  def measure(label: String, threads: Int, raiseQos: Boolean, spin: Boolean)(
      op: () => Unit
  ): Unit = {
    val done = new CountDownLatch(threads)
    val n = 300
    val start = System.nanoTime()
    @volatile var stop = false
    if (spin) {
      val t = new Thread(() => while (!stop) {})
      t.setDaemon(true)
      t.start()
    }
    (0 until threads).foreach { _ =>
      new Thread(() => {
        if (raiseQos) qos.pthread_set_qos_class_self_np(0x21.toUInt, 0)
        var i = 0
        while (i < n) { op(); i += 1 }
        done.countDown()
      }).start()
    }
    done.await()
    stop = true
    val micros = (System.nanoTime() - start) / 1000
    println(
      s"$label threads=$threads raiseQos=$raiseQos spin=$spin: " +
        s"${micros / n} us/iteration"
    )
  }

  def main(args: Array[String]): Unit = {
    println(
      s"cpus=${Runtime.getRuntime().availableProcessors()} " +
        s"qos_main=${qos.qos_class_main().toHexString} " +
        s"qos_self=${qos.qos_class_self().toHexString}"
    )
    for {
      spin <- List(false, true)
      raise <- List(false, true)
      threads <- List(1, 4, 8)
    } {
      measure("Thread.yield", threads, raise, spin)(() => Thread.`yield`())
      measure("sched_yield ", threads, raise, spin)(() => qos.sched_yield())
    }
    println("finished=true")
  }
}
