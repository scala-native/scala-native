package scala.scalanative
package posix

import scala.scalanative.unsafe.*
import scala.scalanative.unsigned.{UByte, UInt, ULong, UShort}

/** POSIX inttypes.h for Scala
 *
 *  The <inttypes.h> header shall include the <stdint.h> header.
 */
@extern object inttypes extends inttypes

@extern trait inttypes extends libc.inttypes {
  /* These should be in POSIX stdint too. They are optional in C
   * stdint so they could be inherited from there. They are here
   * now so leaving them as is.
   *
   * There is also the consideration that importing
   * both inttypes and stdint could create a conflict
   * so it is unclear the path forward for types in general.
   * this is also true for any other API that includes
   * them as a type for convenience to the end user.
   */
  type uint8_t = UByte
  type uint16_t = UShort
  type uint32_t = UInt
  type uint64_t = ULong
}
