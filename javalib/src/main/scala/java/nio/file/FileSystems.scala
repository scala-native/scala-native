package java.nio.file

import java.net.URI
import java.nio.file.spi.FileSystemProvider
import java.util.{HashMap, Map}

import scala.scalanative.meta.LinktimeInfo.isWindows
import scala.scalanative.nio.fs.unix.UnixFileSystemProvider
import scala.scalanative.nio.fs.windows.WindowsFileSystemProvider

object FileSystems {
  private lazy val fs = {
    val provider =
      if (isWindows) new WindowsFileSystemProvider()
      else new UnixFileSystemProvider()

    provider.getFileSystem(
      new URI(
        scheme = "file",
        userInfo = null,
        host = null,
        port = -1,
        path = "/",
        query = null,
        fragment = null
      )
    )
  }
  def getDefault(): FileSystem = fs

  def getFileSystem(uri: URI): FileSystem = {
    val provider = findProvider(uri)
    provider.getFileSystem(uri)
  }

  def newFileSystem(path: Path, loader: ClassLoader): FileSystem = {
    val providers = FileSystemProvider.installedProviders
    val map = new HashMap[String, Object]()
    var i = 0
    var fs: Option[FileSystem] = None
    // Only `UnsupportedOperationException` is interpreted as "this provider
    // doesn't claim this path"; everything else (ZipException via IOException,
    // FileSystemAlreadyExistsException, etc.) is a real failure and must
    // surface to the caller — otherwise broken archives masquerade as
    // ProviderNotFoundException.
    var firstError: Throwable = null
    while (i < providers.size() && fs.isEmpty) {
      try {
        fs = Some(providers.get(i).newFileSystem(path, map))
      } catch {
        case _: UnsupportedOperationException => ()
        case t: Throwable                     =>
          if (firstError == null) firstError = t
      }
      i += 1
    }
    fs.getOrElse {
      if (firstError != null) throw firstError
      else throw new ProviderNotFoundException
    }
  }

  def newFileSystem(uri: URI, env: Map[String, _]): FileSystem = {
    val provider = findProvider(uri)
    provider.newFileSystem(uri, env)
  }

  def newFileSystem(
      uri: URI,
      env: Map[String, _],
      loader: ClassLoader
  ): FileSystem =
    newFileSystem(uri, env)

  private def findProvider(uri: URI): FileSystemProvider = {
    val providers = FileSystemProvider.installedProviders
    var provider: Option[FileSystemProvider] = None
    var i = 0
    while (i < providers.size() && provider.isEmpty) {
      if (providers.get(i).getScheme().equalsIgnoreCase(uri.getScheme())) {
        provider = Some(providers.get(i))
      }
      i += 1
    }

    provider.getOrElse(throw new ProviderNotFoundException)
  }

}
