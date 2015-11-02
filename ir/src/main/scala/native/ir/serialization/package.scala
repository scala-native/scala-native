package native
package ir

import java.nio._
import java.nio.file._
import java.nio.file.{StandardOpenOption => OpenOpt}
import java.nio.channels._

package object serialization {
  private val defaultBuffer = ByteBuffer.allocateDirect(128 * 1024 * 1024)

  def serializeDot(scope: Scope, buffer: ByteBuffer): Unit =
    buffer.put(DotSerializer.showScope(scope).toString.getBytes)

  def serializeSalty(scope: Scope, buffer: ByteBuffer): Unit =
    (new SaltySerializer(buffer)).serialize(scope)

  def serializeFile(serialize: (Scope, ByteBuffer) => Unit, scope: Scope, path: String,
                    buffer: ByteBuffer = defaultBuffer): Unit = {
    buffer.clear
    serialize(scope, buffer)
    buffer.flip
    val channel =
      FileChannel.open(Paths.get(path), OpenOpt.CREATE, OpenOpt.WRITE, OpenOpt.TRUNCATE_EXISTING)
    try channel.write(buffer)
    finally channel.close
  }

  def serializeDotFile(scope: Scope, path: String,
                       buffer: ByteBuffer = defaultBuffer): Unit =
    serializeFile(serializeDot, scope, path, buffer)

  def serializeIRFile(scope: Scope, path: String,
                         buffer: ByteBuffer = defaultBuffer): Unit =
    serializeFile(serializeSalty, scope, path, buffer)
}
