package scala.scalanative.nio.fs.windows

import scalanative.annotation.alwaysinline
import java.nio.file.InvalidPathException
import scala.annotation.tailrec

object WindowsPathParser {
  import WindowsPath.PathType._

  def apply(rawPath: String)(implicit fs: WindowsFileSystem): WindowsPath = {
    @alwaysinline
    def charAtIdx(n: Int, pred: Char => Boolean): Boolean = {
      rawPath.size > n && pred(rawPath.charAt(n))
    }

    val (tpe, root) = if (charAtIdx(0, isSlash)) {
      if (charAtIdx(1, isSlash))
        UNC -> Some(getUNCRoot(rawPath))
      else if (charAtIdx(1, isASCIILetter) && charAtIdx(2, _ == ':'))
        // URI specific, absolute path starts with / followed by absolute path
        Absolute -> Some(rawPath.substring(1, 4))
      else
        DriveRelative -> None
    } else if (charAtIdx(0, isASCIILetter) && charAtIdx(1, _ == ':')) {
      if (charAtIdx(2, isSlash))
        Absolute -> Some(rawPath.substring(0, 3))
      else
        DirectoryRelative -> Some(rawPath.substring(0, 2))
    } else Relative -> None

    val relativePath = root
      .map(r => rawPath.substring(r.length))
      .getOrElse(rawPath)

    val segments = pathSegments(relativePath)

    new WindowsPath(tpe, root.map(fixSlashes), segments)
  }

  private def fixSlashes(str: String): String = str.replace('/', '\\')

  private def isSlash(c: Char): Boolean = {
    c == '\\' || c == '/'
  }

  private def isASCIILetter(c: Char): Boolean = {
    (c >= 'a' && c <= 'z') ||
    (c >= 'A' && c <= 'Z')
  }

  private def getUNCRoot(rawPath: String): String = {
    val hostStartIdx = 2
    val hostEndIdx = rawPath.indexWhere(isSlash, hostStartIdx)
    if (hostEndIdx < 0) {
      throw new InvalidPathException(rawPath, "UNC path is missing host")
    }
    val shareStartIdx = hostEndIdx + 1
    val shareEndIdx = rawPath.indexWhere(isSlash, shareStartIdx)
    if (shareEndIdx < 0) {
      throw new InvalidPathException(rawPath, "UNC path is missing share name")
    }

    // Host can contain `?` or `.` used to indicate DOS device path
    val host = substringAndCheck(rawPath, hostStartIdx, hostEndIdx, "?")
    // We also accept `:` after drive letter
    val share = substringAndCheck(rawPath, shareStartIdx, shareEndIdx, ":")

    raw"""\\$host\$share\"""
  }

  private def pathSegments(path: String): List[String] = {
    @tailrec
    def loop(acc: List[String], idx: Int): List[String] = {
      if (idx >= path.length()) acc.reverse
      else {
        val endIdx = path.indexWhere(isSlash, idx) match {
          case -1     => path.length()
          case endIdx => endIdx
        }
        val segment = substringAndCheck(path, idx, endIdx)
        val fromNext = endIdx + 1

        if (segment.isEmpty)
          loop(acc, fromNext)
        else {
          val lastChar = segment.last
          if (lastChar.isWhitespace) {
            throw new InvalidPathException(
              path,
              s"Trailing char `$lastChar`",
              endIdx - 1
            )
          }
          loop(segment :: acc, fromNext)
        }
      }
    }
    loop(Nil, 0)
  }

  private final val pathReservedChars = "<>:\"/|?*"

  private def substringAndCheck(
      path: String,
      from: Int,
      to: Int,
      allowedChars: String = ""
  ): String = {
    def isControlOrReservedChar(c: Char) = {
      // '\u0001F' - last control character (no. 31)
      c <= '\u001F' || pathReservedChars.contains(c)
    }
    def checkValidSegment(
        segment: String,
        path: String,
        segmentIdx: Int
    ): Unit = {

      val invalidCharIdx = segment.indexWhere { c =>
        isControlOrReservedChar(c) && !allowedChars.contains(c)
      }
      if (invalidCharIdx >= 0) {
        throw new InvalidPathException(
          path,
          s"Illegal character `${segment.charAt(invalidCharIdx)}` in path",
          segmentIdx + invalidCharIdx
        )
      }
    }

    val substring = path.substring(from, to)
    checkValidSegment(substring, path, from)
    substring
  }

}
