package scala.scalanative

import native._
import native.Nat.{_2}

import posix.unistd
import native.stdio

/**
 * Created by remi on 02/03/17.
 */
object SyscallsSuite extends tests.Suite {
  /*
  test("simple pipe") {

    val p = stackalloc[CArray[CInt, _2]]
    val err = unistd.pipe(p)

    assert(err == 0)

    val pid = unistd.fork()

    assert(pid != -1)

    // http://www2.cs.uregina.ca/~hamilton/courses/330/notes/unix/pipes/pipes.html
    val message = "Hi Mom!"


    if(pid == 0) {
      assert(unistd.write(!p._1, toCString(message), message.length + 1) == message.length + 1)
    } else {
      val cstr = stackalloc[CChar](32)
      assert(unistd.read(!p._2, cstr, message.length + 1) == message.length + 1)
      val out = fromCString(cstr)
      println(out)

      wait(pid)

      assert(out == message)
    }
  }*/

  test("pipe + dup + getpid + getppid") {

    val p   = stackalloc[CArray[CInt, _2]]
    val err = unistd.pipe(p)

    assert(err == 0)

    val fd1 = unistd.dup(!p._1)
    val fd2 = unistd.dup(!p._2)

    assert(fd1 != -1 && fd2 != -1)

    val pid = unistd.fork()

    assert(pid != -1)

    println((!p._1, !p._2))

    if (pid == 0) {
      val msg: (CInt, CInt) = (unistd.getpid(), unistd.getppid())

      assert(unistd.write(fd2, toBytePtr(msg._1), 4) == 4)
      assert(unistd.write(fd2, toBytePtr(msg._2), 4) == 4)
    } else {
      val msg: (Ptr[Byte], Ptr[Byte]) =
        (stackalloc[Byte](4), stackalloc[Byte](4))
      assert(unistd.read(fd1, msg._1, 4) == 4)
      assert(unistd.read(fd1, msg._2, 4) == 4)

      wait(pid)

      assert(pid == toCInt(msg._1) && unistd.getpid() == toCInt(msg._2))

    }
  }

  def toBytePtr(a: CInt): Ptr[Byte] = {
    val p = stackalloc[Byte](4)

    for (i <- 3 to 0) {
      !(p + i) = ((a >> 3 - i) & 0xFF).toByte;
    }

    p

  }

  def toCInt(p: Ptr[Byte]): CInt = {
    (!p << 24) | (!(p + 1) << 16) | (!(p + 2) << 8) | !(p + 3)

  }

}
