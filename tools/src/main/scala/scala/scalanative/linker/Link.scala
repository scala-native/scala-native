package scala.scalanative
package linker

import scalanative.nir._
import scalanative.util.Scope
import scala.scalanative.io.VirtualDirectory
import java.nio.file.Path

object Link {

  private val cache =
    collection.mutable.HashMap[(Path, Seq[Global], Seq[Long]), Result]()

  /** Load all clases and methods reachable from the entry points. */
  def apply(config: build.Config, entries: Seq[Global])(implicit
      scope: Scope
  ): Result = {
    val classpath = config.classPath.map { path =>
      ClassPath(VirtualDirectory.real(path))
    }
    val classPathMtime = classpath.map(_.lastModifiedMillis)
    val key = (config.buildPath, entries, classPathMtime)
    cache.get(key) match {
      case None =>
        val res = Reach(config, entries, ClassLoader.fromClasspath(classpath))
        cache.update(key, res)
        res
      case Some(res) =>
        config.logger.info(s"NIR linking skipped due to cache hit.")
        res
    }
  }

  /** Run reachability analysis on already loaded methods. */
  def apply(
      config: build.Config,
      entries: Seq[Global],
      defns: Seq[Defn]
  ): Result =
    Reach(config, entries, ClassLoader.fromMemory(defns))
}
