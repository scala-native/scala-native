package java.io

object IFileSystem {

  val SHARED_LOCK_TYPE: Int = 1;

  val EXCLUSIVE_LOCK_TYPE: Int = 2;

  val SEEK_SET: Int = 1;

  val SEEK_CUR: Int = 2;

  val SEEK_END: Int = 4;

  val O_RDONLY: Int = 0x00000000;

  val O_WRONLY: Int = 0x00000001;

  val O_RDWR: Int = 0x00000010;

  val O_RDWRSYNC: Int = 0x00000020;

  val O_APPEND: Int = 0x00000100;

  val O_CREAT: Int = 0x00001000;

  val O_EXCL: Int = 0x00010000;

  val O_NOCTTY: Int = 0x00100000;

  val O_NONBLOCK: Int = 0x01000000;

  val O_TRUNC: Int = 0x10000000;

}

trait IFileSystem {
  @throws(classOf[FileNotFoundException])
  def open(fileName: Array[Byte], mode: Int): Long

  @throws(classOf[IOException])
  def close(fileDescriptor: Long): Unit

  @throws(classOf[IOException])
  def write(fileDescriptor: Long,
            bytes: Array[Byte],
            offset: Int,
            length: Int): Long

  @throws(classOf[IOException])
  def read(fileDescriptor: Long,
           bytes: Array[Byte],
           offset: Int,
           length: Int): Long

  @throws(classOf[IOException])
  def ttyAvailable(): Long

  @throws(classOf[IOException])
  def ttyRead(bytes: Array[Byte], offset: Int, length: Int): Long

  @throws(classOf[IOException])
  def seek(fileDescriptor: Long, offset: Long, whence: Int): Long

  @throws(classOf[IOException])
  def available(fileDescriptor: Long): Long

}
