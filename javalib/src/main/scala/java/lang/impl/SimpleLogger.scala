package java.lang
package impl

import System.Logger
import scalanative.posix.time._
import scalanative.windows.crt.{time => winTime}
import scalanative.unsafe._
import scalanative.unsigned._
import scala.scalanative.meta.LinktimeInfo.isWindows
import scala.scalanative.runtime.javalib.Proxy

// Simple default logger implementation
private[lang] class SimpleLogger(name: String) extends Logger {
  def getName(): String = name

  def isLoggable(level: Logger.Level): scala.Boolean = level match {
    case Logger.Level.OFF => false
    case _                => true
  }

  override def log(
      level: Logger.Level,
      bundle: java.util.ResourceBundle,
      format: String,
      params: scala.Array[Object]
  ): Unit = {
    if (isLoggable(level)) {
      val message = if (bundle != null && bundle.containsKey(format)) {
        val pattern = bundle.getString(format)
        String.format(pattern, params: _*)
      } else {
        String.format(format, params: _*)
      }
      doLog(level, message)
    }
  }

  def log(
      level: Logger.Level,
      bundle: java.util.ResourceBundle,
      msg: String,
      thrown: Throwable
  ): Unit = {
    if (isLoggable(level)) {
      val message = if (bundle != null && bundle.containsKey(msg)) {
        bundle.getString(msg)
      } else {
        msg
      }
      doLog(level, message)
      if (thrown != null) {
        thrown.printStackTrace(System.err)
      }
    }
  }

  private def doLog(level: Logger.Level, message: String): Unit = {
    System.err.println(
      s"${currentDate()} ${currentCallsite()}\n[$level]: $message"
    )
  }

  private def currentCallsite(): String = {
    val callSiteFrames = Proxy
      .stackTraceIterator()
      .dropWhile { elem =>
        elem.getClassName.startsWith("java.lang.")
      }
    if (!callSiteFrames.hasNext) {
      return ""
    }
    val frame = callSiteFrames.next()
    s"${frame.getClassName} ${frame.getMethodName}"
  }

  private def currentDate(): String = {
    def fallback = System.currentTimeMillis().toString()
    val milliseconds = System.currentTimeMillis()
    val seconds = milliseconds / 1000L
    val ttPtr = stackalloc[time_t]()
    !ttPtr = seconds.toSize

    val tmPtr = stackalloc[tm]()
    def getLocalTime() =
      if (isWindows) winTime.localtime_s(tmPtr, ttPtr) != 0
      else localtime_r(ttPtr, tmPtr) == null

    if (getLocalTime()) {
      return fallback
    }

    val bufSize = 50.toUSize
    val buf: Ptr[CChar] = stackalloc[CChar](bufSize)

    val n =
      if (isWindows)
        winTime.strftime(buf, bufSize, c"%b %d, %Y %I:%M:%S %p", tmPtr)
      else
        strftime(buf, bufSize, c"%b %d, %Y %I:%M:%S %p", tmPtr)

    if (n.toInt == 0) fallback
    else fromCString(buf)
  }
}
