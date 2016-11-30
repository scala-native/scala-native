package scala.scalanative
package linker

import java.io.{File, PrintWriter}
import scala.collection.mutable
import nir._
import nir.serialization._
import util.Scope

sealed trait Linker {

  /** Link the whole world under closed world assumption. */
  def link(entries: Seq[Global]): (Seq[Global], Seq[Attr.Link], Seq[Defn])
}

object Linker {

  val fresh = new Fresh("proxy")

  /** Create a new linker given tools configuration. */
  def apply(config: tools.Config,
            reporter: Reporter = Reporter.empty): Linker =
    new Impl(config, reporter)

  private final class Impl(config: tools.Config, reporter: Reporter)
      extends Linker {
    import reporter._

    private def load(global: Global)
      : Option[(Seq[Dep], Seq[Attr.Link], Seq[String], Defn)] =
      config.paths.collectFirst {
        case path if path.contains(global) =>
          path.load(global)
      }.flatten

    def link(entries: Seq[Global]): (Seq[Global], Seq[Attr.Link], Seq[Defn]) = {
      val resolved          = mutable.Set.empty[Global]
      val unresolved        = mutable.Set.empty[Global]
      val links             = mutable.Set.empty[Attr.Link]
      val defns             = mutable.UnrolledBuffer.empty[Defn]
      val weak              = mutable.Set.empty[Global] // use multi map for constant lookup with new signatures ?
      val dyn               = mutable.Set.empty[String]
      val direct            = mutable.Stack.empty[Global]
      var conditional       = mutable.UnrolledBuffer.empty[Dep.Conditional]
      val structuralMethods = mutable.Set.empty[Global]

      def processDirect =
        while (direct.nonEmpty) {
          val workitem = direct.pop()
          if (!workitem.isIntrinsic && !resolved.contains(workitem) &&
              !unresolved.contains(workitem)) {

            load(workitem).fold[Unit] {
              unresolved += workitem
              onUnresolved(workitem)
            } {
              case (deps, newlinks, newDyns, defn) =>
                resolved += workitem
                defns += defn
                links ++= newlinks
                onResolved(workitem)

                dyn ++= newDyns

                // Comparing new signatures with old weak dependencies
                newDyns
                  .flatMap(s => weak.collect { case g if g.id == s => g })
                  .foreach { global =>
                    direct.push(global)
                    structuralMethods += global
                  }

                deps.foreach {
                  case Dep.Direct(dep) =>
                    direct.push(dep)
                    onDirectDependency(workitem, dep)

                  case cond @ Dep.Conditional(dep, condition) =>
                    conditional += cond
                    onConditionalDependency(workitem, dep, condition)

                  case Dep.Weak(g) =>
                    def genSignature(global: Global): String = {
                      val fullSignature = global.id
                      val index         = fullSignature.lastIndexOf("_")
                      if (index != -1) {
                        fullSignature.substring(0, index)
                      } else {
                        fullSignature
                      }
                    }
                    // comparing new dependencies with all signatures
                    if (dyn(genSignature(g))) {
                      direct.push(g)
                      structuralMethods += g
                    }

                    weak += g
                }

            }
          }
        }

      def genReflProxy(defn: Defn.Define): Defn.Define = {
        val Global.Member(owner, id) = defn.name
        val defnType                 = defn.ty.asInstanceOf[Type.Function]

        val ty = Type.Function(defnType.args, defnType.ret match {
          case Type.Unit => Type.Unit
          case _         => Type.Class(Global.Top("java.lang.Object"))
        })

        val label =
          Inst.Label(fresh(), ty.args.map(arg => Val.Local(fresh(), arg)))
        val call =
          Inst.Let(fresh(), Op.Call(defn.ty, label.params.head, label.params, Next.None))

        val box = Type.box.get(defnType.ret) match {
          case Some(boxTy) =>
            Inst
              .Let(fresh(), Op.Box(boxTy, Val.Local(call.name, defnType.ret)))
          case None =>
            Inst
              .Let(fresh(), Op.As(ty.ret, Val.Local(call.name, defnType.ret)))
        }
        val retInst = ty.ret match {
          case Type.Unit => Inst.Ret(Val.Unit)
          case _         => Inst.Ret(Val.Local(box.name, ty.ret))
        }

        Defn.Define(
          Attrs.fromSeq(Seq(Attr.Dyn)),
          Global.Member(owner, id + "_proxy"),
          ty,
          Seq(
            label,
            call,
            box,
            retInst
          )
        )
      }

      def isReflProxy(global: Global): Boolean = false

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

      val proxies =
        structuralMethods.map(g =>
          genReflProxy(defns.find(_.name == g).get.asInstanceOf[Defn.Define]))


      val defnss = defns.map {
        case defn @ Defn.Define(attrs, name, ty, insts)
            if structuralMethods.contains(name) =>
          Defn.Define(Attrs.fromSeq(attrs.toSeq /* :+ Attr.Dyn*/ ),
                      name,
                      ty,
                      insts)
        case defn => defn
      } ++ proxies

      onComplete()

      (unresolved.toSeq, links.toSeq, defnss.sortBy(_.name.toString))
    }
  }
}
