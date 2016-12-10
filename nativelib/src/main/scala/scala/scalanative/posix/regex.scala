package scala.scalanative.posix

import scalanative.native._, stdlib._, stdio._

@extern
object Regex {
  def regcomp(regex: Ptr[Int], str: CString, num: Int): Int = extern
  def regexec(regex: Ptr[Int], str: CString, num: Int, ptr: Ptr[Int], num2: Int): Int = extern
}

class PosixPattern {
  val pattern: Ptr[Int]
}

object PosixRegex {
  def compileRegex(expr: String): Either[PosixRegexError, PosixPattern] = {
    val regex = malloc(sizeof[Int]).cast[Ptr[Int]]

    val retval = Regex.regcomp(regex, toCString(expr) , 0)
    if (retval == 0) Right(PosixPattern(regex))
    else Left(PosixRegexError(retval))
  }

  def executeRegex(pat: PosixPattern) = {
    ???
      //Regex.regexec(regex, toCString("123"), 0, null, 0)
  }
}
