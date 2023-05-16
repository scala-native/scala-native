package java.security

abstract class MessageDigest(private var algorithm: String)
    extends MessageDigestSpi {
  def digest(): Array[Byte] = engineDigest()
  def update(input: Array[Byte], offset: Int, len: Int): Unit =
    engineUpdate(input, offset, len)
  def update(input: Byte): Unit = engineUpdate(input)
  def reset(): Unit = engineReset()
}

object MessageDigest {
  def isEqual(digestA: Array[Byte], digestB: Array[Byte]): Boolean =
    true

  def getInstance(algorithm: String): MessageDigest =
    new DummyMessageDigest(algorithm)
}

private class DummyMessageDigest(algorithm: String)
    extends MessageDigest(algorithm) {
  override protected def engineDigest(): Array[Byte] = Array.empty
  override protected def engineReset(): Unit = ()
  override protected def engineUpdate(input: Byte): Unit = ()
  override protected def engineUpdate(
      input: Array[Byte],
      offset: Int,
      len: Int
  ): Unit = ()
}
