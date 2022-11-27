package scala.scalanative.safe

import scala.scalanative.unsigned._
import scala.scalanative.unsafe.Tag

private[scalanative] trait SafePackageCompat {

  inline def alloc[T]()(using tag: Tag[T], sz: SafeZone): T = {
    sz.alloc[T]()
  }

}
