package java.lang

class ClassLoader protected (parent: ClassLoader) {
  def this() = this(null)
  def loadClass(name: String): Class[_] = null
}
