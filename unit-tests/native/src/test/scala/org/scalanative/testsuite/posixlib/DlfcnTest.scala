package org.scalanative.testsuite.posixlib

import org.junit.Test
import org.junit.Assert._
import org.junit.Assume._

import scala.scalanative.meta.LinktimeInfo.{isLinux, isMac}
import scala.scalanative.runtime.PlatformExt

import java.io.File

import scala.scalanative.unsafe._

import scala.scalanative.posix.dlfcn._

class DlfcnTest {

  /* Dlfcn is tested on Linux and macOS.
   * With some additional work, it could be tested on FreeBSD.
   * One would have to find a suitable "known" .so file and,
   * possibly, adjust the message returned by dlerror().
   */

  @Test def dlfcnOpensAndObtainsSymbolAddressLinux(): Unit = {
    if (isLinux) Zone { implicit z =>
      val soFilePrefix =
        if (is32BitPlatform)
          "/lib/i386-linux-gnu/"
        else if (PlatformExt.isArm64)
          "/usr/lib/aarch64-linux-gnu"
        else
          "/lib/x86_64-linux-gnu"

      val soFile = s"${soFilePrefix}/libc.so.6"

      /* Ensure the file exists before trying to "dlopen()" it.
       * Someday the ".so.6" suffix is going to change to ".so.7" or such.
       * When it does do a "soft failure", rather than failing the entire
       * build.
       */
      assumeTrue(
        s"shared library ${soFile} not found",
        (new File(soFile)).exists()
      )

      val handle = dlopen(toCString(soFile), RTLD_LAZY | RTLD_LOCAL)
      assertNotNull(s"dlopen of ${soFile} failed", handle)

      try {
        val symbol = "strlen"
        val symbolC = toCString(symbol)

        val cFunc = dlsym(handle, symbolC)
        assertNotNull(s"dlsym lookup of '${symbol}' failed", cFunc)

        // Have symbol, does it function (sic)?
        type StringLengthFn = CFuncPtr1[CString, Int]
        val func: StringLengthFn = CFuncPtr.fromPtr[StringLengthFn](cFunc)

        assertEquals(
          s"executing symbol '${symbol}' failed",
          symbol.length(),
          func(symbolC)
        )

        val missingSymbol = "NOT_IN_LIBC"

        val func2 = dlsym(handle, toCString(missingSymbol))
        assertNull(s"dlsym lookup of ${symbol} should have failed", func2)

        val msg = fromCString(dlerror())
        // It is always chancy trying to match exact text. Go for suffix here.
        assertTrue(
          s"dlerror returned msg: |${msg}|",
          msg.endsWith(s"undefined symbol: ${missingSymbol}")
        )
      } finally {
        dlclose(handle)
      }
    }
  }

  @Test def dlfcnOpensAndObtainsSymbolAddressMacOs(): Unit = {
    if (isMac) Zone { implicit z =>
      val soFile = "/usr/lib/libSystem.dylib"

      val handle = dlopen(toCString(soFile), RTLD_LAZY | RTLD_LOCAL)
      assertNotNull(s"dlopen of ${soFile} failed", handle)

      try {
        val symbol = "strlen"
        val symbolC = toCString(symbol)

        val cFunc = dlsym(handle, symbolC)
        assertNotNull(s"dlsym lookup of '${symbol}' failed", cFunc)

        // Have symbol, does it function (sic)?
        type StringLengthFn = CFuncPtr1[CString, Int]
        val func: StringLengthFn = CFuncPtr.fromPtr[StringLengthFn](cFunc)

        assertEquals(
          s"executing symbol '${symbol}' failed",
          symbol.length(),
          func(symbolC)
        )

        val missingSymbol = "NOT_IN_LIBC"

        val func2 = dlsym(handle, toCString(missingSymbol))
        assertNull(s"dlsym lookup of ${symbol} should have failed", func2)

        val msg = fromCString(dlerror())
        // It is always chancy trying to match exact text. Go for suffix here.
        assertTrue(
          s"dlerror returned msg: |${msg}|",
          msg.endsWith(s"${missingSymbol}): symbol not found")
        )
      } finally {
        dlclose(handle)
      }
    }
  }
}
