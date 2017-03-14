package scala.scalanative

import native._
import native.Nat.{_2}

import posix.unistd
import native.stdio
import native.stdlib

/**
 * Created by remi on 02/03/17.
 */
object SyscallsSuite extends tests.Suite {

  test("pipe + dup + getpid + getppid") {

    val p   = stackalloc[CArray[CInt, _2]]

    val err = unistd.pipe(p)

    assert(err == 0)

    val fd1 = unistd.dup(!p._1)
    val fd2 = unistd.dup(!p._2)

    assert(fd1 != -1 && fd2 != -1)

    val pid = unistd.fork()

    assert(pid != -1)

    if (pid == 0) {

      var msg = stackalloc[Byte](8)

      fillPtr(msg, unistd.getpid())
      fillPtr(msg + 4, unistd.getppid())

      assert(unistd.write(fd2, msg, 8) == 8)


    } else {

      var msg1 = stackalloc[Byte](4)
      var msg2 = stackalloc[Byte](4)

      assert(unistd.read(fd1, msg1, 4) == 4)
      assert(unistd.read(fd1, msg2, 4) == 4)

      wait(pid)

      assert(pid == toCInt(msg1) && unistd.getpid() == toCInt(msg2))

    }
  }

  def fillPtr(p: Ptr[Byte], a: CInt) = {
    p(0) = (a >> 24).toByte
    p(1) = ((a >> 16) & 0xFF).toByte
    p(2) = ((a >> 8) & 0xFF).toByte
    p(3) = (a & 0xFF).toByte
  }

  def toCInt(p: Ptr[Byte]): CInt = {
    (p(0).toInt << 24) | (p(1).toInt << 16) | (p(2).toInt << 8) | p(3)
  }

}
