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
  test("same extern name in two different objects") {
    val bufsize = 10L
    val buf1    = stackalloc[Byte](bufsize)
    val buf2    = stackalloc[Byte](bufsize)
    Ext1.snprintf(buf1, bufsize, c"%s", c"hello")
    assert(string.strcmp(buf1, c"hello") == 0)
    Ext2.p(buf2, bufsize, c"%d", 1)
    assert(string.strcmp(buf2, c"1") == 0)
  }

  val cb: CFuncPtr0[CInt] = new CFuncPtr0[Int] {
    override def apply(): Int = 42
  }
  test("allows to use generic function as argument") {
    val res0 = testlib.exec0(cb) //expected CFuncPtr0[Int]
    val res1 = testlib.exec(cb)  //expected CFuncPtr
    assert(res0 == 42)
    assert(res1 == 42)
  }
}
