import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
object Test {
  def main(args: Array[String]): Unit = {
    println("Hello, World!")
    RustLib.hello()
    RustLib.hello2()
    // println(RustLib.isEven(31))
    // println(RustLib.isEven(32))

  }
  //   import scalanative.runtime.Intrinsics._
  //   import scalanative.runtime.MemoryLayout

  //   1.to(1e3.toInt).foreach { iteration =>
  //     println(Thread.currentThread().getId())
  //     this.synchronized {
  //       this.getClass().synchronized {
  //         println(s"iteration: $iteration")
  //         val thisLock = loadLong(
  //           castLongToRawPtr(
  //             castRawPtrToLong(
  //               castObjectToRawPtr(this)
  //             ) + MemoryLayout.Object.LockWordOffset
  //           )
  //         )
  //         println(thisLock)
  //         val thisClassLock = loadLong(
  //           castLongToRawPtr(
  //             castRawPtrToLong(
  //               castObjectToRawPtr(this.getClass())
  //             ) + MemoryLayout.Object.LockWordOffset
  //           )
  //         )
  //         println(thisClassLock)
  //       }
  //     }
  //     val str = Array.fill(10)(util.Random.nextPrintableChar()).mkString
  //     println(str)
  //     scalanative.runtime.GC.collect()
  //   // Future{println("hello")}
  //   // testFutures()
  //   // testFlatMap()
  //   // testParCollections()
  //   }
  // }

  // def testFutures(): Unit = {
  //   print("Test futures: ")
  //   val in = 0.until(1e5.toInt)
  //   val task = Future.sequence {
  //     in.map { v =>
  //       Future {
  //         v * 10 + 1
  //       }
  //         .map(_ - 1)
  //         .map(_ / 10)
  //     }
  //   }
  //   val res = Await.result(task, 1.minute)
  //   println(res.diff(in).isEmpty)
  // }

  // def testFlatMap(): Unit = {
  //   print("Test flatMap: ")
  //   val in = 0.until(1e5.toInt)
  //   val task = Future.sequence {
  //     in.map { v =>
  //       Future {
  //         v * 10 + 1
  //       }
  //         .flatMap { v => Future(v - 1) }
  //         .flatMap(v => Future(v / 10))
  //     }
  //   }
  //   val res = Await.result(task, 1.minute)
  //   println(res.diff(in).isEmpty)
  // }

  // def testParCollections(): Unit = {
  //   print("Test par collections: ")
  //   val in = 0.until(1e5.toInt)
  //   val res = in.par
  //     .map(_ * 10 + 1)
  //     .map(_ - 1)
  //     .map(_ / 10)
  //     .seq
  //   println(res.diff(in).isEmpty)
  // }
}
