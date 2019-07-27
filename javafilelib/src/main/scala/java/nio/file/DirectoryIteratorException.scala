package java.nio.file

import java.io.IOException
import java.util.ConcurrentModificationException

class DirectoryIteratorException(override val getCause: IOException)
    extends ConcurrentModificationException(getCause)
