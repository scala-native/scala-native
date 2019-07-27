package java.util.regex

class PatternSyntaxException(desc: String, regex: String, index: Int)
    extends IllegalArgumentException() {

  def getPattern: String     = regex
  def getDescription: String = desc
  def getIndex: Int          = index
  override def getMessage(): String = {
    val cursor = (" " * index) + "^"

    s"""|$desc near index $index
        |$regex
        |$cursor""".stripMargin
  }
}
