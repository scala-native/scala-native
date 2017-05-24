package java.nio.file

import java.nio.file.spi.FileSystemProvider

import java.net.URI
import java.util.{HashMap, Map}

import scala.scalanative.nio.fs.{UnixFileSystem, UnixFileSystemProvider}

object FileSystems {
  private lazy val fs =
    (new UnixFileSystemProvider).getFileSystem(
      new URI(scheme = "file",
              userInfo = null,
              host = null,
              port = -1,
              path = "/",
              query = null,
              fragment = null))
  def getDefault(): FileSystem =
    fs

  def getFileSystem(uri: URI): FileSystem = {
    val provider = findProvider(uri)
    provider.getFileSystem(uri)
  }

  def newFileSystem(path: Path, loader: ClassLoader): FileSystem = {
    val providers              = FileSystemProvider.installedProviders
    val map                    = new HashMap[String, Object]()
    var i                      = 0
    var fs: Option[FileSystem] = None
    while (i < providers.size && fs.isEmpty) {
      try {
        fs = Some(providers.get(i).newFileSystem(path, map))
      } catch {
        case _: Throwable => ()
      }
      i += 1
    }
    fs.getOrElse(throw new ProviderNotFoundException)
  }

  def newFileSystem(uri: URI, env: Map[String, _]): FileSystem = {
    val provider = findProvider(uri)
    provider.newFileSystem(uri, env)
  }

  def newFileSystem(uri: URI,
                    env: Map[String, _],
                    loader: ClassLoader): FileSystem =
    newFileSystem(uri, env)

  private def findProvider(uri: URI): FileSystemProvider = {
    val providers                            = FileSystemProvider.installedProviders
    var provider: Option[FileSystemProvider] = None
    var i                                    = 0
    while (i < providers.size && provider.isEmpty) {
      if (providers.get(i).getScheme.equalsIgnoreCase(uri.getScheme)) {
        provider = Some(providers.get(i))
      }
      i += 1
    }

    provider.getOrElse(throw new ProviderNotFoundException)
  }

}
