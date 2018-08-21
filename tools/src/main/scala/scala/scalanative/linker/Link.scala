package scala.scalanative
package linker

import scala.collection.mutable
import scalanative.nir._
import scalanative.nir.serialization._
import scalanative.io.VirtualDirectory
import scalanative.util.Scope

object Link {

  /** Create a new linker given tools configuration. */
  def apply(config: build.Config, entries: Seq[Global]): Result = {
    val result =
      Scope { implicit in =>
        (new Impl(config)).link(entries)
      }
    Reachability(entries, result)
  }

  private final class Impl(config: build.Config)(implicit in: Scope) {
    val enqueued    = mutable.Set.empty[Global]
    val unavailable = mutable.Set.empty[Global]
    val links       = mutable.Set.empty[Attr.Link]
    val defns       = mutable.UnrolledBuffer.empty[Defn]
    val direct      = mutable.Stack.empty[Global]
    var conditional = mutable.Map.empty[Global, mutable.Set[Global]]
    val weaks       = mutable.Map.empty[String, mutable.Set[Global]]
    val dynsigs     = mutable.Set.empty[String]
    val dynimpls    = mutable.Set.empty[Global]

    val classpath = config.classPath.map { path =>
      ClassPath(VirtualDirectory.real(path))
    }

    def load(global: Global) =
      classpath.collectFirst {
        case path if path.contains(global) =>
          path.load(global)
      }.flatten

    def pushDirect(global: Global): Unit = {
      if (!enqueued.contains(global)) {
        direct.push(global)
        enqueued += global
        if (conditional.contains(global)) {
          conditional(global).foreach(pushDirect)
          conditional -= global
        }
      }
    }

    def pushConditional(dep: Global, cond: Global): Unit = {
      if (enqueued.contains(dep)) {
        ()
      } else if (enqueued.contains(cond)) {
        pushDirect(dep)
      } else {
        val buf = conditional.get(cond).getOrElse(mutable.Set.empty[Global])
        buf += dep
        conditional(cond) = buf
      }
    }

    def process(): Unit = {
      while (direct.nonEmpty) {
        val workitem = direct.pop()

        load(workitem)
          .fold[Unit] {
            unavailable += workitem
          } {
            // If this definition is a stub, and linking stubs is disabled,
            // then add this element to the `unavailable` items.
            case (_, _, _, defn) if defn.attrs.isStub && !config.linkStubs =>
              unavailable += workitem

            case (deps, newlinks, newdynsigs, defn) =>
              defns += defn
              links ++= newlinks
              dynsigs ++= newdynsigs

              // Comparing new dynsigs with already collected weak dependencies
              newdynsigs.foreach { signature =>
                if (weaks.contains(signature)) {
                  weaks(signature).foreach { global =>
                    pushDirect(global)
                    dynimpls += global
                  }
                }
              }

              deps.foreach {
                case Dep.Direct(dep) =>
                  pushDirect(dep)

                case cond @ Dep.Conditional(dep, condition) =>
                  pushConditional(dep, condition)

                case Dep.Weak(global) =>
                  // comparing new dependencies with all dynsigs
                  val sig = Global.genSignature(global)
                  if (dynsigs.contains(sig)) {
                    pushDirect(global)
                    dynimpls += global
                  }
                  val buf =
                    weaks.get(sig).getOrElse(mutable.Set.empty[Global])
                  buf += global
                  weaks(sig) = buf
              }

          }
      }
    }

    def link(entries: Seq[Global]): Result = {
      entries.foreach(pushDirect)
      process()

      Result(unavailable.toSeq,
             links.toSeq,
             defns,
             dynsigs.toSeq,
             dynimpls.toSeq)
    }
  }
}
