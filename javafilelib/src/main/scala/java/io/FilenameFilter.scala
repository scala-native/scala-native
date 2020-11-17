package java.io

trait FilenameFilter {
  def accept(dir: File, filename: String): Boolean
}

object FilenameFilter {
  val allPassFilter: FilenameFilter =
    new FilenameFilter {
      override def accept(dir: File, filename: String): Boolean = true
    }
}
