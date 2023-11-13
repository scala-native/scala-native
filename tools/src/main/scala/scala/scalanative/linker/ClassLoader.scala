package scala.scalanative
package linker

import scala.collection.mutable
import scalanative.io.VirtualDirectory
import scalanative.util.Scope

sealed abstract class ClassLoader {

  def classesWithEntryPoints: Iterable[nir.Global.Top]

  def definedServicesProviders: Map[nir.Global.Top, Iterable[nir.Global.Top]]

  def load(global: nir.Global.Top): Option[Seq[nir.Defn]]

}

object ClassLoader {

  def fromDisk(config: build.Config)(implicit in: Scope): ClassLoader = {
    val classpath = config.classPath.map { path =>
      ClassPath(VirtualDirectory.real(path))
    }
    new FromDisk(classpath)
  }

  def fromMemory(defns: Seq[nir.Defn]): ClassLoader =
    new FromMemory(defns)

  final class FromDisk(classpath: Seq[ClassPath]) extends ClassLoader {
    lazy val classesWithEntryPoints: Iterable[nir.Global.Top] = {
      classpath.flatMap(_.classesWithEntryPoints)
    }
    lazy val definedServicesProviders
        : Map[nir.Global.Top, Iterable[nir.Global.Top]] =
      classpath.flatMap(_.definedServicesProviders).toMap

    def load(global: nir.Global.Top): Option[Seq[nir.Defn]] =
      classpath.collectFirst {
        case path if path.contains(global) =>
          path.load(global)
      }.flatten
  }

  final class FromMemory(defns: Seq[nir.Defn]) extends ClassLoader {

    private val scopes = {
      val out =
        mutable.Map.empty[nir.Global.Top, mutable.UnrolledBuffer[nir.Defn]]
      defns.foreach { defn =>
        val owner = defn.name.top
        val buf =
          out.getOrElseUpdate(owner, mutable.UnrolledBuffer.empty[nir.Defn])
        buf += defn
      }
      out
    }

    lazy val classesWithEntryPoints: Iterable[nir.Global.Top] = {
      scopes.filter {
        case (_, defns) => defns.exists(_.isEntryPoint)
      }.keySet
    }

    def definedServicesProviders
        : Map[nir.Global.Top, Iterable[nir.Global.Top]] =
      Map.empty

    def load(global: nir.Global.Top): Option[Seq[nir.Defn]] =
      scopes.get(global).map(_.toSeq)

  }

}
