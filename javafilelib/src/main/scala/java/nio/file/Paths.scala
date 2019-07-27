package java.nio.file

import java.net.URI

object Paths {
  private lazy val fs = FileSystems.getDefault()
  def get(first: String, more: Array[String]): Path =
    fs.getPath(first, more)

  def get(uri: URI): Path =
    if (uri.getScheme() == null) {
      throw new IllegalArgumentException("Missing scheme")
    } else if (uri.getScheme().toLowerCase == "file") {
      fs.getPath(uri.getPath(), Array.empty)
    } else {
      throw new FileSystemNotFoundException(
        s"Provider ${uri.getScheme()} is not installed.")
    }
}
