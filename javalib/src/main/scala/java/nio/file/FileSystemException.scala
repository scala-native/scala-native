package java.nio.file

import java.io.IOException

class FileSystemException(file: String, other: String, reason: String)
    extends IOException() {

  def this(file: String) = this(file, null, null)

  def getFile(): String =
    file

  override def getMessage(): String = {

    val files =
      (file, other) match {
        case (null, null) => null
        case (null, f2)   => s" -> $f2"
        case (f1, null)   => s"$f1"
        case (f1, f2)     => s"$f1 -> $f2"
      }
    (files, reason) match {
      case (null, null)    => null
      case (null, reason)  => reason
      case (files, null)   => files
      case (files, reason) => s"$files: $reason"
    }
  }

  def getOtherFile(): String =
    other

  def getReason(): String =
    reason
}
