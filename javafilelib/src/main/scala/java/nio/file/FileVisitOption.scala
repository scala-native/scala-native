package java.nio.file

class FileVisitOption private (name: String, ordinal: Int)
    extends Enum[FileVisitOption](name, ordinal)
object FileVisitOption {

  val FOLLOW_LINKS = new FileVisitOption("FOLLOW_LINKS", 0)

  val _values                          = Array(FOLLOW_LINKS)
  def values(): Array[FileVisitOption] = _values.clone()
  def valueOf(name: String): FileVisitOption = {
    _values.find(_.name == name).getOrElse {
      throw new IllegalArgumentException(
        "No enum const FileVisitOption." + name)
    }
  }
}
