package native
package compiler

import java.io.File
import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.{DirectoryFileFilter, RegexFileFilter}
import scala.collection.mutable
import native.ir._
import native.ir.serialization._

sealed trait Loader {
  def resolve(name: Name): Option[Node]
}

final class BuiltinLoader extends Loader {
  lazy val StringName            = Name.Class("java.lang.String")
  lazy val ClassName             = Name.Class("java.lang.Class")
  lazy val ObjectName            = Name.Class("java.lang.Object")
  lazy val ObjectConstructorName = Name.Constructor(ObjectName, Seq())
  lazy val ObjectCloneName       = Name.Method(ObjectName, "clone", Seq(), ObjectName)
  lazy val ObjectEqualsName      = Name.Method(ObjectName, "equals", Seq(ObjectName), Prim.Bool.name)
  lazy val ObjectFinalizeName    = Name.Method(ObjectName, "finalize", Seq(), Prim.Unit.name)
  lazy val ObjectGetClassName    = Name.Method(ObjectName, "getClass", Seq(), ClassName)
  lazy val ObjectHashCodeName    = Name.Method(ObjectName, "hashCode", Seq(), Prim.I32.name)
  lazy val ObjectNotifyName      = Name.Method(ObjectName, "notify", Seq(), Prim.Unit.name)
  lazy val ObjectNotifyAllName   = Name.Method(ObjectName, "notifyAll", Seq(), Prim.Unit.name)
  lazy val ObjectToStringName    = Name.Method(ObjectName, "toString", Seq(), StringName)
  lazy val ObjectWait0Name       = Name.Method(ObjectName, "wait", Seq(), Prim.Unit.name)
  lazy val ObjectWait1Name       = Name.Method(ObjectName, "wait", Seq(Prim.I64.name), Prim.Unit.name)
  lazy val ObjectWait2Name       = Name.Method(ObjectName, "wait", Seq(Prim.I64.name, Prim.I32.name), Prim.Unit.name)
  lazy val ThisName              = Name.Local("this")

  lazy val ObjectClass: Node =
    Defn.Class(Empty, Seq(), ObjectName)
  lazy val ObjectConstructor: Node =
    Defn.Method(Prim.Unit, Seq(Param(ObjectClass, ThisName)),
                End(Seq(Return(Empty, Empty, Lit.Unit()))),
                ObjectClass, ObjectConstructorName)

  lazy val StringClass: Node =
    Defn.Class(ObjectClass, Seq(), StringName)

  def resolve(name: Name): Option[Node] = name match {
    case `ObjectName`            => Some(ObjectClass)
    case `ObjectConstructorName` => Some(ObjectConstructor)
    case `ObjectCloneName`       => ???
    case `ObjectEqualsName`      => ???
    case `ObjectFinalizeName`    => ???
    case `ObjectGetClassName`    => ???
    case `ObjectHashCodeName`    => ???
    case `ObjectNotifyName`      => ???
    case `ObjectNotifyAllName`   => ???
    case `ObjectToStringName`    => ???
    case `ObjectWait0Name`       => ???
    case `ObjectWait1Name`       => ???
    case `ObjectWait2Name`       => ???
    case `StringName`            => Some(StringClass)
    case _                       => None
  }
}

final class SaltyLoader(cp: ClasspathLoader, path: String) extends SaltyDeserializer(path) with Loader {
  override def extern(attrs: Seq[Attr]) = {
    val name = attrs.collectFirst { case n: Name => n }.get
    cp.resolve(name).getOrElse {
      cp.externs.get(name).getOrElse {
        val ext = Defn.Extern(name)
        cp.externs += (name -> ext)
        ext
      }
    }
  }
}

// TODO: rewrite using nio-only
// TODO: replace with something less naive in the future
final class ClasspathLoader(val paths: Seq[String]) extends Loader { self =>
  var externs: mutable.Map[Name, Node] = mutable.Map.empty
  val builtins = new BuiltinLoader
  val loaders = mutable.Map[Name, Loader](
    builtins.ObjectName -> builtins,
    builtins.ClassName -> builtins,
    builtins.StringName -> builtins
  )

  lazy val pathmap: Map[Name, String] =
    paths.flatMap { path =>
      val base = new File(path)
      val baseabs = base.getAbsolutePath()
      val files =
        FileUtils.listFiles(
          base,
          new RegexFileFilter("(.*)\\.gir"),
          DirectoryFileFilter.DIRECTORY).toArray.toIterator
      files.map { case file: File =>
        val fileabs = file.getAbsolutePath()
        val relpath = fileabs.replace(baseabs + "/", "")
        val (nm, rel) =
          if (relpath.endsWith(".class.gir"))
            (Name.Class, relpath.replace(".class.gir", ""))
          // TODO: it might be better to strip $ in plugin
          else if (relpath.endsWith("$.module.gir"))
            (Name.Module, relpath.replace("$.module.gir", ""))
          else if (relpath.endsWith(".module.gir"))
            (Name.Module, relpath.replace(".module.gir", ""))
          else if (relpath.endsWith(".interface.gir"))
            (Name.Interface, relpath.replace(".interface.gir", ""))
          else
            throw new Exception(s"can't parse file kind $relpath")
        val parts = rel.split("/").toSeq
        val name = nm(parts.mkString("."))
        (name -> fileabs)
      }.toSeq
    }.toMap

  def root(name: Name): Name = name match {
    case _: Name.Class               |
         _: Name.Interface           |
         _: Name.Module              => name
    case Name.Field(owner, _)        => root(owner)
    case Name.Constructor(owner, _)  => root(owner)
    case Name.Method(owner, _, _, _) => root(owner)
    case _                           => throw new Exception("unreachable")
  }

  def loaderForName(rt: Name): Option[Loader] = {
    loaders.get(rt).map(Some(_)).getOrElse {
      pathmap.get(rt).map { path =>
        val loader = new SaltyLoader(this, path)
        loaders += (rt -> loader)
        loader
      }
    }
  }

  def relatives(node: Node): Option[Seq[Name]] =
    node match {
      case Defn.Class(parent, ifaces)     => Some((parent +: ifaces).map(_.name))
      case Defn.Module(parent, ifaces, _) => Some((parent +: ifaces).map(_.name))
      case Defn.Interface(ifaces)         => Some(ifaces.map(_.name))
      case _                              => None
    }

  def chown(name: Name, owner: Name): Name =
    name match {
      case Name.Field(_, id)             => Name.Field(owner, id)
      case Name.Method(_, id, args, ret) => Name.Method(owner, id, args, ret)
      case _                             => throw new Exception("unreachable")
    }

  def resolve(name: Name): Option[Node] = {
    val rt = root(name)
    loaderForName(rt).flatMap { loader =>
      loader.resolve(name).map(Some(_)).getOrElse {
        println(s"name $name, root $rt")
        val rels = relatives(loader.resolve(rt).get).get
        rels.reverse.map { pname =>
          resolve(chown(name, pname))
        }.headOption.flatten
      }
    }
  }
}
