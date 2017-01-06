package java.io

trait FileFilter {

  def accept(pathname: File): Boolean
}
