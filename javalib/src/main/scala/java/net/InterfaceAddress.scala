package java.net

// Ported from Apache Harmony

class InterfaceAddress(private val addr: InetAddress, prefixLength: Short)
    extends Object {
  private val mask: InetAddress           = calSubnetMask()
  private var cacheBroadcast: InetAddress = new InetAddress(null)

  private def calSubnetMask(): InetAddress = addr match {
    case address: Inet4Address => {
      val maskAddr = Array.fill[Byte](4)(0)
      for (i <- 0 until prefixLength) {
        maskAddr(i / 8) = (maskAddr(i / 8) | (1 << (7 - i % 8))).toByte
      }
      new Inet4Address(maskAddr)
    }
    case _ => null
  }

  override def equals(obj: Any): Boolean = obj match {
    case obj: AnyRef if this eq obj => true
    case anotherInterAddr: InterfaceAddress => {
      var equals = addr == anotherInterAddr.addr
      if (equals && (anotherInterAddr
            .getNetworkPrefixLength() == prefixLength)) {
        val broadcast =
          if (cacheBroadcast.ipAddress == null) getBroadcast()
          else cacheBroadcast
        val anotherbr = anotherInterAddr.getBroadcast()
        equals =
          if (broadcast == null) anotherbr == null else broadcast == anotherbr
      }
      equals
    }
    case _ => false
  }

  override def hashCode(): Int = {
    var hashCode = if (addr == null) 0 else -addr.hashCode()
    val broadcast =
      if (cacheBroadcast.ipAddress == null) getBroadcast() else cacheBroadcast
    hashCode += (if (broadcast == null) 0 else broadcast.hashCode())
    hashCode += prefixLength
    hashCode
  }

  override def toString(): String = {
    val builder = new StringBuilder()
    if (addr != null) {
      builder ++= addr.toString()
    }
    builder += '/' ++= prefixLength.toString() ++= " ["
    val broadcast =
      if (cacheBroadcast.ipAddress == null) getBroadcast() else cacheBroadcast
    if (broadcast != null)
      builder ++= broadcast.toString()
    else
      builder ++= "null"
    builder += ']'
    builder.toString()
  }

  def getAddress(): InetAddress = addr

  def getBroadcast(): InetAddress = (addr, mask) match {
    case (x: Inet4Address, y: Inet4Address) => {
      if (cacheBroadcast.ipAddress == null) {
        val broadcast = Array.fill[Byte](4)(0)
        if (prefixLength > 0) {
          val maskBytes = mask.getAddress()
          val addrBytes = addr.getAddress()
          cacheBroadcast = new InetAddress(
            addrBytes
              .zip(maskBytes)
              .map(tuple => (tuple._1 | ~tuple._2).toByte))
        }
      }
      cacheBroadcast
    }
    case _ => null
  }

  def getNetworkPrefixLength(): Short = prefixLength
}
