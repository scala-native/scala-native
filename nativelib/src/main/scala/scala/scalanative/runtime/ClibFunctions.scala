package scala.scalanative

import native._

/**
 * C stdlib functions needed by nativelib
 *
 * Note: clib depends on nativelib.
 */

@extern
private [scalanative] object clib_string {
  def memset(ptr: Ptr[_], i: CInt, n: CSize): CInt = extern
}

