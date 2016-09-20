package scala.scalanative
package native

@extern
class assert {

  def __assert_fail(assertion: CString,
                    file: CString,
                    line: CUnsignedInt,
                    function: CString): Unit = extern
  def __assert_perror_fail(errnum: CInt,
                           file: CString,
                           line: CUnsignedInt,
                           function: CString): Unit                 = extern
  def __assert(assertion: CString, file: CString, line: CInt): Unit = extern

}
