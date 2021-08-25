package scala.scalanative
package unsafe

import org.junit.Ignore
import org.junit.Test
import org.junit.Assert._

import scalanative.unsafe._
import scalanative.unsigned._

object ExternTest {
  /* These can be nested inside an object but not a class - see #897
   * These also are having problems at the top level with our CI
   * with the current Docker configuration, see #1991
   */
  @extern
  object Ext1 {
    def snprintf(buf: CString, size: CSize, format: CString, l: CString): Int =
      extern
  }
  @extern
  object Ext2 {
    @name("snprintf")
    def p(buf: CString, size: CSize, format: CString, i: Int): Int = extern
  }

  // workaround for CI
  def runTest(): Unit = {
    import scalanative.libc.string
    val bufsize = 10.toUInt
    val buf1 = stackalloc[Byte](bufsize)
    val buf2 = stackalloc[Byte](bufsize)
    Ext1.snprintf(buf1, bufsize, c"%s", c"hello")
    assertTrue(string.strcmp(buf1, c"hello") == 0)
    Ext2.p(buf2, bufsize, c"%d", 1)
    assertTrue(string.strcmp(buf2, c"1") == 0)
  }
}

class ExternTest {

  @Test def externVariableReadAndAssign(): Unit = {
    import scala.scalanative.posix.getopt

    val args = Seq("skipped", "skipped", "skipped", "-b", "-f", "farg")

    Zone { implicit z =>
      val argv = stackalloc[CString](args.length.toUInt)

      for ((arg, i) <- args.zipWithIndex) {
        argv(i) = toCString(arg)
        ()
      }

      // Skip first 3 arguments
      getopt.optind = 3

      val bOpt = getopt.getopt(args.length, argv, c"bf:")
      assertTrue(bOpt == 'b')

      val fOpt = getopt.getopt(args.length, argv, c"bf:")
      assertTrue(fOpt == 'f')
      val fArg = fromCString(getopt.optarg)
      assertTrue(fArg == "farg")
    }
  }

  @Test def sameExternNameInTwoDifferentObjects_Issue1652(): Unit = {
    ExternTest.runTest()
  }

  val cb: CFuncPtr0[CInt] = () => 42
  @Test def allowsToUseGenericFunctionAsArgument(): Unit = {
    val res0 = testlib.exec0(cb) //expected CFuncPtr0[Int]
    val res1 = testlib.exec(cb) //expected CFuncPtr
    assertTrue(res0 == 42)
    assertTrue(res1 == 42)
  }
}
