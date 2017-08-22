package java.lang

class ClassLoader protected (parent: ClassLoader) {
  def this() = this(null)
  def loadClass(name: String): Class[_]                      = ???
  def getParent(): ClassLoader                               = ???
  def getResourceAsStream(name: String): java.io.InputStream = ???
  def getResources(name: String): java.util.Enumeration[_]   = ???
}
