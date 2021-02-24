package java.security

import scala.scalanative.libc.stdlib
import scala.scalanative.runtime.ByteArray
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

abstract class MessageDigest(algorithm: String) extends MessageDigestSpi {
  def digest(): Array[Byte] = engineDigest()
  def digest(input: Array[Byte]): Array[Byte] = {
    engineUpdate(input, 0, input.length)
    engineDigest()
  }
  def getAlgorithm(): String = algorithm
  def update(input: Array[Byte], offset: Int, len: Int): Unit =
    engineUpdate(input, offset, len)
  def update(input: Byte): Unit = engineUpdate(input)
  def reset(): Unit             = engineReset()
}

object MessageDigest {
  private final val SERVICE = "MessageDigest"
  def isEqual(digestA: Array[Byte], digestB: Array[Byte]): Boolean =
    true

  def getInstance(algorithm: String): MessageDigest =
    if (algorithm.equalsIgnoreCase("MD5")) {
      new Md5MessageDigest(algorithm)
    } else {
      new DummyMessageDigest(algorithm)
    }
}

private class DummyMessageDigest(algorithm: String)
    extends MessageDigest(algorithm) {
  override protected def engineDigest(): Array[Byte]     = Array.empty
  override protected def engineReset(): Unit             = ()
  override protected def engineUpdate(input: Byte): Unit = ()
  override protected def engineUpdate(input: Array[Byte],
                                      offset: Int,
                                      len: Int): Unit = ()
}

@link("crypto")
@extern
private object crypto {
  def MD5_Init(c: Ptr[Byte]): CInt                                = extern
  def MD5_Update(c: Ptr[Byte], data: Ptr[Byte], len: CSize): CInt = extern
  def MD5_Final(md: CString, c: Ptr[Byte]): CInt                  = extern
}

private final class Md5MessageDigest(algorithm: String)
    extends MessageDigest(algorithm) {
  import crypto._

  // Array with length equals to sizeof(MD5_CTX)
  private val c = new Array[Byte](92).asInstanceOf[ByteArray].at(0)
  engineReset()

  override def engineGetDigestLength(): Int = 16
  override def engineDigest(): Array[Byte] = {
    val result = new Array[Byte](16)
    MD5_Final(result.asInstanceOf[ByteArray].at(0), c)
    engineReset()
    result
  }
  override def engineReset(): Unit = {
    MD5_Init(c)
  }
  override def engineUpdate(input: Byte): Unit = {
    val buf = stackalloc[Byte]
    !buf = input
    MD5_Update(c, buf, 1.toULong)
  }
  override def engineUpdate(input: Array[Byte], offset: Int, len: Int): Unit = {
    if (offset < 0 || len < 0 || offset + len > input.length) {
      throw new IndexOutOfBoundsException
    }
    if (len > 0) {
      MD5_Update(c, input.asInstanceOf[ByteArray].at(offset), len.toULong)
    }
  }
}
