package java.io

trait FilenameFilter {

  def accept(dir: File, filename: String): Boolean
}
