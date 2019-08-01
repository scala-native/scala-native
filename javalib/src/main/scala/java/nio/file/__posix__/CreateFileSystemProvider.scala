package java.nio.file

import java.nio.file.spi.FileSystemProvider

import scala.scalanative.nio.fs.{UnixFileSystem, UnixFileSystemProvider}

object CreateFileSystem {
    def apply(): FileSystem = (new UnixFileSystemProvider).getFileSystem(
                                        new URI(scheme = "file",
                                            userInfo = null,
                                            host = null,
                                            port = -1,
                                            path = "/",
                                            query = null,
                                            fragment = null))        
}

object CreateFileSystemProvider {
    def apply(): FileSystemProvider = new UnixFileSystemProvider)
}