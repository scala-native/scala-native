package scala.scalanative.runtime

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.unsafe._

class SymbolFormatterTest {
  @Test def acceptsDecoratedAndUndecoratedMangledNames(): Unit = {
    val className = stackalloc[Byte](256)
    val methodName = stackalloc[Byte](256)
    val fileName = stackalloc[Byte](512)
    val line = stackalloc[Int]()

    Seq(c"_SM3fooD3baruEO", c"__SM3fooD3baruEO", c"SM3fooD3baruEO")
      .foreach { symbol =>
        ffi.memset(className.rawptr, 0, Intrinsics.castIntToRawSize(256))
        ffi.memset(methodName.rawptr, 0, Intrinsics.castIntToRawSize(256))
        SymbolFormatter.asyncSafeFromSymbol(
          symbol,
          className,
          methodName,
          fileName,
          line
        )
        assertEquals("foo", fromCString(className))
        assertEquals("bar", fromCString(methodName))
      }
  }
}
