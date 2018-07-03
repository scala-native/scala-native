package java.net

// Ported from Apache Harmony

object DatagramSocketImplSuite extends tests.Suite {
  test("constructor") {
    val impl = new MockDatagramSocketImpl()
    assert(impl.getFileDescriptor() == null)
  }

  test("connect") {
    val impl      = new MockDatagramSocketImpl()
    val localhost = InetAddress.getByName("localhost")
    impl.test_connect(localhost, 0)
    impl.test_connect(localhost, -1)
    impl.test_connect(null, -1)
    impl.test_disconnect()
  }
}

final class MockDatagramSocketImpl extends DatagramSocketImpl {
  protected[net] def bind(port: Int, addr: InetAddress): Unit = {}

  protected[net] def close(): Unit = {}

  protected[net] def create(): Unit = {}

  @deprecated
  protected[net] def getTTL(): Byte = 0

  protected[net] def getTimeToLive(): Int = 0

  protected[net] def join(addr: InetAddress): Unit = {}

  protected[net] def joinGroup(addr: SocketAddress,
                               netInterface: NetworkInterface): Unit = {}

  protected[net] def leave(addr: InetAddress): Unit = {}

  protected[net] def leaveGroup(addr: SocketAddress,
                                netInterface: NetworkInterface): Unit = {}

  protected[net] def peek(sender: InetAddress): Int = 0

  protected[net] def receive(pack: DatagramPacket): Unit = {}

  protected[net] def send(pack: DatagramPacket): Unit = {}

  protected[net] def setTimeToLive(ttl: Int): Unit = {}

  @deprecated
  protected[net] def setTTL(ttl: Byte): Unit = {}

  protected[net] def peekData(pack: DatagramPacket): Int = 0

  def getOption(optID: Int): Object = null

  def setOption(optID: Int, value: Object): Unit = {}

  def test_connect(inetAddr: InetAddress, port: Int): Unit =
    super.connect(inetAddr, port)

  def test_disconnect(): Unit = super.disconnect()
}
