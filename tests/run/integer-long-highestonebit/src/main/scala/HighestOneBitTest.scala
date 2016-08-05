import java.lang.{Integer, Long}

object HighestOneBitTest {
  def main(args: Array[String]): Unit = {
    testInteger()
    testLong()
  }

  def testInteger(): Unit = {
    assert(Integer.highestOneBit(1) == 1)
    assert(Integer.highestOneBit(2) == 2)
    assert(Integer.highestOneBit(3) == 2)
    assert(Integer.highestOneBit(4) == 4)
    assert(Integer.highestOneBit(5) == 4)
    assert(Integer.highestOneBit(6) == 4)
    assert(Integer.highestOneBit(7) == 4)
    assert(Integer.highestOneBit(8) == 8)
    assert(Integer.highestOneBit(9) == 8)
    assert(Integer.highestOneBit(63) == 32)
    assert(Integer.highestOneBit(64) == 64)
  }

  def testLong(): Unit = {
    assert(Long.highestOneBit(1) == 1L)
    assert(Long.highestOneBit(2) == 2L)
    assert(Long.highestOneBit(3) == 2L)
    assert(Long.highestOneBit(4) == 4L)
    assert(Long.highestOneBit(5) == 4L)
    assert(Long.highestOneBit(6) == 4L)
    assert(Long.highestOneBit(7) == 4L)
    assert(Long.highestOneBit(8) == 8L)
    assert(Long.highestOneBit(9) == 8L)
    assert(Long.highestOneBit(63) == 32L)
    assert(Long.highestOneBit(64) == 64L)
    assert(Long.highestOneBit(Int.MaxValue) == 1073741824)
    assert(Long.highestOneBit(Int.MaxValue + 1L) == 2147483648L)
  }
}
