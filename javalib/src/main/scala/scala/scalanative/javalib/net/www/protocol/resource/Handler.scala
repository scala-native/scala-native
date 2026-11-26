package scala.scalanative.javalib.net.www.protocol.resource

import java.io.{FileNotFoundException, IOException, InputStream}
import java.net.{Proxy, URI, URL, URLConnection, URLStreamHandler}

import scala.scalanative.javalib.net.www.protocol.file.FileURLConnection

/** `resource:/<path>` URLs of resources embedded at link time. */
final class Handler extends URLStreamHandler {

  @throws[IOException]
  override def openConnection(u: URL): URLConnection =
    new ResourceURLConnection(u)

  @throws[IOException]
  override def openConnection(u: URL, p: Proxy): URLConnection =
    openConnection(u)
}

object Handler {

  /** The URL of the embedded resource with an absolute `path`, or `null`. */
  def urlOf(path: String): URL = {
    val in = open(path)
    if (in == null) null
    else {
      in.close()
      new URI("resource", null, path, null).toURL()
    }
  }

  private[resource] def open(path: String): InputStream =
    classOf[Object].getResourceAsStream(path)
}

final class ResourceURLConnection(u: URL) extends URLConnection(u) {

  private val path =
    FileURLConnection.decode(if (u.getPath() == null) "" else u.getPath())
  private var is: InputStream = null

  @throws[IOException]
  override def connect(): Unit = if (!connected) {
    is = if (path.startsWith("/")) Handler.open(path) else null
    if (is == null) throw new FileNotFoundException(path)
    connected = true
  }

  @throws[IOException]
  override def getInputStream(): InputStream = {
    connect()
    is
  }

  override def getContentType(): String = {
    val t = URLConnection.guessContentTypeFromName(path)
    if (t == null) "content/unknown" else t
  }
}
