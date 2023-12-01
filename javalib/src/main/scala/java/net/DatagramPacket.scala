package java.net

final class DatagramPacket(
    var buf: Array[Byte],
    var offset: Int,
    var length: Int,
    // bufLength: Int,
    var address: InetAddress,
    var port: Int
) {
  if (length < 0 || offset < 0 || (length + offset) < 0 || ((length + offset) > buf.length)) {
    throw new IllegalArgumentException("illegal length or offset");
  }

  def this(buff: Array[Byte], offset: Int, length: Int) =
    this(buff, offset, length, null, -1)

  def this(buff: Array[Byte], length: Int) =
    this(buff, 0, length, null, -1)

  def this(buff: Array[Byte], length: Int, address: InetAddress) =
    this(buff, 0, length, address, -1)

  def this(buff: Array[Byte], length: Int, address: InetAddress, port: Int) =
    this(buff, 0, length, address, port)

  def getAddress(): InetAddress = address

  def getPort(): Int = port

  def getData(): Array[Byte] = buf

  def getOffset(): Int = offset

  def getLength(): Int = length

  def setData(buff: Array[Byte], offset: Int, length: Int): Unit = {
    if (length < 0 || offset < 0 || (length + offset) < 0 || ((length + offset) > buf.length)) {
      throw new IllegalArgumentException("illegal length or offset");
    }
    this.buf = buff
    this.offset = 0
    this.length = length
  }

  def setData(buff: Array[Byte]): Unit = {
    if (buf == null) {
      throw new NullPointerException("null packet buffer")
    }
    this.buf = buff
    this.offset = 0
    this.length = buff.length
  }

  def setAddress(iaddr: InetAddress): Unit = {
    this.address = iaddr
  }

  def setPort(iport: Int): Unit = {
    if (iport < 0 || iport > 0xffff) {
      throw new IllegalArgumentException("Port out of range:" + iport)
    }
    this.port = iport
  }

  def setSocketAddress(address: SocketAddress) = {
    if (address == null || !address.isInstanceOf[InetSocketAddress]) {
      throw new IllegalArgumentException("unsupported address type")
    }
    val addr = address.asInstanceOf[InetSocketAddress]
    if (addr.isUnresolved) {
      throw new IllegalArgumentException("unresolved address")
    }
    setAddress(addr.getAddress)
    setPort(addr.getPort)
  }

  def getSocketAddress(): SocketAddress = {
    new InetSocketAddress(getAddress(), getPort())
  }

  def setLength(length: Int): Unit = {
    if ((length + offset) > buf.length || length < 0 || (length + offset) < 0) {
      throw new IllegalArgumentException("illegal length")
    }
    this.length = length
  }

}
