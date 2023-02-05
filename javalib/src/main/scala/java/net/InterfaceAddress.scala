package java.net

class InterfaceAddress private[net] (
    inetAddr: InetAddress,
    broadcastAddr: Option[Array[Byte]],
    prefixLength: Short
) {

  override def equals(that: Any): Boolean = that match {
    case that: InterfaceAddress => this.hashCode() == that.hashCode()
    case _                      => false
  }

  def getAddress(): InetAddress = inetAddr

  lazy val bcAddress = {
    if (broadcastAddr.isEmpty) null
    else InetAddress.getByAddress(broadcastAddr.get)
  }

  def getBroadcast(): InetAddress = bcAddress

  def getNetworkPrefixLength(): Short = prefixLength

  /** This hashCode is not intended or guaranteed to match Java.
   */
  override def hashCode(): Int =
    inetAddr.hashCode() + broadcastAddr.hashCode() + prefixLength

  override def toString(): String = {
    val iaPart = inetAddr.getHostAddress()

    val broadcastPart =
      if (broadcastAddr.isEmpty) "null"
      else {
        // Not the most runtime efficient algorithm, but easy to implement.
        InetAddress.getByAddress(broadcastAddr.get).toString()
      }

    s"/${iaPart}/${prefixLength} [${broadcastPart}]"
  }

}
