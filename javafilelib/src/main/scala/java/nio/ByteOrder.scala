package java.nio

import scala.scalanative.runtime.Platform

// Ported from Scala.js

final class ByteOrder private (name: String) {
  override def toString(): String = name
}

object ByteOrder {
  val BIG_ENDIAN: ByteOrder    = new ByteOrder("BIG_ENDIAN")
  val LITTLE_ENDIAN: ByteOrder = new ByteOrder("LITTLE_ENDIAN")

  def nativeOrder(): ByteOrder = {
    if (Platform.littleEndian()) LITTLE_ENDIAN
    else BIG_ENDIAN
  }
}
