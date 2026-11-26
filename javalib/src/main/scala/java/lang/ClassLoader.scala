package java.lang

import java.net.URL
import java.util.stream.Stream
import java.util.{Collections, Enumeration, Objects}

class ClassLoader protected (parent: ClassLoader) {
  def this() = this(null)
  def getResourceAsStream(name: String): java.io.InputStream =
    if (name.startsWith("/")) null
    else this.getClass().getResourceAsStream("/" + name)

  def getResources(name: String): Enumeration[URL] = {
    Objects.requireNonNull(name)
    Collections.emptyEnumeration[URL]()
  }

  /** @since JDK 9 */
  def resources(name: String): Stream[URL] = {
    Objects.requireNonNull(name)
    Stream.empty[URL]()
  }

  def getUnnamedModule(): Module = new Module(null, this)
}

object ClassLoader {

  /** @since JDK 9 */
  def getPlatformClassLoader(): ClassLoader = NativeClassLoader
  def getSystemClassLoader(): ClassLoader = NativeClassLoader

  def getSystemResources(name: String): Enumeration[URL] =
    getSystemClassLoader().getResources(name)

  def getSystemResourceAsStream(name: String): java.io.InputStream =
    getSystemClassLoader().getResourceAsStream(name)

  private object NativeClassLoader extends ClassLoader()

}
