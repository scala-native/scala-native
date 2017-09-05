package java.net

import scalanative.native.stub

class URLClassLoader(args: Array[Object], parent: ClassLoader)
    extends ClassLoader(parent) {
  @stub
  def getURLs(): Array[Object] = ???
  @stub
  def close(): Unit = ???
}
