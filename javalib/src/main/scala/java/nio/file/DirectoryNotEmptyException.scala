package java.nio.file

class DirectoryNotEmptyException(file: String)
    extends FileSystemException(file, null, null)
