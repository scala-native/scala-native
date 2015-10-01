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

  def root(name: Name): Name = name match {
    case Name.No           |
         _: Name.Slice     |
         _: Name.Primitive => throw new Exception("unreachable")
    case _: Name.Class     |
         _: Name.Interface |
         _: Name.Module    => name
    case Name.Field(owner, _) => root(owner)
    case Name.Method(owner, _, _, _) => root(owner)
  }

  def deserializer(rt: Name): Option[BinaryDeserializer] = {
    deserializers.get(rt).map(Some(_)).getOrElse {
      paths.get(rt).map { path =>
        val de = new LinkingDeserializer(path)
        deserializers += (rt -> de)
        de
      }
    }
  }

  def defnName(node: Node) =
    node match {
      case Class(name, _)     => name
      case Module(name, _)    => name
      case Interface(name, _) => name
    }

  def relatives(node: Node): Option[Seq[Name]] =
    node match {
      case Class(_, rels)     => Some(rels.map(defnName))
      case Module(_, rels)    => Some(rels.map(defnName))
      case Interface(_, rels) => Some(rels.map(defnName))
      case _                  => None
    }

  def chown(name: Name, owner: Name): Name =
    name match {
      case Name.Field(_, id)             => Name.Field(owner, id)
      case Name.Method(_, id, args, ret) => Name.Method(owner, id, args, ret)
    }

  def resolve(name: Name): Option[Node] = {
    val rt = root(name)
    deserializer(rt).flatMap { des =>
      des.resolve(name).map(Some(_)).getOrElse {
        val rels = relatives(des.resolve(rt).get).get
        rels.reverse.map { pname =>
          resolve(chown(name, pname))
        }.headOption.flatten
      }
    }
  }
}
object ClasspathDeserializer {
  def load(classpath: Classpath, entryPoints: Seq[Name]): Scope = {
    val deserializer = new ClasspathDeserializer(classpath)
    val nodemap = entryPoints.map(n => (n, deserializer.resolve(n).get)).toMap
    Scope(nodemap)
  }
}
