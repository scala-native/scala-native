package java.util.stream

import java.util.function.Function

object StreamSuite extends tests.Suite {
  test("Stream.builder can build an empty stream") {
    val s  = Stream.builder().build()
    val it = s.iterator()
    assert(!it.hasNext())
  }

  test("Stream.empty is empty") {
    val s  = Stream.empty[Int]()
    val it = s.iterator()
    assert(!it.hasNext())
  }

  test("Stream.of can put elements in a stream") {
    val s  = Stream.of(1, 2, 3)
    val it = s.iterator()
    assert(it.next() == 1)
    assert(it.next() == 2)
    assert(it.next() == 3)
    assert(!it.hasNext())
  }

  test("Stream.flatMap works") {
    val s = Stream.of(1, 2, 3)
    val mapper = new Function[Int, Stream[Int]] {
      override def apply(v: Int): Stream[Int] =
        Stream.of((1 to v): _*)
    }
    val s2 = s.flatMap(mapper)
    val it = s2.iterator()

    assert(it.next() == 1)
    assert(it.next() == 1)
    assert(it.next() == 2)
    assert(it.next() == 1)
    assert(it.next() == 2)
    assert(it.next() == 3)
    assert(!it.hasNext())
  }

  test("Stream.flatMap works twice") {
    val stream = Stream.of(1, 2, 3)
    val mapper1 = new Function[Int, Stream[Int]] {
      override def apply(v: Int): Stream[Int] =
        Stream.of((v to 3): _*)
    }
    val mapper2 = new Function[Int, Stream[Int]] {
      override def apply(v: Int): Stream[Int] =
        Stream.of((5 to v by -1): _*)
    }
    val s2 = stream.flatMap(mapper1).flatMap(mapper2)
    val expected =
      Seq(5, 4, 3, 2, 1, 5, 4, 3, 2, 5, 4, 3, 5, 4, 3, 2, 5, 4, 3, 5, 4, 3)
    val result = scala.collection.mutable.ArrayBuffer.empty[Int]
    val it     = s2.iterator()
    while (it.hasNext()) {
      result += it.next()
    }
    assert(result == expected)
  }

  test("Stream.onClose works") {
    var success = false
    val handler = new Runnable { override def run(): Unit = success = true }
    val s       = Stream.empty[Int]().onClose(handler)
    assert(!success)
    s.close()
    assert(success)
  }
}
