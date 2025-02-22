package java.lang

import java.io.{InputStream, OutputStream}
import java.util.Optional
import java.util.concurrent.TimeUnit
import java.util.stream.Stream

abstract class Process {

  /** @since JDK 9 */
  def children(): Stream[ProcessHandle] =
    toHandle().children()

  /** @since JDK 9 */
  def descendants(): Stream[ProcessHandle] =
    toHandle().descendants()

  def destroy(): Unit

  def destroyForcibly(): Process // See SN Issue 4233, should be concrete.

  def exitValue(): Int

  def getErrorStream(): InputStream

  def getInputStream(): InputStream

  def getOutputStream(): OutputStream

  /** @since JDK 9 */
  def info(): ProcessHandle.Info =
    toHandle().info()

  def isAlive(): scala.Boolean

  // Since: JDK 9
  // Not yet implemented - a good candidate for one of Herakles' 12 labors.
  // import java.util.concurrent.CompletableFuture
  // def onExit(): CompletableFuture[ProcessHandle]

  /** @since JDK 9 */
  def pid(): scala.Long =
    toHandle().pid()

  /** @since JDK 9 */
  def supportsNormalTermination(): scala.Boolean =
    throw new UnsupportedOperationException(
      "Process.supportsNormalTermination()"
    )

  /** @since JDK 9 */
  def toHandle(): ProcessHandle =
    throw new UnsupportedOperationException("Process.toHandle()")

  def waitFor(): Int

  def waitFor(timeout: scala.Long, unit: TimeUnit): scala.Boolean
}
