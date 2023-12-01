package java.net

import java.io.FileDescriptor

abstract class DatagramSocketImpl extends SocketOptions {
  protected[net] var localport: Int
  protected[net] var fd: FileDescriptor
  protected[net] var socket: DatagramSocket

  protected[net] def setDatagramSocket(socket: DatagramSocket): Unit
  protected[net] def getDatagramSocket(): DatagramSocket
  protected[net] def create(): Unit
  protected[net] def bind(port: Int, laddr: InetAddress): Unit
  protected[net] def send(p: DatagramPacket): Unit
  protected[net] def connect(address: InetAddress, port: Int): Unit
  protected[net] def disconnect(): Unit
  protected[net] def peek(i: InetAddress): Int
  protected[net] def peekData(p: DatagramPacket): Int
  protected[net] def receive(p: DatagramPacket): Unit
  protected[net] def setTTL(ttl: Byte): Unit
  protected[net] def getTTL(): Byte
  protected[net] def setTimeToLive(ttl: Int): Unit
  protected[net] def getTimeToLive(): Int
  protected[net] def join(inetaddr: InetAddress): Unit
  protected[net] def leave(inetaddr: InetAddress): Unit
  protected[net] def joinGroup(
      mcastaddr: SocketAddress,
      netIf: NetworkInterface
  ): Unit
  protected[net] def leaveGroup(
      mcastaddr: SocketAddress,
      netIf: NetworkInterface
  ): Unit
  private[net] def dataAvailable(): Int
  protected[net] def close(): Unit

  protected[net] def getLocalPort(): Int = localport
  protected[net] def getFileDescriptor(): FileDescriptor
}
