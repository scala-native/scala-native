package salty.tools.linker

import scala.collection.mutable
import salty.ir._

final class ClasspathDeserializer(classpath: Classpath) { self =>
  var externs: mutable.Map[Name, Node] = mutable.Map.empty

  final class LinkingDeserializer(path: String) extends BinaryDeserializer(path) {
    override def extern(name: Name) =
      self.resolve(name).getOrElse {
        externs.get(name).getOrElse {
          val ext = Extern(name)
          externs += (name -> ext)
          ext
        }
      }
  }

  val paths = classpath.resolve.toMap

  val deserializers = mutable.Map.empty[Name, LinkingDeserializer]

  def deserializer(name: Name): Option[BinaryDeserializer] = {
    def root(name: Name): Name = name match {
      case Name.No              => throw new Exception("can't resolve unnamed node")
      case name: Name.Simple    => name
      case Name.Nested(left, _) => root(left)
    }
    val rt = root(name)
    deserializers.get(rt).map(Some(_)).getOrElse {
      paths.get(rt).map { path =>
        val de = new LinkingDeserializer(path)
        deserializers += (rt -> de)
        de
      }
    }
  }

  def resolve(name: Name): Option[Node] =
    deserializer(name).flatMap(_.resolve(name))
}
object ClasspathDeserializer {
  def load(classpath: Classpath, entryPoints: Seq[Name]): Scope = {
    val deserializer = new ClasspathDeserializer(classpath)
    val nodemap = entryPoints.map(n => (n, deserializer.resolve(n).get)).toMap
    Scope(nodemap)
  }
}
