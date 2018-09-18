package java.net

import scala.scalanative.native._
import scala.scalanative.runtime.SocketHelpers
import scala.collection.mutable.ArrayBuffer

import java.util.StringTokenizer

// Ported from Apache Harmony
private[net] trait InetAddressBase {

  private[net] val wildcard =
    new Inet4Address(Array[Byte](0, 0, 0, 0), "0.0.0.0")

  def getByName(host: String): InetAddress = {

    if (host == null || host.length == 0)
      return getLoopbackAddress()

    var address: InetAddress = null
    if (isValidIPv4Address(host)) {
      val byteAddress: Array[Byte] = Array.ofDim[Byte](4)
      val parts: Array[String]     = host.split("\\.")
      val length: Int              = parts.length
      if (length == 1) {
        val value: Long = java.lang.Long.parseLong(parts(0))
        for (i <- 0.until(4)) {
          byteAddress(i) = (value >> ((3 - i) * 8)).toByte
        }
      } else {
        for (i <- 0 until length) {
          byteAddress(i) = java.lang.Integer.parseInt(parts(i)).toByte
        }
      }
      if (length == 2) {
        byteAddress(3) = byteAddress(1)
        byteAddress(1) = 0
      }
      if (length == 3) {
        byteAddress(3) = byteAddress(2)
        byteAddress(2) = 0
      }
      address = new Inet4Address(byteAddress)
    } else if (isValidIPv6Address(host)) {
      var ipAddressString = host
      if (ipAddressString.charAt(0) == '[') {
        ipAddressString =
          ipAddressString.substring(1, ipAddressString.length - 1)
      }
      val tokenizer: StringTokenizer =
        new StringTokenizer(ipAddressString, ":.%", true)
      val hexStrings            = new ArrayBuffer[String]()
      val decStrings            = new ArrayBuffer[String]()
      var scopeString: String   = null
      var token: String         = ""
      var prevToken: String     = ""
      var prevPrevToken: String = ""
      var doubleColonIndex: Int = -1
      while (tokenizer.hasMoreTokens()) {
        prevPrevToken = prevToken
        prevToken = token
        token = tokenizer.nextToken()
        if (token == ":") {
          if (prevToken == ":") {
            doubleColonIndex = hexStrings.size
          } else if (prevToken != "") {
            hexStrings.append(prevToken)
          }
        } else if (token == ".") {
          decStrings.append(prevToken)
        } else if (token == "%") {
          if (prevToken != ":" && prevToken != ".") {
            if (prevPrevToken == ":") {
              hexStrings.append(prevToken)
            } else if (prevPrevToken == ".") {
              decStrings.append(prevToken)
            }
          }
          val buf: StringBuilder = new StringBuilder()
          while (tokenizer.hasMoreTokens()) buf.append(tokenizer.nextToken())
          scopeString = buf.toString
        }
      }
      if (prevToken == ":") {
        if (token == ":") {
          doubleColonIndex = hexStrings.size
        } else {
          hexStrings.append(token)
        }
      } else if (prevToken == ".") {
        decStrings.append(token)
      }
      var hexStringsLength: Int = 8
      if (decStrings.size > 0) {
        hexStringsLength -= 2
      }
      if (doubleColonIndex != -1) {
        val numberToInsert: Int = hexStringsLength - hexStrings.size
        for (i <- 0 until numberToInsert) {
          hexStrings.insert(doubleColonIndex, "0")
        }
      }
      val ipByteArray: Array[Byte] = Array.ofDim[Byte](16)
      for (i <- 0 until hexStrings.size) {
        convertToBytes(hexStrings(i), ipByteArray, i * 2)
      }
      for (i <- 0 until decStrings.size) {
        ipByteArray(i + 12) =
          (java.lang.Integer.parseInt(decStrings(i)) & 255).toByte
      }
      var ipV4 = true
      if (ipByteArray.take(10).exists(_ != 0)) {
        ipV4 = false
      }
      if (ipByteArray(10) != -1 || ipByteArray(11) != -1) {
        ipV4 = false
      }
      if (ipV4) {
        val ipv4ByteArray = new Array[Byte](4)
        for (i <- 0.until(4)) {
          ipv4ByteArray(i) = ipByteArray(i + 12)
        }
        address = InetAddress.getByAddress(ipv4ByteArray)
      } else {
        var scopeId: Int = 0
        if (scopeString != null) {
          try {
            scopeId = java.lang.Integer.parseInt(scopeString)
          } catch {
            case e: Exception => {}
          }
        }
        address = Inet6Address.getByAddress(null, ipByteArray, scopeId)
      }
    } else {
      val ip = SocketHelpers.hostToIp(host).getOrElse {
        throw new UnknownHostException(
          "No IP address could be found for the specified host: " + host)
      }
      if (isValidIPv4Address(ip))
        address = new Inet4Address(byteArrayFromIPString(ip), host)
      else if (isValidIPv6Address(ip))
        address = new Inet6Address(byteArrayFromIPString(ip), host)
      else
        throw new UnknownHostException("Malformed IP: " + ip)
    }
    address
  }

  def getAllByName(host: String): Array[InetAddress] = {
    if (host == null || host.length == 0)
      return Array[InetAddress](getLoopbackAddress())

    if (isValidIPv4Address(host))
      return Array[InetAddress](new Inet4Address(byteArrayFromIPString(host)))

    if (isValidIPv6Address(host))
      return Array[InetAddress](new Inet6Address(byteArrayFromIPString(host)))

    val ips: Array[String] = SocketHelpers.hostToIpArray(host)
    if (ips.isEmpty) {
      throw new UnknownHostException(
        "No IP address could be found for the specified host: " + host)
    }

    ips.map(ip => {
      if (isValidIPv4Address(ip)) {
        new Inet4Address(byteArrayFromIPString(ip), host)
      } else {
        new Inet6Address(byteArrayFromIPString(ip), host)
      }
    })
  }

  def getByAddress(addr: Array[Byte]): InetAddress =
    getByAddress(null, addr)

  def getByAddress(host: String, addr: Array[Byte]): InetAddress = {
    if (addr.length == 4)
      return new Inet4Address(addr.clone, host)
    else if (addr.length == 16)
      return new Inet6Address(addr.clone, host)
    else
      throw new UnknownHostException(
        "IP address is of illegal length: " + addr.length)
  }

  private def isValidIPv4Address(addr: String): Boolean = {
    if (!addr.matches("[0-9\\.]*")) {
      return false
    }

    val parts = addr.split("\\.")
    if (parts.length > 4) return false

    if (parts.length == 1) {
      val longValue = parts(0).toLong
      longValue >= 0 && longValue <= 0xFFFFFFFFL
    } else {
      parts.forall(part => {
        part.length <= 3 || Integer.parseInt(part) <= 255
      })
    }
  }

  private[net] def isValidIPv6Address(ipAddress: String): Boolean = {
    val length: Int          = ipAddress.length
    var doubleColon: Boolean = false
    var numberOfColons: Int  = 0
    var numberOfPeriods: Int = 0
    var numberOfPercent: Int = 0
    var word: String         = ""
    var c: Char              = 0
    var prevChar: Char       = 0
    // offset for [] IP addresses
    var offset: Int = 0
    if (length < 2) {
      return false
    }
    for (i <- 0 until length) {
      prevChar = c
      c = ipAddress.charAt(i)
      c match {
        // case for an open bracket [x:x:x:...x]
        case '[' =>
          if (i != 0) {
            // must be first character
            return false
          }
          if (ipAddress.charAt(length - 1) != ']') {
            // must have a close ]
            return false
          }
          offset = 1
          if (length < 4) {
            return false
          }
        // case for a closed bracket at end of IP [x:x:x:...x]
        case ']' =>
          if (i != (length - 1)) {
            // must be last character
            return false
          }
          if (ipAddress.charAt(0) != '[') {
            // must have a open [
            return false
          }
        // case for the last 32-bits represented as IPv4 x:x:x:x:x:x:d.d.d.d
        case '.' =>
          numberOfPeriods += 1
          if (numberOfPeriods > 3) {
            return false
          }
          if (!isValidIP4Word(word)) {
            return false
          }
          if (numberOfColons != 6 && !doubleColon) {
            return false
          }
          // IPv4 ending, otherwise 7 :'s is bad
          if (numberOfColons == 7 && ipAddress.charAt(0 + offset) != ':' &&
              ipAddress.charAt(1 + offset) != ':') {
            return false
          }
          word = ""
        // a special case ::1:2:3:4:5:d.d.d.d allows 7 colons with an
        case ':' =>
          numberOfColons += 1
          if (numberOfColons > 7) {
            return false
          }
          if (numberOfPeriods > 0) {
            return false
          }
          if (prevChar == ':') {
            if (doubleColon) {
              return false
            }
            doubleColon = true
          }
          word = ""
        case '%' =>
          if (numberOfColons == 0) {
            return false
          }
          numberOfPercent += 1
          // validate that the stuff after the % is valid
          if ((i + 1) >= length) {
            // in this case the percent is there but no number is available
            return false
          }
          try Integer.parseInt(ipAddress.substring(i + 1))
          catch {
            case e: NumberFormatException => return false
          }
        case _ =>
          if (numberOfPercent == 0) {
            if (word.length > 3) {
              return false
            }
            if (!isValidHexChar(c)) {
              return false
            }
          }
          word += c

      }
    }
    // Check if we have an IPv4 ending
    if (numberOfPeriods > 0) {
      if (numberOfPeriods != 3 || !isValidIP4Word(word)) {
        return false
      }
    } else {
      if (numberOfColons != 7 && !doubleColon) {
        return false
      }
      if (numberOfPercent == 0) {
        if (word == "" && ipAddress.charAt(length - 1 - offset) == ':' &&
            ipAddress.charAt(length - 2 - offset) != ':') {
          return false
        }
      }
    }
    true
  }

  private def isValidHexChar(c: Char): Boolean =
    (c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f')

  private def isValidIP4Word(word: String): Boolean = {
    if (word.length < 1 || word.length > 3) {
      return false
    }

    for (c <- word) {
      if (!(c >= '0' && c <= '9')) {
        return false
      }
    }

    if (Integer.parseInt(word) > 255) {
      return false
    }

    true
  }

  private val loopback = new Inet4Address(Array[Byte](127, 0, 0, 1))

  def getLoopbackAddress(): InetAddress = loopback

  private def byteArrayFromIPString(ip: String): Array[Byte] = {
    if (isValidIPv4Address(ip))
      return ip.split("\\.").map(Integer.parseInt(_).toByte)

    var ipAddr = ip
    if (ipAddr.charAt(0) == '[')
      ipAddr = ipAddr.substring(1, ipAddr.length - 1)

    val tokenizer        = new StringTokenizer(ipAddr, ":.", true)
    val hexStrings       = new ArrayBuffer[String]()
    val decStrings       = new ArrayBuffer[String]()
    var token            = ""
    var prevToken        = ""
    var doubleColonIndex = -1

    /*
     * Go through the tokens, including the separators ':' and '.' When we
     * hit a : or . the previous token will be added to either the hex list
     * or decimal list. In the case where we hit a :: we will save the index
     * of the hexStrings so we can add zeros in to fill out the string
     */
    while (tokenizer.hasMoreTokens()) {
      prevToken = token
      token = tokenizer.nextToken()

      if (token == ":") {
        if (prevToken == ":")
          doubleColonIndex = hexStrings.size
        else if (prevToken != "")
          hexStrings += prevToken
      } else if (token == ".")
        decStrings += prevToken
    }

    if (prevToken == ":") {
      if (token == ":")
        doubleColonIndex = hexStrings.size
      else
        hexStrings += token
    } else if (prevToken == ".")
      decStrings += token

    // figure out how many hexStrings we should have
    // also check if it is a IPv4 address
    var hexStringLength = 8
    // If we have an IPv4 address tagged on at the end, subtract
    // 4 bytes, or 2 hex words from the total
    if (decStrings.size > 0)
      hexStringLength -= 2

    if (doubleColonIndex != -1) {
      val numberToInsert = hexStringLength - hexStrings.size
      for (i <- 0 until numberToInsert)
        hexStrings.insert(doubleColonIndex, "0")
    }

    val ipByteArray = new Array[Byte](16)

    for (i <- 0 until hexStrings.size)
      convertToBytes(hexStrings(i), ipByteArray, i * 2)

    for (i <- 0 until decStrings.size)
      ipByteArray(i + 12) =
        (java.lang.Byte.parseByte(decStrings(i)) & 255).toByte

    // now check to see if this guy is actually and IPv4 address
    // an ipV4 address is ::FFFF:d.d.d.d
    var ipV4 = true
    for (i <- 0 until 10) {
      if (ipByteArray(i) != 0)
        ipV4 = false
    }

    if (ipByteArray(10) != -1 || ipByteArray(11) != -1)
      ipV4 = false

    if (ipV4) {
      val ipv4ByteArray = new Array[Byte](4)
      for (i <- 0 until 4)
        ipv4ByteArray(i) = ipByteArray(i + 12)
      return ipv4ByteArray
    }

    return ipByteArray
  }

  private def convertToBytes(hexWord: String,
                             ipByteArray: Array[Byte],
                             byteIndex: Int): Unit = {
    val hexWordLength = hexWord.length
    var hexWordIndex  = 0
    ipByteArray(byteIndex) = 0
    ipByteArray(byteIndex + 1) = 0

    var charValue = 0
    if (hexWordLength > 3) {
      charValue = getIntValue(hexWord.charAt(hexWordIndex))
      hexWordIndex += 1
      ipByteArray(byteIndex) =
        (ipByteArray(byteIndex) | (charValue << 4)).toByte
    }
    if (hexWordLength > 2) {
      charValue = getIntValue(hexWord.charAt(hexWordIndex))
      hexWordIndex += 1
      ipByteArray(byteIndex) = (ipByteArray(byteIndex) | charValue).toByte
    }
    if (hexWordLength > 1) {
      charValue = getIntValue(hexWord.charAt(hexWordIndex))
      hexWordIndex += 1
      ipByteArray(byteIndex + 1) =
        (ipByteArray(byteIndex + 1) | (charValue << 4)).toByte
    }

    charValue = getIntValue(hexWord.charAt(hexWordIndex))
    ipByteArray(byteIndex + 1) =
      (ipByteArray(byteIndex + 1) | charValue & 15).toByte
  }

  private def getIntValue(c: Char): Int = {
    if (c <= '9' && c >= '0')
      return c - '0'
    val cLower = Character.toLowerCase(c)
    if (cLower <= 'f' && cLower >= 'a') {
      return cLower - 'a' + 10
    }
    return 0
  }

  private val hexCharacters = "0123456789ABCDEF"

  private[net] def createIPStringFromByteArray(
      ipByteArray: Array[Byte]): String = {
    if (ipByteArray.length == 4)
      return addressToString(bytesToInt(ipByteArray, 0))

    if (ipByteArray.length == 16) {
      if (isIPv4MappedAddress(ipByteArray)) {
        val ipv4ByteArray = new Array[Byte](4)
        for (i <- 0 until 4)
          ipv4ByteArray(i) = ipByteArray(i + 12)

        return addressToString(bytesToInt(ipv4ByteArray, 0))
      }
      val buffer  = new StringBuilder()
      var isFirst = true
      for (i <- 0 until ipByteArray.length) {
        if ((i & 1) == 0)
          isFirst = true

        var j = (ipByteArray(i) & 0xf0) >>> 4
        if (j != 0 || !isFirst) {
          buffer.append(hexCharacters.charAt(j))
          isFirst = false
        }
        j = ipByteArray(i) & 0x0f
        if (j != 0 || !isFirst) {
          buffer.append(hexCharacters.charAt(j))
          isFirst = false
        }
        if ((i & 1) != 0 && (i + 1) < ipByteArray.length) {
          if (isFirst)
            buffer.append('0')
          buffer.append(':')
        }
      }
      return buffer.toString
    }
    null
  }

  private def isIPv4MappedAddress(ipAddress: Array[Byte]): Boolean = {
    // Check if the address matches ::FFFF:d.d.d.d
    // The first 10 bytes are 0. The next to are -1 (FF).
    // The last 4 bytes are varied.
    for (i <- 0 until 10)
      if (ipAddress(i) != 0)
        return false

    if (ipAddress(10) != -1 || ipAddress(11) != -1)
      return false

    return true
  }

  private[net] def bytesToInt(bytes: Array[Byte], start: Int): Int = {
    // First mask the byte with 255, as when a negative
    // signed byte converts to an integer, it has bits
    // on in the first 3 bytes, we are only concerned
    // about the right-most 8 bits.
    // Then shift the rightmost byte to align with its
    // position in the integer.
    return (((bytes(start + 3) & 255)) | ((bytes(start + 2) & 255) << 8)
      | ((bytes(start + 1) & 255) << 16)
      | ((bytes(start) & 255) << 24))
  }

  private def addressToString(value: Int): String = {
    return (((value >> 24) & 0xff) + "." + ((value >> 16) & 0xff) + "."
      + ((value >> 8) & 0xff) + "." + (value & 0xff))
  }
}

object InetAddress extends InetAddressBase

class InetAddress private[net] (ipAddress: Array[Byte],
                                private var host: String)
    extends Serializable {
  import InetAddress._

  private[net] def this(ipAddress: Array[Byte]) = this(ipAddress, null)

  def getHostAddress(): String = createIPStringFromByteArray(ipAddress)

  def getHostName(): String = {
    if (host == null) {
      val ipString = createIPStringFromByteArray(ipAddress)
      host = SocketHelpers
        .ipToHost(ipString, isValidIPv6Address(ipString))
        .getOrElse {
          ipString
        }
    }
    host
  }

  def getAddress() = ipAddress.clone

  override def equals(obj: Any): Boolean = {
    if (obj == null || obj.getClass != this.getClass) {
      false
    } else {
      val objIPAddress = obj.asInstanceOf[InetAddress].getAddress;
      objIPAddress.indices.forall(i => objIPAddress(i) == ipAddress(i))
    }
  }

  override def hashCode(): Int = InetAddress.bytesToInt(ipAddress, 0)

  override def toString(): String = {
    if (host == null)
      return "/" + getHostAddress()
    else
      return host + "/" + getHostAddress()
  }

  def isReachable(timeout: Int): Boolean = {
    if (timeout < 0) {
      throw new IllegalArgumentException(
        "Argument 'timeout' in method 'isReachable' is negative")
    } else {
      val ipString = createIPStringFromByteArray(ipAddress)
      SocketHelpers.isReachableByEcho(ipString, timeout, 7)
    }
  }

  def isLinkLocalAddress(): Boolean = false

  def isAnyLocalAddress(): Boolean = false

  def isLoopbackAddress(): Boolean = false

  def isMCGlobal(): Boolean = false

  def isMCLinkLocal(): Boolean = false

  def isMCNodeLocal(): Boolean = false

  def isMCOrgLocal(): Boolean = false

  def isMCSiteLocal(): Boolean = false

  def isMulticastAddress(): Boolean = false

  def isSiteLocalAddress(): Boolean = false

}
