package java.util.zip

import java.io.IOException

class ZipException(s: String) extends IOException(s) {
  def this() = this(null)
}
