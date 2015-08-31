package salty.tools
package linker

import java.io._
import java.nio._
import java.nio.channels._

import salty.ir._, Deserializers.RichGet

class Linker {
  def load(paths: List[String]): Unit = paths.foreach { path =>
    try {
      val file = new RandomAccessFile(path, "r")
      val channel = file.getChannel
      val buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size())
      buffer.load()
      val res = buffer.getStat
      buffer.clear()
      channel.close()
      file.close()
    } catch {
      case _ =>
        println(s"failed with $path")
    }
  }
}
