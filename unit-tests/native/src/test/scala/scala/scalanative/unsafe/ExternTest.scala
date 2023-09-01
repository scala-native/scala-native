package scala.scalanative
package unsafe

import org.junit.Ignore
import org.junit.Test
import org.junit.Assert._
import org.junit.Assume._

import scalanative.unsafe._
import scalanative.unsigned._
import scalanative.meta.LinktimeInfo.isWindows

object ExternTest {
  /* These can be nested inside an object but not a class - see #897
   * These also are having problems at the top level with our CI
   * with the current Docker configuration, see #1991
   */
  @extern
  object Ext1 {
    // Previously snprintf method was used here, however on MacOs Arm64
    // it is defined as a macro and takes some additional implicit arguments
    def vsnprintf(
        buf: CString,
        size: CSize,
        format: CString,
        args: CVarArgList
    ): Int =
      extern
  }
  @extern
  object Ext2 {
    @name("vsnprintf")
    def p(buf: CString, size: CSize, format: CString, args: CVarArgList): Int =
      extern
  }

  // workaround for CI
  def runTest(): Unit = Zone { implicit z: Zone =>
    import scalanative.libc.string
    val bufsize = 10.toUInt
    val buf1: Ptr[Byte] = stackalloc[Byte](bufsize)
    val buf2: Ptr[Byte] = stackalloc[Byte](bufsize)

    val arg1 = c"hello"
    Ext1.vsnprintf(buf1, bufsize, c"%s", toCVarArgList(arg1))
    assertEquals("case 1", 0, string.strcmp(buf1, arg1))

    Ext2.p(buf2, bufsize, c"%d", toCVarArgList(1))
    assertEquals("case 2", 0, string.strcmp(buf2, c"1"))
  }
}

class ExternTest {

  @Test def externVariableReadAndAssign(): Unit = {
    assumeFalse("No getOpt in Windows", isWindows)
    if (isWindows) ??? // unsupported extern methods
    else externVariableReadAndAssignUnix()
  }

  def externVariableReadAndAssignUnix(): Unit = {
    import scala.scalanative.posix.unistd

    val args = Seq("skipped", "skipped", "skipped", "-b", "-f", "farg")

    Zone { implicit z =>
      val argv: Ptr[CString] = stackalloc[CString](args.length)

      for ((arg, i) <- args.zipWithIndex) {
        argv(i) = toCString(arg)
        ()
      }

      // Skip first 3 arguments
      unistd.optind = 3

      val bOpt = unistd.getopt(args.length, argv, c"bf:")
      assertTrue(bOpt == 'b')

      val fOpt = unistd.getopt(args.length, argv, c"bf:")
      assertTrue(fOpt == 'f')
      val fArg = fromCString(unistd.optarg)
      assertTrue(fArg == "farg")
    }
  }

  @Test def sameExternNameInTwoDifferentObjects_Issue1652(): Unit = {
    ExternTest.runTest()
  }

  val cb: CFuncPtr0[CInt] = () => 42
  @Test def allowsToUseGenericFunctionAsArgument(): Unit = {
    val res0 = testlib.exec0(cb) // expected CFuncPtr0[Int]
    val res1 = testlib.exec(cb) // expected CFuncPtr
    assertTrue(res0 == 42)
    assertTrue(res1 == 42)
  }
}
