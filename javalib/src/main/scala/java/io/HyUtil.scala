package java.io
import scalanative.native._
import java.nio.charset._

//Implementation of the few used methods from
// org.hapache.harmony.luni.util.Util

object HyUtil {

  private val defaultEncoding: String = {
    var encoding: String = fromCString(CFile.getOsEncoding())
    if (encoding != null) {
      try {
        "".getBytes(encoding)
      } catch {
        case t: Throwable =>
          encoding = null
      }
    }
    encoding
  }

  def getBytes(name: String): Array[Byte] = {
    if (defaultEncoding != null) {
      try {
        return name.getBytes(defaultEncoding)
      } catch {
        case e: UnsupportedEncodingException =>
      }
    }
    return name.getBytes()
  }

  def getUTF8Bytes(name: String): Array[Byte] =
    try {
      return name.getBytes("UTF-8")
    } catch {
      case e: UnsupportedEncodingException => return getBytes(name);
    }

  def toString(bytes: Array[Byte]): String = {
    if (defaultEncoding != null) {
      try {
        return new String(bytes, 0, bytes.length, defaultEncoding)
      } catch {
        case e: UnsupportedEncodingException =>
      }
    }
    return new String(bytes, 0, bytes.length)
  }

  def toString(bytes: Array[Byte], offset: Int, length: Int): String = {
    if (defaultEncoding != null) {
      try {
        return new String(bytes, offset, length, defaultEncoding)
      } catch {
        case e: UnsupportedEncodingException =>
      }
    }
    return new String(bytes, offset, length)
  }

  def toUTF8String(bytes: Array[Byte]): String =
    toUTF8String(bytes, 0, bytes.length)

  def toUTF8String(bytes: Array[Byte], offset: Int, length: Int): String =
    try {
      new String(bytes, offset, length, "UTF-8");
    } catch {
      case e: UnsupportedEncodingException => toString(bytes, offset, length)
    }
}
