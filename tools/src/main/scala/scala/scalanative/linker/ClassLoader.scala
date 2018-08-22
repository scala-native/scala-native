package scala.scalanative
package linker

import scalanative.nir._
import scalanative.io.VirtualDirectory
import scalanative.util.Scope

final class ClassLoader(classpath: Seq[ClassPath]) {

  def load(global: Global) =
    classpath.collectFirst {
      case path if path.contains(global) =>
        path.load(global)
    }.flatten
}

object ClassLoader {

  def apply(config: build.Config)(implicit in: Scope): ClassLoader = {
    val classpath = config.classPath.map { path =>
      ClassPath(VirtualDirectory.real(path))
    }
    new ClassLoader(classpath)
  }
}
