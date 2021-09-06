package java.nio.file

class InvalidPathException(input: String, reason: String, index: Int)
    extends IllegalArgumentException(reason) {
  def this(input: String, reason: String) = this(input, reason, -1)

  def getInput(): String = input
  def getReason() = reason
  def getIndex(): Int = index

  override def getMessage() = {
    val location =
      if (index >= 0) s"at index $index"
      else ""
    s"${this.getClass().getName()}: $reason $location: $input"
  }
}
