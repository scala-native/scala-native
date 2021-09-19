package java.lang.impl

import scala.annotation.tailrec
import scala.scalanative.posix.errno.EINTR
import scala.scalanative.posix.time._
import scala.scalanative.posix.timeOps._
import scala.scalanative.unsafe._
import scala.scalanative.posix.unistd
import scala.scalanative.libc.errno

private[lang] object PosixThread {
  def sleep(millis: scala.Long, nanos: scala.Int): Unit = {
    @tailrec
    def doSleep(requestedTime: Ptr[timespec]): Unit = {
      val remaining = stackalloc[timespec]
      unistd.nanosleep(requestedTime, remaining) match {
        case _ if Thread.interrupted() =>
          throw new InterruptedException("Sleep was interrupted")

        case -1 if errno.errno == EINTR =>
          doSleep(remaining)

        case _ => ()
      }
    }

    val requestedTime = stackalloc[timespec]
    requestedTime.tv_sec = millis / 1000
    requestedTime.tv_nsec = (millis % 1000) * 1e6.toInt + nanos
    doSleep(requestedTime)
  }

}
