package java.net

import scala.scalanative.native._
import scala.scalanative.posix.errno._
import scala.scalanative.posix.sys.socket
import scala.scalanative.posix.sys.socketOps._
import scala.scalanative.posix.netdb._
import scala.scalanative.posix.netdbOps._
import scala.scalanative.posix.sys.ioctl._
import scala.scalanative.posix.unistd.{close => cClose}
import java.io.{FileDescriptor, IOException, OutputStream, InputStream}

private[net] class PlainSocketImpl extends SocketImpl  {

  protected[net] var fd = -1
  protected[net] var localport = 0
  protected[net] var addr: InetAddress = null
  protected[net] var port = 0

  override def getInetAddress: InetAddress = addr

  override def create(streaming: Boolean): Unit = {
    val sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM, 0)
    if(sock < 0) throw new IOException("Couldn't create a socket")
    fd = sock
  }

  override def connect(host: String, port: Int): Unit = {
    val addr = InetAddress.getByName(host)
    connect(addr, port)
  }

  override def connect(address: InetAddress, port: Int): Unit = {
    connect(address, port, 0) 
  }

  private def connect(address: InetAddress, port: Int, timeout: Int) = {
    val hints = stackalloc[addrinfo]
    val ret = stackalloc[addrinfo]
    string.memset(hints.cast[Ptr[Byte]], 0, sizeof[addrinfo])
    hints.ai_family = socket.AF_UNSPEC
    hints.ai_flags = 4 // AI_NUMERICHOST
    hints.ai_socktype = socket.SOCK_STREAM
    ret.ai_next = null
    
    Zone { implicit z =>
      val cIP = toCString(address.getHostAddress)
      if(getaddrinfo(cIP, toCString(port.toString), hints, ret) != 0) {
        throw new IOException("Couldn't resolve address: " + address.getHostAddress)
      }
    }
    // TODO: timeout
    val connectRes = socket.connect(fd, ret.ai_addr, ret.ai_addrlen)
    if(connectRes < 0) { 
      throw new IOException("Couldn't connect to address: " + address.getHostAddress
                            + " on port: " + port)
    }
    this.addr = address
    this.port = port
    // TODO: freeaddrinfo(ret)
  }

  override def close: Unit = {
    if(fd != -1) {
      cClose(fd)
      fd = -1
    }
  }

  override def getOutputStream: OutputStream = {
    // TODO: is fd valid?
    new SocketOutputStream(this)
  }

  override def getInputStream: InputStream = {
    // TODO: is fd valid?
    new SocketInputStream(this)
  }

  override def shutdownOutput: Unit = {
    socket.shutdown(fd, 1) match {
      case 0 => shutOutput = true
      case _ => throw new IOException("Error while shutting down socket's output")
    }
  }

  def write(buffer: Array[Byte], offset: Int, count: Int): Long = {
    if(shutOutput)
      throw new IOException("Trying to write to a shut down socket")
    else if(fd == -1)
      0
    else {
      Zone { implicit z =>
        val cArr = stackalloc[Byte](count)
        for(i <- 0 until count)
          !(cArr + i) = buffer(i + offset)
        socket.send(fd, cArr, count, 0)
      }
    }
  }

  def read(buffer: Array[Byte], offset: Int, count: Int): Int = {
    if(shutInput) -1

    val cBuff = stackalloc[Byte](count)
    val bytesNum = socket.recv(fd, cBuff, count, 0).toInt
    if(bytesNum <= 0) -1
    else {
      for(i <- 0 until bytesNum) {
        buffer(offset + i) = cBuff(i)
      }
      bytesNum
    }
  }

  override def available: Int = {
    val bytesAvailable = stackalloc[CInt]
    ioctl(fd, FIONREAD, bytesAvailable.cast[Ptr[Byte]])
    !bytesAvailable match {
      case -1 => throw new IOException(
                  "Error while trying to estimate available bytes to read")
      case x => x
    }
  }
  
}
