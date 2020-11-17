package java.lang

import java.io.{InputStream, OutputStream}
import java.util.concurrent.TimeUnit

abstract class Process {
  def destroy(): Unit

  def destroyForcibly(): Process

  def exitValue(): Int

  def getErrorStream(): InputStream

  def getInputStream(): InputStream

  def getOutputStream(): OutputStream

  def isAlive(): scala.Boolean

  def waitFor(): Int

  def waitFor(timeout: scala.Long, unit: TimeUnit): scala.Boolean
}
