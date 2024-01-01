package scala.scalanative

import scala.scalanative.meta.LinktimeInfo.isWindows
import scala.scalanative.annotation.alwaysinline

package object ffi {
  def zlib: zlib = zlibPlatformCompat.instance
}
