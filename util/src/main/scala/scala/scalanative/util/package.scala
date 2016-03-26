package scala.scalanative

import java.nio.ByteBuffer

package object util {
  implicit class sh(val ctx: StringContext) extends AnyVal {
    def sh(args: Show.Result*) =
      Show.Interpolated(ctx.parts, args)
  }
  object sh {
    def apply(res: Show.Result) = res
  }

  final case object UnreachableException extends Exception
  def unreachable = throw UnreachableException

  final case class UnsupportedException(msg: String) extends Exception(msg)
  def unsupported(v: Any = "") =
    throw UnsupportedException(s"$v (${v.getClass})")
}
