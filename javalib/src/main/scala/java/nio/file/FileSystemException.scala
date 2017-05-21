package java.nio.file

import java.io.IOException

class FileSystemException(file: String, other: String, reason: String)
    extends IOException() {

  def this(file: String) = this(file, null, null)

  def getFile(): String =
    file

  override def getMessage(): String = {
    val message =
      if (reason == null) ""
      else s": $reason"
    val files =
      (file, other) match {
        case (null, null) => ""
        case (null, f2)   => s":  -> $f2"
        case (f1, null)   => s": $f1"
        case (f1, f2)     => s": $f1 -> $f2"
      }
    s"$files$message"
  }

  def getOtherFile(): String =
    other

  def getReason(): String =
    reason
}
