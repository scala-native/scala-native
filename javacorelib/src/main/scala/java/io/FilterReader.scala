package java.io

// Ported from Apache Harmony

abstract class FilterReader protected (in: Reader) extends Reader {

  override def mark(readLimit: Int): Unit = lock.synchronized {
    in.mark(readLimit)
  }

  override def markSupported(): Boolean = lock.synchronized {
    in.markSupported()
  }

  override def read(): Int = lock.synchronized {
    in.read()
  }

  override def read(buffer: Array[Char], offset: Int, count: Int): Int =
    lock.synchronized {
      in.read(buffer, offset, count)
    }

  override def ready(): Boolean = lock.synchronized {
    in.ready()
  }

  override def reset(): Unit = lock.synchronized {
    in.reset()
  }

  override def skip(count: Long): Long = lock.synchronized {
    in.skip(count)
  }
}
