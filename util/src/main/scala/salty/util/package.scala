package salty

import java.nio.ByteBuffer

package object util {
  def serialize[T: Serialize](t: T): ByteBuffer = (t: Serialize.Result).build

  implicit class sh(val ctx: StringContext) extends AnyVal {
    def sh(args: Show.Result*) =
      Show.Interpolated(ctx.parts, args)
  }
  object sh {
    def apply(res: Show.Result) = res
  }
}
