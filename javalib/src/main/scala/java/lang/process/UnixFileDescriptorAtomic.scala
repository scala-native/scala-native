package java.lang.process

import java.util.concurrent.atomic.AtomicInteger

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.posix.unistd

class UnixFileDescriptorAtomic(val value: AtomicInteger) extends AnyVal {

  @alwaysinline def get(): Int = value.get()

  @alwaysinline def release(): Int = value.getAndSet(-1)

  @alwaysinline def close(): Unit = {
    val prev = release()
    if (prev != -1) unistd.close(prev)
  }

}

object UnixFileDescriptorAtomic {

  @alwaysinline
  def apply(value: Int): UnixFileDescriptorAtomic =
    new UnixFileDescriptorAtomic(new AtomicInteger(value))

}
