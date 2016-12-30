package scala.scalanative
package runtime

import scala.reflect.ClassTag
import native._

sealed trait Tag[P]

object Tag {
  implicit val Unit: Tag[Unit]                    = new Tag[Unit]    {}
  implicit val Boolean: Tag[Boolean]              = new Tag[Boolean] {}
  implicit val Char: Tag[Char]                    = new Tag[Char]    {}
  implicit val Byte: Tag[Byte]                    = new Tag[Byte]    {}
  implicit val UByte: Tag[UByte]                  = new Tag[UByte]   {}
  implicit val Short: Tag[Short]                  = new Tag[Short]   {}
  implicit val UShort: Tag[UShort]                = new Tag[UShort]  {}
  implicit val Int: Tag[Int]                      = new Tag[Int]     {}
  implicit val UInt: Tag[UInt]                    = new Tag[UInt]    {}
  implicit val Long: Tag[Long]                    = new Tag[Long]    {}
  implicit val ULong: Tag[ULong]                  = new Tag[ULong]   {}
  implicit val Float: Tag[Float]                  = new Tag[Float]   {}
  implicit val Double: Tag[Double]                = new Tag[Double]  {}
  implicit def Ptr[T: Tag]: Tag[Ptr[T]]           = new Tag[Ptr[T]]  {}
  implicit def Ref[T <: AnyRef: ClassTag]: Tag[T] = new Tag[T]       {}
}
