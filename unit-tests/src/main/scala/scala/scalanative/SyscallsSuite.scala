package scala.scalanative

import scala.scalanative.native._
import scala.scalanative.native.Nat.{_2}
import scala.scalanative.posix.unistd

/**
  * Created by remi on 02/03/17.
  */
object SyscallsSuite extends tests.Suite {

  test("simple pipe") {

    val p = stackalloc[CArray[CInt, _2]]
    val ptr = stackalloc[CInt]

    val err = pipe(p)

    /* write down pipe */
    (0 to 2) foreach{ i =>
      !ptr = i
      write(p._2, ptr, 4)
    }

    /* read pipe */
    (0 to 2) foreach { i =>
      read(p._1, ptr, 4)
      assert(!ptr == i)
    }

  }


}
