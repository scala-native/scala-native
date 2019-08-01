package scala.scalanative
package unsafe

import scalanative.unsafe._
import scalanative.libc.{stdio, stdlib, string}

object ExternSuite extends tests.Suite {
  test("extern variable read and assign") {
    import scala.scalanative.posix.getopt

    val args = Seq("skipped", "skipped", "skipped", "-b", "-f", "farg")

    Zone { implicit z =>
      val argv = stackalloc[CString](args.length)

      for ((arg, i) <- args.zipWithIndex) {
        argv(i) = toCString(arg)
        ()
      }

      // Skip first 3 arguments
      getopt.optind = 3

      val bOpt = getopt.getopt(args.length, argv, c"bf:")
      assert(bOpt == 'b')

      val fOpt = getopt.getopt(args.length, argv, c"bf:")
      assert(fOpt == 'f')
      val fArg = fromCString(getopt.optarg)
      assert(fArg == "farg")
    }
  }
}
