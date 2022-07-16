package scala.scalanative.posix

import scalanative.unsafe.{extern, CInt, CString}

@extern
object string {
  def strsignal(signum: CInt): CString = extern
}
