package scala.scalanative.unsafe

import org.junit.Assert._
import org.junit.Test

// The libc function, declared by a different owner than the redirect below
@extern object ExternRedirectTestLibc {
  def llabs(x: CLongLong): CLongLong = extern
}

@extern object ExternRedirectTestNative {
  def strtoll(str: CString, end: Ptr[CString], base: CInt): CLongLong = extern
  def abs(x: CInt): CInt = extern

  @name("scalanative_test_redirect_hex")
  def parseHex(str: CString): CLongLong = externRedirect(strtoll(str, null, 16))

  @name("scalanative_test_redirect_reordered")
  def parse(base: CInt, str: CString): CLongLong =
    externRedirect(strtoll(str, null, base))

  @name("scalanative_test_redirect_block")
  def parseScaled(str: CString, factor: CInt): CLongLong = externRedirect {
    val parsed = strtoll(str, null, 10)
    parsed * factor
  }

  @name("scalanative_test_redirect_unit")
  def store(ptr: Ptr[CInt], value: CInt): Unit = externRedirect {
    !ptr = abs(value)
  }

  // Named after a libc function: it must not replace that function
  @name("llabs")
  def llabsRedirect(x: CInt): CInt = externRedirect(abs(x) + 1)
}

class ExternRedirectTest {
  import ExternRedirectTestNative._

  @Test def passesLiterals(): Unit =
    assertEquals(255L, parseHex(c"ff").toLong)

  @Test def reordersParameters(): Unit =
    assertEquals(8L, parse(8, c"10").toLong)

  @Test def evaluatesBlocks(): Unit =
    assertEquals(42L, parseScaled(c"21", 2).toLong)

  @Test def returnsUnit(): Unit = {
    val value = stackalloc[CInt]()
    store(value, -7)
    assertEquals(7, !value)
  }

  @Test def doesNotShadowNativeSymbol(): Unit = {
    assertEquals(6, llabsRedirect(-5))
    assertEquals(5L, ExternRedirectTestLibc.llabs(-5L))
  }
}
