package salty.ir

import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import scala.collection.{mutable => mut}
import salty.ir.Deserializers._

sealed abstract class Scope {
  def resolve(name: Name): Option[Defn]
  def entries: Map[Name, Defn]
  def isEmpty: Boolean
}
object Scope {
  def apply(classpath: Classpath): Scope = new ClasspathScope(classpath)
  def apply(entries: Map[Name, Defn]): Scope = new MapScope(entries)
}

final class ClasspathScope(classpath: Classpath) extends Scope {
  private sealed abstract class Entry
  private final case class Loaded(stat: Defn) extends Entry
  private final case class Persisted(name: Name, path: String) extends Entry

  private val underlying: mut.Map[Name, Entry] =
    mut.Map(classpath.resolve.map { case (name, path) =>
      name -> Persisted(name, path)
    }: _*)

  private def fetch(persisted: Persisted): Defn = {
    val file = new RandomAccessFile(persisted.path, "r")
    val channel = file.getChannel
    val buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size())
    buffer.load
    val stat = buffer.getDefn
    buffer.clear
    channel.close
    stat
  }

  private def unwrap(entry: Entry): Defn = entry match {
    case p: Persisted =>
      val stat = fetch(p)
      underlying(p.name) = Loaded(stat)
      stat
    case l: Loaded =>
      l.stat
  }

  def resolve(name: Name): Option[Defn] =
    underlying.get(name).map(unwrap)

  def entries: Map[Name, Defn] =
    underlying.toIterator.map {
      case (name, entry) =>
        (name, unwrap(entry))
    }.toMap

  def isEmpty: Boolean = underlying.isEmpty
}

final class MapScope(val entries: Map[Name, Defn]) extends Scope {
  def resolve(name: Name): Option[Defn] = entries.get(name)
  def isEmpty = entries.isEmpty
}


