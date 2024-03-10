package scala.scalanative
package posix
package sys

import scalanative.unsafe._
import scalanative.unsigned._

import scalanative.meta.LinktimeInfo._

/** POSIX sys/un.h for Scala
 */

@extern
object un {
  type _108 = Nat.Digit3[Nat._1, Nat._0, Nat._8]

  type sa_family_t = socket.sa_family_t

  /* _Static_assert guard code in the un.c assures the SN sockaddr_un is
   * >= the  corresponding Unix operating system version.
   * 108 for sun_path is the Linux & Windows value. It is >= macOS 104 bytes.
   */

  type sockaddr_un = CStruct2[
    sa_family_t, // sun_family, sun_len is synthesized if needed
    CArray[CChar, _108] // sun_path
  ]
}

/** Allow using C names to access socket_un structure fields.
 */
object unOps {
  import un._
  import posix.inttypes.uint8_t

  @resolvedAtLinktime
  def useSinXLen = !isLinux &&
    (isMac || isFreeBSD || isOpenBSD)

  implicit class sockaddr_unOps(val ptr: Ptr[sockaddr_un]) extends AnyVal {
    def sun_len: uint8_t = if (!useSinXLen) {
      sizeof[sockaddr_un].toUByte // length is synthesized
    } else {
      ptr._1.toUByte
    }

    def sun_family: sa_family_t = if (!useSinXLen) {
      ptr._1
    } else {
      (ptr._1 >>> 8).toUByte
    }

    def sun_path: CArray[CChar, _108] = ptr._2

    def sun_len_=(v: uint8_t): Unit = if (useSinXLen) {
      ptr._1 = ((ptr._1 & 0xff00.toUShort) + v).toUShort
    } // else silently do nothing

    def sun_family_=(v: sa_family_t): Unit =
      if (!useSinXLen) {
        ptr._1 = v
      } else {
        ptr._1 = ((v << 8) + ptr.sun_len).toUShort
      }

    def sun_path_=(v: CArray[CChar, _108]): Unit = ptr._2 = v
  }
}
