package java.nio.file

final class FileVisitResult private (name: String, ordinal: Int)
    extends Enum[FileVisitResult](name, ordinal)

object FileVisitResult {
  final val CONTINUE      = new FileVisitResult("CONTINUE", 0)
  final val TERMINATE     = new FileVisitResult("TERMINATE", 1)
  final val SKIP_SUBTREE  = new FileVisitResult("SKIP_SUBTREE", 2)
  final val SKIP_SIBLINGS = new FileVisitResult("SKIP_SIBLINGS", 3)

  private val _values                  = Array(CONTINUE, TERMINATE, SKIP_SUBTREE, SKIP_SIBLINGS)
  def values(): Array[FileVisitResult] = _values.clone()
  def valueOf(name: String): FileVisitResult = {
    _values.find(_.name == name).getOrElse {
      throw new IllegalArgumentException(
        "No enum const FileVisitResult." + name)
    }
  }

}
