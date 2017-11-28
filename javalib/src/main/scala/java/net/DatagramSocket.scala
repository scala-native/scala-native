package java.net

import java.util.concurrent.locks.Lock

// Ported from Apache Harmony

class DatagramSocket private (private[net] var impl: DatagramSocketImpl,
                              private[net] var address: InetAddress) {

  private final class Lock
  private val lock = new Lock

  private[net] var port  = -1
  private[net] var bound = false
  private var connected  = false
  private var closed     = false

  def this(aPort: Int) = {
    this(null, null)
    checkListen(aPort)
    createSocket(aPort, InetAddress.ANY)
  }

  def this(aPort: Int, addr: InetAddress) = {
    this(null, null)
    checkListen(aPort)
    createSocket(aPort, if (addr == null) InetAddress.ANY else addr)
  }

  def this() = this(0)

  private[net] def checkListen(aPort: Int): Unit = {
    if (aPort < 0 || aPort > 65535) {
      throw new IllegalArgumentException(s"Port out of range: $aPort")
    }
  }

  def close(): Unit = {
    closed = true
    impl.close()
  }

  def connect(anAddress: InetAddress, aPort: Int): Unit = {
    if (anAddress == null || aPort < 0 || aPort > 655535) {
      throw new IllegalArgumentException(
        s"Address is null or port $aPort is not within range.")
    }

    lock.synchronized {
      if (isClosed()) {
        return
      }

      try {
        checkClosedAndBind(true)
      } catch {
        case e: SocketException => // Ignored
      }

      try {
        impl.connect(anAddress, aPort)
      } catch {
        case e: SocketException => // not connected at the native level just do what we did before
      }

      address = anAddress
      port = aPort
      connected = true
    }
  }

  def disconnect(): Unit = {
    if (isClosed() || !isConnected()) {
      return
    }

    impl.disconnect()
    address = null
    port = -1
    connected = false
  }

  private[net] def createSocket(aPort: Int, addr: InetAddress): Unit =
    synchronized {
      impl = new PlainDatagramSocketImpl()
      impl.create()

      try {
        impl.bind(aPort, addr)
        bound = true
      } catch {
        case e: SocketException =>
          close()
          throw e
      }
    }

  def getInetAddress(): InetAddress = address

  def getLocalAddress(): InetAddress = {
    if (isClosed()) {
      return null
    }

    if (!isBound()) {
      return InetAddress.ANY
    }

    impl.getLocalAddress()
  }

  def getLocalPort(): Int = {
    if (isClosed()) {
      return -1
    }

    if (!isBound()) {
      return 0
    }

    impl.getLocalPort()
  }

  def getPort(): Int = port

  private[net] def isMulticastSocket(): Boolean = false

  def getReceiveBufferSize(): Int = synchronized {
    checkClosedAndBind(false)
    impl.getOption(SocketOptions.SO_RCVBUF).asInstanceOf[Int]
  }

  def getSendBufferSize(): Int = synchronized {
    checkClosedAndBind(false)
    impl.getOption(SocketOptions.SO_SNDBUF).asInstanceOf[Int]
  }

  def getSoTimeout(): Int = synchronized {
    checkClosedAndBind(false)
    impl.getOption(SocketOptions.SO_TIMEOUT).asInstanceOf[Int]
  }

  def receive(pack: DatagramPacket): Unit = synchronized {
    checkClosedAndBind(true)

    var senderAddr: InetAddress = null;
    var senderPort              = -1;
    var tempPack                = new DatagramPacket(Array.fill[Byte](1)(0), 1)

    var copy = false

    if (address != null) {
      if (pack == null) {
        throw new NullPointerException()
      }

      var loop = true
      while (loop) {
        copy = false

        try {
          senderPort = impl.peekData(tempPack)
          senderAddr = tempPack.getAddress()
        } catch {
          case e: SocketException => {
            if (e.getMessage() == "The Socket does not support the operation") {
              tempPack =
                new DatagramPacket(Array.fill[Byte](pack.getCapacity())(0),
                                   pack.getCapacity())
              impl.receive(tempPack)

              senderAddr = tempPack.getAddress()
              senderPort = tempPack.getPort()
              copy = true
            } else {
              throw e
            }
          }
        }

        if (address == null) {
          loop = false
        } else if (port == senderPort && address == senderAddr) {
          loop = false
        } else if (!copy) {
          impl.receive(tempPack)
        }
      }
    }

    if (copy) {
      Array.copy(tempPack.getData(),
                 0,
                 pack.getData(),
                 pack.getOffset(),
                 tempPack.getLength())

      pack.setLengthOnly(tempPack.getLength())
      pack.setAddress(tempPack.getAddress())
      pack.setPort(tempPack.getPort())
    } else {
      pack.setLength(pack.getCapacity())
      impl.receive(pack)
    }
  }

  def send(pack: DatagramPacket): Unit = {
    checkClosedAndBind(true)

    val packAddr = pack.getAddress()
    if (address != null) {
      if (pack != null) {
        if (address != packAddr || port != pack.getPort()) {
          throw new IllegalArgumentException(
            "Packet address mismatch with connected address")
        }
      } else {
        pack.setAddress(address)
        pack.setPort(port)
      }
    } else {
      if (packAddr == null) {
        throw new NullPointerException("Destination address is null")
      }
    }

    impl.send(pack)
  }

  def setSendBufferSize(size: Int): Unit = synchronized {
    if (size < 1) {
      throw new IllegalArgumentException("Zero or negative buffer size")
    }

    checkClosedAndBind(false)
    impl.setOption(SocketOptions.SO_SNDBUF, Int.box(size))
  }

  def setReceiveBufferSize(size: Int): Unit = synchronized {
    if (size < 1) {
      throw new IllegalArgumentException("Zero or negative buffer size")
    }

    checkClosedAndBind(false)
    impl.setOption(SocketOptions.SO_RCVBUF, Int.box(size))
  }

  def setSoTimeout(timeout: Int): Unit = synchronized {
    if (timeout < 0) {
      throw new IllegalArgumentException("Invalid negative timeout")
    }

    checkClosedAndBind(false)
    impl.setOption(SocketOptions.SO_TIMEOUT, Int.box(timeout))
  }

  protected[net] def this(socketImpl: DatagramSocketImpl) = {
    this(socketImpl, null)
    if (socketImpl == null) {
      throw new NullPointerException()
    }
  }

  def this(localAddr: SocketAddress) = {
    this(null, null)
    if (localAddr != null) {
      if (!localAddr.isInstanceOf[InetSocketAddress]) {
        val socketClass = localAddr.getClass()
        throw new IllegalArgumentException(
          s"SocketAddress $socketClass not supported")
      }
      checkListen(localAddr.asInstanceOf[InetSocketAddress].getPort)
    }

    impl = new PlainDatagramSocketImpl()
    impl.create()

    if (localAddr != null) {
      try {
        bind(localAddr)
      } catch {
        case e: SocketException =>
          close()
          throw e
      }
    }

    setBroadcast(true)
  }

  private[net] def checkClosedAndBind(bind: Boolean): Unit = {
    if (isClosed()) {
      throw new SocketException("Socket is closed")
    }
    if (bind && !isBound()) {
      checkListen(0)
      impl.bind(0, InetAddress.ANY)
      bound = true
    }
  }

  def bind(localAddr: SocketAddress): Unit = {
    checkClosedAndBind(false)
    var localPort         = 0
    var addr: InetAddress = InetAddress.ANY

    if (localAddr != null) {
      if (!localAddr.isInstanceOf[InetSocketAddress]) {
        val socketClass = localAddr.getClass()
        throw new IllegalArgumentException(
          s"SocketAddress $socketClass not supported")
      }
      val inetAddr = localAddr.asInstanceOf[InetSocketAddress]
      addr = inetAddr.getAddress
      if (addr == null) {
        val hostName = inetAddr.getHostName
        throw new SocketException(s"Host is unresolved\: $hostName")
      }
      localPort = inetAddr.getPort
      checkListen(localPort)
    }
    impl.bind(localPort, addr)
    bound = true
  }

  def connect(remoteAddr: SocketAddress): Unit = {
    if (remoteAddr == null) {
      throw new IllegalArgumentException("SocketAddress is null")
    }

    if (!remoteAddr.isInstanceOf[InetSocketAddress]) {
      val socketClass = remoteAddr.getClass()
      throw new IllegalArgumentException(
        s"SocketAddress $socketClass not supported")
    }

    val inetAddr = remoteAddr.asInstanceOf[InetSocketAddress]
    if (inetAddr.getAddress == null) {
      val hostName = inetAddr.getHostName
      throw new SocketException(s"Host is unresolved\: $hostName")
    }

    lock.synchronized {
      checkClosedAndBind(true)

      try {
        impl.connect(inetAddr.getAddress, inetAddr.getPort)
      } catch {
        case e: ConnectException => throw (e)
        case e: Exception        => // Not connected at the native level just do what we did before
      }

      address = inetAddr.getAddress
      port = inetAddr.getPort
      connected = true
    }
  }

  def isBound(): Boolean = bound

  def isConnected(): Boolean = connected

  def getRemoteSocketAddress(): SocketAddress = {
    return if (!isConnected()) null
    else new InetSocketAddress(getInetAddress(), getPort())
  }

  def getLocalSocketAddress(): SocketAddress = {
    return if (!isBound()) null
    else new InetSocketAddress(getLocalAddress(), getLocalPort())
  }

  def setReuseAddress(reuse: Boolean): Unit = {
    checkClosedAndBind(false)
    impl.setOption(SocketOptions.SO_REUSEADDR, Boolean.box(reuse))
  }

  def getReuseAddress(): Boolean = {
    checkClosedAndBind(false)
    impl.getOption(SocketOptions.SO_REUSEADDR).asInstanceOf[Boolean]
  }

  def setBroadcast(broadcast: Boolean): Unit = {
    checkClosedAndBind(false)
    impl.setOption(SocketOptions.SO_BROADCAST, Boolean.box(broadcast))
  }

  def getBroadcast(): Boolean = {
    checkClosedAndBind(false)
    impl.getOption(SocketOptions.SO_BROADCAST).asInstanceOf[Boolean]
  }

  def setTrafficClass(value: Int): Unit = {
    checkClosedAndBind(false)
    if (value < 0 || value > 255) {
      throw new IllegalArgumentException()
    }
    impl.setOption(SocketOptions.IP_TOS, Int.box(value))
  }

  def getTrafficClass(): Int = {
    checkClosedAndBind(false)
    impl.getOption(SocketOptions.IP_TOS).asInstanceOf[Int]
  }

  def isClosed(): Boolean = closed
}
