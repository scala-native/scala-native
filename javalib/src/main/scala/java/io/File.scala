package java.io

class File private () extends Serializable with Comparable[File] {
  def this(path: String) = this()
  def this(parent: String, child: String) = this()
  def this(parent: File, child: String) = this()

  def compareTo(file: File): scala.Int = ???
}
