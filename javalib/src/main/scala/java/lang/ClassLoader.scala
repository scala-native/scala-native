package java.lang

import java.net.URL
import java.util.stream.Stream
import java.util.{Collections, Enumeration}

import scala.scalanative.javalib.net.www.protocol.resource.{
  Handler => ResourceHandler
}

class ClassLoader protected (parent: ClassLoader) {
  def this() = this(null)
  def getResourceAsStream(name: String): java.io.InputStream =
    if (name.startsWith("/")) null
    else this.getClass().getResourceAsStream("/" + name)

  def getResource(name: String): URL =
    if (name.startsWith("/")) null
    else ResourceHandler.urlOf("/" + name)

  def getResources(name: String): Enumeration[URL] = {
    val url = getResource(name)
    if (url == null) Collections.emptyEnumeration[URL]()
    else Collections.enumeration(Collections.singletonList(url))
  }

  /** @since JDK 9 */
  def resources(name: String): Stream[URL] = {
    val url = getResource(name)
    if (url == null) Stream.empty[URL]()
    else Stream.of(url)
  }

  def getUnnamedModule(): Module = new Module(null, this)
}

object ClassLoader {

  /** @since JDK 9 */
  def getPlatformClassLoader(): ClassLoader = NativeClassLoader
  def getSystemClassLoader(): ClassLoader = NativeClassLoader

  def getSystemResource(name: String): URL =
    getSystemClassLoader().getResource(name)

  def getSystemResources(name: String): Enumeration[URL] =
    getSystemClassLoader().getResources(name)

  def getSystemResourceAsStream(name: String): java.io.InputStream =
    getSystemClassLoader().getResourceAsStream(name)

  private object NativeClassLoader extends ClassLoader()

}
