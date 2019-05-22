package java.lang

import scalanative.annotation.stub

class ClassLoader protected (parent: ClassLoader) {
  def this() = this(null)
  @stub
  def loadClass(name: String): Class[_] = ???
  @stub
  def getParent(): ClassLoader = ???
  @stub
  def getResourceAsStream(name: String): java.io.InputStream = ???
  @stub
  def getResources(name: String): java.util.Enumeration[_] = ???
}
