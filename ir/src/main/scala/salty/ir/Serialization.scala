package salty.ir

import java.nio.ByteBuffer

object Serialization {
  implicit class RichPut(val bb: ByteBuffer) extends AnyVal
  implicit class RichGet(val bb: ByteBuffer) extends AnyVal
}
