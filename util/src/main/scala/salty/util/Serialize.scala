package salty.util

import java.nio.ByteBuffer

trait Serialize[T] {
  def apply(t: T): Serialize.Result
}
object Serialize {
  sealed abstract class Result {
    final def size: scala.Int = this match {
      case None               => 0
      case _: Byte            => 1
      case _: Short | _: Char => 2
      case _: Int             => 4
      case _: Long            => 8
      case Bytes(bytes)       => bytes.length
      case Sequence(xs @ _*)  => xs.map(_.size).sum
    }
    final def build: ByteBuffer = {
      val buffer = ByteBuffer.allocate(size)
      def loop(res: Result): Unit = res match {
        case None              => ()
        case Byte(v)           => buffer.put(v)
        case Short(v)          => buffer.putShort(v)
        case Char(v)           => buffer.putChar(v)
        case Int(v)            => buffer.putInt(v)
        case Long(v)           => buffer.putLong(v)
        case Bytes(bytes)      => buffer.put(bytes)
        case Sequence(xs @ _*) => xs.foreach(loop)
      }
      loop(this)
      buffer
    }
  }
  final case object None extends Result
  implicit object Bytes extends Serialize[Array[scala.Byte]]
  final case class Byte(value: scala.Byte) extends Result
  implicit object Byte extends Serialize[scala.Byte]
  final case class Short(value: scala.Short) extends Result
  implicit object Short extends Serialize[scala.Short]
  final case class Char(value: scala.Char) extends Result
  implicit object Char extends Serialize[scala.Char]
  final case class Int(value: scala.Int) extends Result
  implicit object Int extends Serialize[scala.Int]
  final case class Long(value: scala.Long) extends Result
  implicit object Long extends Serialize[scala.Long]
  final case class Bytes(bytes: Array[scala.Byte]) extends Result
  final case class Sequence(xs: Result*) extends Result

  def apply[T](f: T => Result): Serialize[T] =
    new Serialize[T] { def apply(input: T): Result = f(input) }
  implicit def toResult[T: Serialize](t: T): Result =
    implicitly[Serialize[T]].apply(t)

  import Serialize.{Sequence => s}

  implicit def serializeOption[T: Serialize]: Serialize[Option[T]] = Serialize {
    case scala.None    => s(0.toByte)
    case scala.Some(v) => s(1.toByte, v)
  }
  implicit def serializeSeq[T: Serialize]: Serialize[Seq[T]] = Serialize { seq =>
    s(seq.length, s(seq.map(el => (el: Serialize.Result)): _*))
  }
  implicit val serializeString: Serialize[String] = Serialize(_.toSeq)
  implicit val serializeBoolean: Serialize[Boolean] = Serialize { b => if (b) 1.toByte else 0.toByte }
}
