package scala.scalanative
package linker

import scala.collection.mutable
import scalanative.nir._
import scalanative.nir.serialization._
import scalanative.io.VirtualDirectory
import scalanative.util.Scope

import ReflectiveProxy._

sealed trait Linker {

  /** Link the whole world under closed world assumption. */
  def link(entries: Seq[Global]): Result
}

object Linker {

  /** Create a new linker given tools configuration. */
  def apply(config: tools.Config,
            reporter: Reporter = Reporter.empty): Linker =
    new Impl(config, reporter)

  private final class Impl(config: tools.Config, reporter: Reporter)
      extends Linker {
    import reporter._

    def link(entries: Seq[Global]): Result = Scope { implicit in =>
      val resolved    = mutable.Set.empty[Global]
      val unresolved  = mutable.Set.empty[Global]
      val links       = mutable.Set.empty[Attr.Link]
      val defns       = mutable.UnrolledBuffer.empty[Defn]
      val direct      = mutable.Stack.empty[Global]
      var conditional = mutable.UnrolledBuffer.empty[Dep.Conditional]
      val weaks       = mutable.Set.empty[Global]
      val signatures  = mutable.Set.empty[String]
      val dyndefns    = mutable.Set.empty[Global]

      val paths = config.paths.map(p => ClassPath(VirtualDirectory.real(p)))
      def load(global: Global) =
        paths.collectFirst {
          case path if path.contains(global) =>
            path.load(global)
        }.flatten

      def processDirect =
        while (direct.nonEmpty) {
          val workitem = direct.pop()
          if (!workitem.isIntrinsic && !resolved.contains(workitem) &&
              !unresolved.contains(workitem)) {

            load(workitem).fold[Unit] {
              unresolved += workitem
              onUnresolved(workitem)
            } {
              case (deps, newlinks, newsignatures, defn) =>
                resolved += workitem
                defns += defn
                links ++= newlinks
                signatures ++= newsignatures

                // Comparing new signatures with already collected weak dependencies
                newsignatures
                  .flatMap(signature =>
                    weaks.collect {
                      case weak if Global.genSignature(weak) == signature =>
                        weak
                  })
                  .foreach { global =>
                    direct.push(global)
                    dyndefns += global
                  }

                onResolved(workitem)

                deps.foreach {
                  case Dep.Direct(dep) =>
                    direct.push(dep)
                    onDirectDependency(workitem, dep)

                  case cond @ Dep.Conditional(dep, condition) =>
                    conditional += cond
                    onConditionalDependency(workitem, dep, condition)

                  case Dep.Weak(global) =>
                    // comparing new dependencies with all signatures
                    if (signatures(Global.genSignature(global))) {
                      direct.push(global)
                      onDirectDependency(workitem, global)
                      dyndefns += global
                    }
                    weaks += global
                }

            }
          }
        }

      def processConditional = {
        val rest = mutable.UnrolledBuffer.empty[Dep.Conditional]

        conditional.foreach {
          case Dep.Conditional(dep, cond)
              if resolved.contains(dep) || unresolved.contains(dep) =>
            ()

          case Dep.Conditional(dep, cond) if resolved.contains(cond) =>
            direct.push(dep)

          case dep =>
            rest += dep
        }

        conditional = rest
      }

      onStart()

      entries.foreach { entry =>
        direct.push(entry)
        onEntry(entry)
      }

      while (direct.nonEmpty) {
        processDirect
        processConditional
      }

      val reflectiveProxies =
        genAllReflectiveProxies(dyndefns, defns)

      val defnss = defns ++ reflectiveProxies

      onComplete()

      Result(unresolved.toSeq,
             links.toSeq,
             defnss.sortBy(_.name.toString),
             signatures.toSeq)
    }
  }
}
