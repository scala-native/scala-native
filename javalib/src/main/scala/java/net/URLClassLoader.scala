package java.net

class URLClassLoader(args: Array[Object], parent: ClassLoader)
    extends ClassLoader(parent) {
  def getURLs(): Array[Object] = ???
  def close(): Unit            = ???
}
