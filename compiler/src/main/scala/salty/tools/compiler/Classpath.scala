package salty.tools.compiler

import java.io.File
import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.{DirectoryFileFilter, RegexFileFilter}
import scala.collection.mutable
import salty.ir._
import salty.ir.serialization._

// TODO: rewrite using nio-only
// TODO: replace with something less naive in the future
final case class Classpath(val paths: Seq[String]) { self =>
  var externs: mutable.Map[Name, Node] = mutable.Map.empty
  val deserializers = mutable.Map.empty[Name, LinkingDeserializer]

  lazy val pathmap: Map[Name, String] =
    paths.flatMap { path =>
      val base = new File(path)
      val baseabs = base.getAbsolutePath()
      val files =
        FileUtils.listFiles(
          base,
          new RegexFileFilter("(.*)\\.salty"),
          DirectoryFileFilter.DIRECTORY).toArray.toIterator
      files.map { case file: File =>
        val fileabs = file.getAbsolutePath()
        val relpath = fileabs.replace(baseabs + "/", "")
        val (nm, rel) =
          if (relpath.endsWith(".class.salty"))
            (Name.Class, relpath.replace(".class.salty", ""))
          else if (relpath.endsWith("$.module.salty"))
            (Name.Module, relpath.replace("$.module.salty", ""))
          else if (relpath.endsWith(".module.salty"))
            (Name.Module, relpath.replace(".module.salty", ""))
          else if (relpath.endsWith(".interface.salty"))
            (Name.Interface, relpath.replace(".interface.salty", ""))
          else
            throw new Exception(s"can't parse file kind $relpath")
        val parts = rel.split("/").toSeq
        val name = nm(parts.mkString("."))
        (name -> fileabs)
      }.toSeq
    }.toMap

  final class LinkingDeserializer(path: String) extends SaltyDeserializer(path) {
    override def extern(name: Name) =
      self.resolve(name).getOrElse {
        externs.get(name).getOrElse {
          val ext = Extern(name)
          externs += (name -> ext)
          ext
        }
      }
  }

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

  def deserializer(rt: Name): Option[SaltyDeserializer] = {
    deserializers.get(rt).map(Some(_)).getOrElse {
      pathmap.get(rt).map { path =>
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
