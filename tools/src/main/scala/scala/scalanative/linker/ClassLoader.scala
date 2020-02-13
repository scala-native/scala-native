package scala.scalanative
package linker

import scala.collection.mutable
import scalanative.nir._
import scalanative.io.VirtualDirectory
import scalanative.util.Scope

sealed abstract class ClassLoader {
  def classesWithEntryPoints(): Iterable[Global]

  def load(global: Global): Option[Seq[Defn]]
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
    def classesWithEntryPoints(): Iterable[Global] = {
      classpath.flatMap(_.classesWithEntryPoints())
    }

    def load(global: Global) =
      classpath.collectFirst {
        case path if path.contains(global) =>
          path.load(global)
      }.flatten
  }

  final class FromMemory(defns: Seq[Defn]) extends ClassLoader {
    private val scopes = {
      val out = mutable.Map.empty[Global, mutable.UnrolledBuffer[Defn]]
      defns.foreach { defn =>
        val owner = defn.name.top
        val buf   = out.getOrElseUpdate(owner, mutable.UnrolledBuffer.empty[Defn])
        buf += defn
      }
      out
    }

    def classesWithEntryPoints(): Iterable[Global] = {
      scopes.filter {
        case (top, defns) =>
          defns.exists {
            case defn: Defn.Define =>
              val Global.Member(_, sig) = defn.name
              sig.isClinit
            case _ => false
          }
      }.keySet
    }

    def load(global: Global): Option[Seq[Defn]] =
      scopes.get(global)
  }
}
