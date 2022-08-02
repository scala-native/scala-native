package java.nio.file

class ProviderMismatchException(msg: String)
    extends IllegalArgumentException(msg) {

  def this() = this(null)

}
