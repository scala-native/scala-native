package salty

import java.nio.ByteBuffer

package object util {
  def show[T: Show](t: T): String = (t: Show.Result).build
  def serialize[T: Serialize](t: T): ByteBuffer = (t: Serialize.Result).build
}
