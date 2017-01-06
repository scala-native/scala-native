package java.io

import scalanative.native._

final class FileDescriptor() {

  /**
   * Represents a link to any underlying OS resources for this FileDescriptor.
   * A value of -1 indicates that this FileDescriptor is invalid.
   */
  var descriptor: Long = -1L

  var readOnly: Boolean = false

  @throws(classOf[SyncFailedException])
  def sync(): Unit = if (!readOnly) syncImpl()

  //native funct.
  @throws(classOf[SyncFailedException])
  private def syncImpl(): Unit = {
    var syncfailed = false
    if (descriptor == -1) syncfailed = true
    if (!syncfailed && (descriptor > 2))
      syncfailed = (CFile.fileSync(descriptor) != 0)
    if (syncfailed) throw new SyncFailedException("Failed to Sync File")
  }

  def valid(): Boolean = descriptor != -1
}

object FileDescriptor {

  val in: FileDescriptor = new FileDescriptor()
  in.descriptor = getStdInDescriptor()

  val out: FileDescriptor = new FileDescriptor()
  out.descriptor = getStdOutDescriptor()

  val err: FileDescriptor = new FileDescriptor()
  err.descriptor = getStdErrDescriptor()

  //native funct.
  private def getStdInDescriptor(): Long = 0L

  //native funct.
  private def getStdOutDescriptor(): Long = 1L

  //native funct.
  private def getStdErrDescriptor(): Long = 2L

}
