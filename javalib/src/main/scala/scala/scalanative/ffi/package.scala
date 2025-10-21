package scala.scalanative

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.meta.LinktimeInfo.isWindows

package object ffi {
  def zlib: zlib = zlibPlatformCompat.instance
}
