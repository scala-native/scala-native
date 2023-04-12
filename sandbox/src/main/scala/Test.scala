import scala.scalanative.runtime.Continuations.*
import scala.collection.mutable.ArrayBuffer

object Test {
  def main(args: Array[String]): Unit = {
    basic()
    fibonacci()
    // generator()
  }

  // def generator(): Unit = {
  //   object Gen {
  //     opaque type Label[+T] = BoundaryLabel[Gen[T]]
  //     enum Gen[+T] {
  //       case Next(cur: T, nx: () => Gen[T])
  //       case Empty
  //     }

  //     inline def apply[T](inline f: Label[T] ?=> Unit): Gen[T] =
  //       boundary[Gen[T]] {
  //         f
  //         Gen.Empty
  //       }

  //     inline def put[T](inline value: T)(using Label[T]): Unit =
  //       suspend[Gen[T]](cont => Gen.Next(value, cont))

  //     // inline def put[T](values: List[T])(using Label[T]): Unit =
  //     //   for (value <- values) do
  //     //     put(value)

  //     // private def collect[T](t: Gen[T]): List[T] = t match {
  //     //   case Gen.Next(cur, nx) => cur +: collect(nx())
  //     //   case Gen.Empty => List.empty
  //     // }
  //   }

  //   import Gen.*

  //   val nums = Gen[Int] {
  //     var i = 0
  //     while (true) { put(i); i += 1; }
  //   }

  //   val evens = Gen {

  //   }
  // }

  def fibonacci(): Unit = {
    case class Seqnt[T, R](v: T, nx: R => Seqnt[T, R])
    type Seqn[T] = Seqnt[T, Int]

    def fib = boundary[Seqn[Int]] {
      // these get boxed, so it's not really working different from a generator
      var a = 1
      var b = 1
      while (true) {
        val steps = suspend[Int, Seqn[Int]](c => Seqnt(a, c))
        for (i <- 1 to steps) {
          val c = a + b
          a = b
          b = c
        }
      }
      Seqnt(0, ???)
    }

    val fibs = ArrayBuffer(fib)
    for (step <- 1 to 10) {
      fibs += fibs(step-1).nx(1)
    }

    println(fibs.map(_.v))

    // val fibs2 = ArrayBuffer(fib)
    // for (step <- 1 to 10) {
    //   fibs2 += fibs(0).nx(2)
    // }

    // println(fibs2.map(_.v))
  }

  def basic(): Unit = {
    enum Response[T] {
      case Next(nx: () => Response[T], v: T)
      case End(v: T)
    }
    import Response.*
    val oneThenTwo = boundary[Response[Int]] {
      suspend[Response[Int]](Next(_, 1))
      End(2)
    }

    oneThenTwo match {
      case Next(nx, v) =>
        assert(v == 1)
        val v2 = nx()
        assert(v2 == End(2))
      case End(v) =>
        assert(false)
    }
  }
}
