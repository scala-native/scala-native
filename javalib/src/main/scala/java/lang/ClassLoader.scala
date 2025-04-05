package java.lang

class ClassLoader protected (parent: ClassLoader) {
  def this() = this(null)
  def getResourceAsStream(name: String): java.io.InputStream =
    this.getClass().getResourceAsStream(name)
}
