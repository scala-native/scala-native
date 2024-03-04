package java.nio.channels

// Ported from Apache Harmony
abstract class SelectionKey protected () {

  @volatile private var currentAttachment: Object = null

  def channel: SelectableChannel
  def selector: Selector
  def isValid: Boolean
  def cancel(): Unit
  def interestOps(): Int
  def interestOps(ops: Int): SelectionKey
  def readyOps: Int

  def attach(ob: Object): Object = {
    val oldAttachment = currentAttachment
    currentAttachment = ob
    oldAttachment
  }

  def attachment: Object = currentAttachment

  def isReadable: Boolean =
    (readyOps & SelectionKey.OP_READ) == SelectionKey.OP_READ

  def isWritable: Boolean =
    (readyOps & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE

  def isConnectable: Boolean =
    (readyOps & SelectionKey.OP_CONNECT) == SelectionKey.OP_CONNECT

  def isAcceptable: Boolean =
    (readyOps & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT

}

object SelectionKey {

  val OP_READ: Int = 1 << 0
  val OP_WRITE: Int = 1 << 2
  val OP_CONNECT: Int = 1 << 3
  val OP_ACCEPT: Int = 1 << 4

}
