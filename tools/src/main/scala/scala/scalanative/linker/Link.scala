package scala.scalanative
package linker

import scala.collection.mutable
import scalanative.nir._
import scalanative.nir.serialization._
import scalanative.io.VirtualDirectory
import scalanative.util.{Stats, Scope}

object Cache {
  val entries = mutable.Map.empty[java.nio.file.Path, ClassPath]
}

object Link {

  /** Create a new linker given tools configuration. */
  def apply(config: build.Config, entries: Seq[Global]): Result = {
    Stats.in(Stats.time("link") {
      val result =
        Stats.time("load") {
          Scope { implicit in =>
            (new Impl(config)).link(entries)
          }
        }
      Stats.time("reach") {
        Reachability(entries, result)
      }
    })
  }

  private final class Impl(config: build.Config)(implicit in: Scope) {
    val enqueued    = mutable.Set.empty[Global]
    val defns       = mutable.UnrolledBuffer.empty[Defn]
    val todo        = mutable.Stack.empty[Global]
    val unavailable = mutable.Set.empty[Global]

    val classpath = config.classPath.map { path =>
      Cache.entries.getOrElseUpdate(path, {
        ClassPath(VirtualDirectory.real(path))
      })
    }

    def load(global: Global) =
      classpath.collectFirst {
        case path if path.contains(global) =>
          path.load(global)
      }.flatten

    def push(global: Global): Unit = {
      if (!enqueued.contains(global)) {
        enqueued += global
        todo.push(global)
      }
    }

    def process(): Unit = {
      while (todo.nonEmpty) {
        val workitem = todo.pop()

        load(workitem)
          .fold[Unit] {
            unavailable += workitem
          } {
            case (newDeps, newDefns) =>
              newDeps.foreach(push)
              defns ++= newDefns
          }
      }
    }

    def link(entries: Seq[Global]): Result = {
      entries.foreach(push)
      process()

      Result.empty.withDefns(defns)
    }
  }
}
