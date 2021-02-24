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

  def getInstance(algorithm: String): MessageDigest = {
    val impl = algorithm.toUpperCase() match {
      case "MD5" => MD5Impl
      case "SHA-1" => SHA1Impl
      case "SHA-224" => SHA224Impl
      case "SHA-256" => SHA256Impl
      case "SHA-384" => SHA384Impl
      case "SHA-512" => SHA512Impl
      case _ => throw new NoSuchAlgorithmException(s"$algorithm MessageDigest not available")
    }
    new CryptoMessageDigest(algorithm, impl)
  }
}

@link("crypto")
@extern
private object crypto {
  def MD5_Init(c: Ptr[Byte]): CInt                                = extern
  def MD5_Update(c: Ptr[Byte], data: Ptr[Byte], len: CSize): CInt = extern
  def MD5_Final(md: CString, c: Ptr[Byte]): CInt                  = extern

  def SHA1_Init(c: Ptr[Byte]): CInt                                = extern
  def SHA1_Update(c: Ptr[Byte], data: Ptr[Byte], len: CSize): CInt = extern
  def SHA1_Final(md: CString, c: Ptr[Byte]): CInt                  = extern

  def SHA224_Init(c: Ptr[Byte]): CInt                                = extern
  def SHA224_Update(c: Ptr[Byte], data: Ptr[Byte], len: CSize): CInt = extern
  def SHA224_Final(md: CString, c: Ptr[Byte]): CInt                  = extern

  def SHA256_Init(c: Ptr[Byte]): CInt                                = extern
  def SHA256_Update(c: Ptr[Byte], data: Ptr[Byte], len: CSize): CInt = extern
  def SHA256_Final(md: CString, c: Ptr[Byte]): CInt                  = extern

  def SHA384_Init(c: Ptr[Byte]): CInt                                = extern
  def SHA384_Update(c: Ptr[Byte], data: Ptr[Byte], len: CSize): CInt = extern
  def SHA384_Final(md: CString, c: Ptr[Byte]): CInt                  = extern

  def SHA512_Init(c: Ptr[Byte]): CInt                                = extern
  def SHA512_Update(c: Ptr[Byte], data: Ptr[Byte], len: CSize): CInt = extern
  def SHA512_Final(md: CString, c: Ptr[Byte]): CInt                  = extern
}

private abstract class AlgoImpl {
  def Init(c: Ptr[Byte]): CInt
  def Update(c: Ptr[Byte], data: Ptr[Byte], len: CSize): CInt = extern
  def Final(res: Ptr[Byte], c: Ptr[Byte]): CInt
  def CTXSize: Int
  def digestLength: Int
}
private object MD5Impl extends AlgoImpl {
  override def Init(c: Ptr[Byte]): CInt = crypto.MD5_Init(c)
  override def Update(c: Ptr[Byte], data: Ptr[Byte], len: CSize): CInt = crypto.MD5_Update(c,data,len)
  override def Final(res: Ptr[Byte], c: Ptr[Byte]): CInt = crypto.MD5_Final(res, c)
  override def CTXSize: Int = 92
  override def digestLength: Int = 16
}
private object SHA1Impl extends AlgoImpl {
  override def Init(c: Ptr[Byte]): CInt = crypto.SHA1_Init(c)
  override def Update(c: Ptr[Byte], data: Ptr[Byte], len: CSize): CInt = crypto.SHA1_Update(c,data,len)
  override def Final(res: Ptr[Byte], c: Ptr[Byte]): CInt = crypto.SHA1_Final(res, c)
  override def CTXSize: Int = 96
  override def digestLength: Int = 20
}
private object SHA224Impl extends AlgoImpl {
  override def Init(c: Ptr[Byte]): CInt = crypto.SHA224_Init(c)
  override def Update(c: Ptr[Byte], data: Ptr[Byte], len: CSize): CInt = crypto.SHA224_Update(c,data,len)
  override def Final(res: Ptr[Byte], c: Ptr[Byte]): CInt = crypto.SHA224_Final(res, c)
  override def CTXSize: Int = 112
  override def digestLength: Int = 28
}
private object SHA256Impl extends AlgoImpl {
  override def Init(c: Ptr[Byte]): CInt = crypto.SHA256_Init(c)
  override def Update(c: Ptr[Byte], data: Ptr[Byte], len: CSize): CInt = crypto.SHA256_Update(c,data,len)
  override def Final(res: Ptr[Byte], c: Ptr[Byte]): CInt = crypto.SHA256_Final(res, c)
  override def CTXSize: Int = 112
  override def digestLength: Int = 32
}
private object SHA384Impl extends AlgoImpl {
  override def Init(c: Ptr[Byte]): CInt = crypto.SHA384_Init(c)
  override def Update(c: Ptr[Byte], data: Ptr[Byte], len: CSize): CInt = crypto.SHA384_Update(c,data,len)
  override def Final(res: Ptr[Byte], c: Ptr[Byte]): CInt = crypto.SHA384_Final(res, c)
  override def CTXSize: Int = 216
  override def digestLength: Int = 48
}
private object SHA512Impl extends AlgoImpl {
  override def Init(c: Ptr[Byte]): CInt = crypto.SHA512_Init(c)
  override def Update(c: Ptr[Byte], data: Ptr[Byte], len: CSize): CInt = crypto.SHA512_Update(c,data,len)
  override def Final(res: Ptr[Byte], c: Ptr[Byte]): CInt = crypto.SHA512_Final(res, c)
  override def CTXSize: Int = 216
  override def digestLength: Int = 64
}

private final class CryptoMessageDigest(algorithm: String, algoImpl: AlgoImpl)
    extends MessageDigest(algorithm) {
  // Array with length equals to sizeof(Algo_CTX)
  private val c = new Array[Byte](algoImpl.CTXSize).asInstanceOf[ByteArray].at(0)
  engineReset()

  override def engineGetDigestLength(): Int = algoImpl.digestLength
  override def engineDigest(): Array[Byte] = {
    val result = new Array[Byte](algoImpl.digestLength)
    algoImpl.Final(result.asInstanceOf[ByteArray].at(0), c)
    engineReset()
    result
  }
  override def engineReset(): Unit = {
    algoImpl.Init(c)
  }
  override def engineUpdate(input: Byte): Unit = {
    val buf = stackalloc[Byte]
    !buf = input
    algoImpl.Update(c, buf, 1.toULong)
  }
  override def engineUpdate(input: Array[Byte], offset: Int, len: Int): Unit = {
    if (offset < 0 || len < 0 || offset + len > input.length) {
      throw new IndexOutOfBoundsException
    }
    if (len > 0) {
      algoImpl.Update(c, input.asInstanceOf[ByteArray].at(offset), len.toULong)
    }
  }
}
