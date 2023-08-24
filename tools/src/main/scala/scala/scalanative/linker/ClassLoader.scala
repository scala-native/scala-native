package scala.scalanative
package linker

import scala.collection.mutable
import scalanative.nir._
import scalanative.io.VirtualDirectory
import scalanative.util.Scope

sealed abstract class ClassLoader {
  def classesWithEntryPoints: Iterable[Global.Top]

  def load(global: Global.Top): Option[Seq[Defn]]
}

object ClassLoader {

  def fromDisk(config: build.Config)(implicit in: Scope): ClassLoader = {
    val classpath = config.classPath.map { path =>
      ClassPath(VirtualDirectory.real(path))
    }
    new FromDisk(classpath)
  }

  def fromMemory(defns: Seq[Defn]): ClassLoader =
    new FromMemory(defns)

  final class FromDisk(classpath: Seq[ClassPath]) extends ClassLoader {
    lazy val classesWithEntryPoints: Iterable[Global.Top] = {
      classpath.flatMap(_.classesWithEntryPoints)
    }

    def load(global: Global.Top): Option[Seq[Defn]] =
      classpath.collectFirst {
        case path if path.contains(global) =>
          path.load(global)
      }.flatten
  }

  final class FromMemory(defns: Seq[Defn]) extends ClassLoader {
    private val scopes = {
      val out = mutable.Map.empty[Global.Top, mutable.UnrolledBuffer[Defn]]
      defns.foreach { defn =>
        val owner = defn.name.top
        val buf = out.getOrElseUpdate(owner, mutable.UnrolledBuffer.empty[Defn])
        buf += defn
      }
      out
    }

    lazy val classesWithEntryPoints: Iterable[Global.Top] = {
      scopes.filter {
        case (_, defns) => defns.exists(_.isEntryPoint)
      }.keySet
    }

    def load(global: Global.Top): Option[Seq[Defn]] =
      scopes.get(global).map(_.toSeq)
  }
}
