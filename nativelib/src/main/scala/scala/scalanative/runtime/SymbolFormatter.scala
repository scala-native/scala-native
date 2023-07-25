package scala.scalanative.runtime

import scala.scalanative.meta.LinktimeInfo.isWindows
import scalanative.unsigned._
import scala.scalanative.unsafe._
import scala.scalanative.runtime.libc._

object SymbolFormatter {

  /* Async-signal-safe stack trace symbol formatter function.
   * Uses only async-signal-safe methods to allow use in a signal handler.
   */
  def asyncSafeFromSymbol(
      sym: CString,
      classNameOut: CString,
      methodNameOut: CString
  ): Unit = {

    val len = strlen(sym)
    var pos = 0
    val ident =
      fromRawPtr[CChar](
        Intrinsics.stackalloc[CChar](
          Intrinsics.castIntToRawSizeUnsigned(1024)
        )
      )
    classNameOut(0) = 0.toByte
    methodNameOut(0) = 0.toByte

    def readSymbol(): Boolean = {
      // On Windows symbol names are different then on Unix platforms.
      // Due to differences in implementation between WinDbg and libUnwind used
      // on each platform, symbols on Windows do not contain '_' prefix.
      if (!isWindows) {
        if (read() != '_') {
          false
        } else if (read() != 'S') {
          false
        } else {
          readGlobal()
        }
      } else {
        if (read() != 'S') {
          false
        } else {
          readGlobal()
        }
      }
    }

    def readGlobal(): Boolean = read() match {
      case 'M' =>
        readIdent()
        if (strlen(ident) == 0.toUInt) {
          false
        } else {
          strcpy(classNameOut, ident)
          readSig()
        }
      case _ =>
        false
    }

    def readSig(): Boolean = read() match {
      case 'R' =>
        strcpy(methodNameOut, c"<init>")
        true
      case 'D' | 'P' | 'C' | 'G' =>
        readIdent()
        if (strlen(ident) == 0.toUInt) {
          false
        } else {
          strcpy(methodNameOut, ident)
          true
        }
      case 'K' =>
        readSig()
      case _ =>
        false
    }

    def readIdent(): Unit = {
      val n = readNumber()
      if (n <= 0) {
        ident(0) = 0.toByte
      } else if (!inBounds(pos) || !inBounds(pos + n)) {
        ident(0) = 0.toByte
      } else {
        var i = 0
        while (i < n) {
          ident(i) = sym(pos + i)
          i += 1
        }
        ident(i) = 0.toByte
        pos += n
      }
    }

    def readNumber(): Int = {
      val start = pos
      var number = 0
      while ('0' <= at(pos) && at(pos) <= '9') {
        number = number * 10 + (at(pos) - '0').toInt
        pos += 1
      }
      if (start == pos) {
        -1
      } else {
        number
      }
    }

    def read(): Char = {
      if (inBounds(pos)) {
        val res = sym(pos).toChar
        pos += 1
        res
      } else {
        -1.toChar
      }
    }

    def at(pos: Int): Char = {
      if (inBounds(pos)) {
        sym(pos).toChar
      } else {
        -1.toChar
      }
    }

    def inBounds(pos: Int) =
      pos >= 0 && pos < len.toLong

    if (!readSymbol()) {
      strcpy(classNameOut, c"<none>")
      strcpy(methodNameOut, sym)
    }
  }
}
