package java.nio.file

import java.nio.file.spi.FileSystemProvider

object CreateFileSystem {
    def apply(): FileSystem = null       
}

object CreateFileSystemProvider {
    def apply(): FileSystemProvider = null
}