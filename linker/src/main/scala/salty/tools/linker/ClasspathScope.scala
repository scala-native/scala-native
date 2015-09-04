package salty.tools.linker

import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import scala.collection.{mutable => mut}
import salty.ir._
import salty.ir.Deserializers._

final class ClasspathScope(classpath: Classpath) {
  private final case class Persisted(name: Name, path: String)

  private val loaded: mut.Map[Name, Defn] =
    mut.Map.empty
  private val persisted: mut.Map[Name, Persisted] =
    mut.Map(classpath.resolve.map { case (name, path) =>
      name -> Persisted(name, path)
    }: _*)

  private def fetch(persisted: Persisted): Scope = {
    val file = new RandomAccessFile(persisted.path, "r")
    val channel = file.getChannel
    val buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size())
    buffer.load
    val scope = buffer.getScope
    buffer.clear
    channel.close
    scope
  }

  def resolve(name: Name): Option[Defn] =
    loaded.get(name) match {
      case None =>
        persisted.get(name).map { p =>
          val Scope(fetched) = fetch(p)
          loaded ++= fetched
          persisted -= name
          fetched(name)
        }
      case some @ Some(_) =>
        some
    }
}
