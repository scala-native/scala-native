package scala.scalanative
package posix

import scalanative.unsafe._

import scalanative.posix.sys.types._

/** POSIX monetary.h for Scala
 *
 *  The Open Group Base Specifications
 *  [[https://pubs.opengroup.org/onlinepubs/9699919799 Issue 7, 2018]] edition.
 *
 *  POSIX defines strfmon() and strfmon_l() using the "..." form of C variable
 *  arguments. Scala Native "supports native interoperability with C’s variadic
 *  argument list type (i.e. va_list), but not ... varargs".
 *
 *  This implementation supports up to 10 items in the variable arguments to
 *  strfmon() and strfmon_1().
 */

object monetary {
  type locale_t = locale.locale_t
  private final val maxOutArgs = 10

  def strfmon(
      str: CString,
      max: size_t,
      format: CString,
      argsIn: Double*
  ): ssize_t = Zone { implicit z =>
    val argsOut = new Array[Double](maxOutArgs)
    val limit = Math.min(argsIn.size, maxOutArgs)

    for (j <- 0 until limit)
      argsOut(j) = argsIn(j)

    // format: off
    val nFormatted = monetaryExtern.strfmon_10(str, max, format,
                                                 argsOut(0), argsOut(1),
                                                 argsOut(2), argsOut(3),
                                                 argsOut(4), argsOut(5),
                                                 argsOut(6), argsOut(7),
                                                 argsOut(8), argsOut(9)
                                                 )
    // format: on

    nFormatted
  }

  def strfmon_l(
      str: CString,
      max: size_t,
      locale: locale_t,
      format: CString,
      argsIn: Double*
  ): ssize_t = Zone { implicit z =>
    val argsOut = new Array[Double](maxOutArgs)
    val limit = Math.min(argsIn.size, maxOutArgs)

    for (j <- 0 until limit)
      argsOut(j) = argsIn(j)

    // format: off
    val nFormatted = monetaryExtern.strfmon_l_10(str, max, locale, format, 
                                                 argsOut(0), argsOut(1),
                                                 argsOut(2), argsOut(3),
                                                 argsOut(4), argsOut(5),
                                                 argsOut(6), argsOut(7),
                                                 argsOut(8), argsOut(9)
                                                 )
    // format: on

    nFormatted
  }

}

@extern
object monetaryExtern {

  // format: off
  @name("scalanative_strfmon_10")
  def strfmon_10(
      str: CString,
      max: size_t,
      format: CString,
      arg0: Double, arg1: Double,
      arg2: Double, arg3: Double,
      arg4: Double, arg5: Double,
      arg6: Double, arg7: Double,
      arg8: Double, arg9: Double
  ): ssize_t = extern
  // format: on

  // format: off
  @name("scalanative_strfmon_l_10")
  def strfmon_l_10(
      str: CString,
      max: size_t,
      locale: monetary.locale_t,
      format: CString,
      arg0: Double, arg1: Double,
      arg2: Double, arg3: Double,
      arg4: Double, arg5: Double,
      arg6: Double, arg7: Double,
      arg8: Double, arg9: Double
  ): ssize_t = extern
  // format: on
}
