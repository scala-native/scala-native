package scala.scalanative.native

import scalanative.libc.{stdio, stdlib, string}

object CInteropSuite extends tests.Suite {

  test("varargs") {
    val buff = stackalloc[CChar](64)
    stdio.sprintf(buff, c"%d %d %d", 1, 2, 3)
    for ((c, i) <- "1 2 3".zipWithIndex) {
      assert(buff(i) == c)
    }
  }

  test("pointer substraction") {
    Zone { implicit z =>
      val carr: Ptr[CChar] = toCString("abcdefg")
      val cptr: Ptr[CChar] = string.strchr(carr, 'd')
      assert((cptr - carr).toInt == 3)
      assert((carr - cptr).toInt == -3)

      val iarr: Ptr[CInt] = stackalloc[CInt](8)
      val iptr: Ptr[CInt] = iarr + 4
      assert((iptr - iarr).toInt == 4)
      assert((iarr - iptr).toInt == -4)

      type StructType = CStruct4[CChar, CInt, CLong, CDouble]
      val sarr: Ptr[StructType] = stackalloc[StructType](8)
      val sptr: Ptr[StructType] = sarr + 7
      assert((sptr - sarr).toInt == 7)
      assert((sarr - sptr).toInt == -7)
    }
  }

  def randFunc = CFunctionPtr.fromFunction0(stdlib.rand _)

  test("CFunctionPtr cast and call with given signature") {
    val wrongRand = randFunc.cast[CFunctionPtr1[Int, Int]] // wrong signature
    wrongRand(42) // no argument declared
  }

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
