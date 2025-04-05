package java.lang

class ClassLoader protected (parent: ClassLoader) {
  def this() = this(null)
  def getResourceAsStream(name: String): java.io.InputStream =
    ClassLoader.getResourceAsStream(name)
  def getUnnamedModule(): Module = new Module(null, this)
}

object ClassLoader {
  def getPlatformClassLoader(): ClassLoader = NativeClassLoader
  def getSystemClassLoader(): ClassLoader = NativeClassLoader

  private object NativeClassLoader extends ClassLoader()
  def getResourceAsStream(name: String): java.io.InputStream =
    classOf[NativeClassLoader.type].getResourceAsStream(name)
}
