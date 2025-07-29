package scala.scalanative.runtime

import scala.scalanative.meta.LinktimeInfo.{isWindows, sourceLevelDebuging}
import scalanative.unsigned._
import scala.scalanative.unsafe._
import scala.scalanative.runtime.ffi._

object SymbolFormatter {

  /* Async-signal-safe stack trace symbol formatter function.
   * Uses only async-signal-safe methods to allow use in a signal handler.
   */
  def asyncSafeFromSymbol(
      sym: CString,
      classNameOut: CString,
      methodNameOut: CString,
      fileNameOut: CString, // Windows only
      lineOut: Ptr[Int] // Windows only
  ): Unit = {

    val len = strlen(sym)
    var pos = 0
    classNameOut(0) = 0.toByte
    methodNameOut(0) = 0.toByte

    def readSymbol(): Boolean = {
      // On Windows symbol names are different than on Unix platforms.
      // Due to differences in implementation between WinDbg and libUnwind used
      // on each platform, symbols on Windows do not contain '_' prefix.
      // When debug metadata is generated and there is no symbols (LTO) then
      // returned sybmols have form `fqcn.methodName:(file:line)` (linkage name from MetadataCodeGen)
      def mayHaveLinkageSymbol =
        isWindows && sourceLevelDebuging.generateFunctionSourcePositions
      // If symbol is not linkage symbol when it would skip Windows specific prefix allowing to continue unix-like reading
      val head = read()
      // unlikekly that package name would start with upper case 'S'
      if (mayHaveLinkageSymbol && head != 'S')
        readLinkageSymbol()
      else if (head == 'S') readGlobal() // Windows
      else if (head == '_' && read() == 'S') readGlobal() // Unix
      else false
    }

    def readGlobal(): Boolean = read() match {
      case 'M' => readIdent(classNameOut) && readSig()
      case _   => false
    }

    def readSig(): Boolean = read() match {
      case 'R' =>
        strcpy(methodNameOut, c"<init>")
        true
      case 'D' | 'P' | 'C' | 'G' =>
        readIdent(methodNameOut)
      case 'K' =>
        readSig()
      case _ =>
        false
    }

    def readIdent(output: CString): Boolean = {
      val n = readNumber()
      (n > 0 && inBounds(pos) && inBounds(pos + n)) && {
        strncpy(output, sym + pos, Intrinsics.castIntToRawSize(n))
        pos += n
        true
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

    // Windows only
    def readLinkageSymbol(): Boolean = {
      fileNameOut(0) = 0.toByte
      val location = strchr(sym, ':')
      if (location == null) {
        // No location part, simplifield
        val methodNamePos = strrchr(sym, '.')
        if (methodNamePos != null) {
          strncpy(classNameOut, sym, toRawSize(methodNamePos - sym))
          strcpy(methodNameOut, methodNamePos + 1)
          true
        } else false
      } else {
        val lineSeparator = strrchr(location, ':')
        val fileName = strrchr(location, '\\')
        val fileOffset = 2 // ':('
        if (lineSeparator != null) {
          // skip ':(', take until line number ':num)'
          if (fileName != null) {
            strncpy(
              fileNameOut,
              fileName + 1,
              toRawSize(strlen(fileName) - strlen(lineSeparator) - 1.toUSize)
            )
          } else {
            strncpy(
              fileNameOut,
              location + fileOffset,
              toRawSize(
                strlen(location) - strlen(lineSeparator) - fileOffset.toUSize
              )
            )
          }
          pos = (lineSeparator - sym).toInt + 1
          !lineOut = readNumber()
        } else if (fileName != null) strcpy(fileNameOut, fileName + 1)
        else strcpy(fileNameOut, location + fileOffset)

        // Find methodStart, we cannot use strrchr becouse there is no last index limitter and filename would contain extension
        var methodStart = sym
        while ({
          val nextDot = strchr(methodStart, '.')
          val isBeforeLocation =
            nextDot != null && (nextDot.toLong < location.toLong)
          if (isBeforeLocation) methodStart = nextDot + 1
          isBeforeLocation
        }) ()
        if (methodStart != null) {
          strncpy(
            methodNameOut,
            methodStart,
            toRawSize(location - methodStart)
          )
        }
        if (methodStart == sym) strcpy(classNameOut, c"<none>")
        else strncpy(classNameOut, sym, toRawSize(methodStart - sym - 1))
        true
      }
    }

    if (!readSymbol()) {
      strcpy(classNameOut, c"<none>")
      strcpy(methodNameOut, sym)
    }
  }
}
