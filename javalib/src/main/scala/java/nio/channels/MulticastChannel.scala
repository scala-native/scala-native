package java.nio.channels

import java.net.{InetAddress, NetworkInterface}

trait MulticastChannel extends NetworkChannel {

  def join(
      group: InetAddress,
      networkInterface: NetworkInterface
  ): MembershipKey
  def join(
      group: InetAddress,
      networkInterface: NetworkInterface,
      source: InetAddress
  ): MembershipKey

}
