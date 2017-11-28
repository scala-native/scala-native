package java.net

// Ported from Apache Harmony

final class DatagramPacket(private[net] var data: Array[Byte],
                           private[net] var offset: Int = 0,
                           private[net] var length: Int) {
  checkDataArgs(data, offset, length)
  private[net] var capacity             = length
  private[net] var port                 = -1
  private[net] var address: InetAddress = null

  def this(data: Array[Byte], length: Int) = this(data, 0, length)

  def this(data: Array[Byte],
           offset: Int,
           length: Int,
           host: InetAddress,
           aPort: Int) = {
    this(data, offset, length)
    setPort(aPort)
    address = host
  }

  def this(data: Array[Byte], length: Int, host: InetAddress, port: Int) =
    this(data, 0, length, host, port)

  def this(data: Array[Byte], length: Int, sockAddr: SocketAddress) = {
    this(data, 0, length)
    setSocketAddress(sockAddr)
  }

  def this(data: Array[Byte],
           offset: Int,
           length: Int,
           sockAddr: SocketAddress) = {
    this(data, offset, length)
    setSocketAddress(sockAddr)
  }

  def getAddress(): InetAddress = synchronized { address }

  def getData(): Array[Byte] = synchronized { data }

  def getLength(): Int = synchronized { length }

  def getOffset(): Int = synchronized { offset }

  def getPort(): Int = synchronized { port }

  def setAddress(addr: InetAddress): Unit = synchronized {
    address = addr
  }

  def setData(buf: Array[Byte], anOffset: Int, aLength: Int): Unit =
    synchronized {
      checkDataArgs(buf, anOffset, aLength)
      data = buf
      offset = anOffset
      length = aLength
      capacity = aLength
    }

  private def checkDataArgs(buf: Array[Byte],
                            anOffset: Int,
                            aLength: Int): Unit = synchronized {
    if (0 > anOffset || anOffset > buf.size || 0 > aLength ||
        aLength > buf.size - anOffset) {
      throw new IllegalArgumentException("Arguments out of bounds")
    }
  }

  def setData(buf: Array[Byte]): Unit = {
    length = buf.size
    capacity = buf.size
    data = buf
    offset = 0
  }

  private[net] def getCapacity(): Int = synchronized { capacity }

  def setLength(len: Int): Unit = synchronized {
    if (len < 0 || offset + len > data.size) {
      throw new IllegalArgumentException("Arguments out of bounds")
    }
    length = len
  }

  private[net] def setLengthOnly(len: Int): Unit = synchronized {
    if (0 > len || offset + len > data.size) {
      throw new IllegalArgumentException("Arguments out of bounds")
    }
    length = len
  }

  def setPort(aPort: Int): Unit = synchronized {
    if (aPort < 0 || aPort > 65535) {
      throw new IllegalArgumentException(s"Port out of range: $aPort")
    }
    port = aPort
  }

  def getSocketAddress(): SocketAddress = synchronized {
    new InetSocketAddress(getAddress(), getPort())
  }

  def setSocketAddress(sockAddr: SocketAddress): Unit = synchronized {
    sockAddr match {
      case inetAddr: InetSocketAddress => {
        if (inetAddr.isUnresolved) {
          throw new IllegalArgumentException("Unresolved address")
        }
        port = inetAddr.getPort
        address = inetAddr.getAddress
      }
      case _ => {
        val sClass = if (sockAddr == null) null else sockAddr.getClass()
        throw new IllegalArgumentException(
          s"SocketAddress $sClass not supported")
      }
    }
  }
}
