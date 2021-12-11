package java.nio.file

import java.io.IOException
import java.util.ConcurrentModificationException

class DirectoryIteratorException(cause: IOException)
    extends ConcurrentModificationException(cause)
