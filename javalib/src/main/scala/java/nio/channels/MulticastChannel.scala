package java.nio.channels

import java.net.{InetAddress, NetworkInterface}

trait MulticastChannel extends NetworkChannel {

  def join(group: InetAddress, interf: NetworkInterface): MembershipKey

}
