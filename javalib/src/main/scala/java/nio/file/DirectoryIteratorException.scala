package java.nio.file

import java.io.IOException
import java.util.{ConcurrentModificationException, Objects}

final class DirectoryIteratorException(cause: IOException)
    extends ConcurrentModificationException(
      Objects.requireNonNull(cause).toString,
      cause
    ) {
  override def getCause(): IOException =
    super.getCause().asInstanceOf[IOException]
}
