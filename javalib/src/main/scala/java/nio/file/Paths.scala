package java.nio.file

import java.net.URI

object Paths {
  private lazy val fs = FileSystems.getDefault()
  def get(first: String, more: Array[String]): Path =
    fs.getPath(first, more)

  // TODO:
  // def get(uri: URI): Path =
  //   ???
}
