package scala.scalanative.nscplugin.check

import scala.scalanative.nscplugin.NirPhase
import scala.scalanative.util
import scala.scalanative.util.ScopedVar
import scala.tools.nsc.{Phase, Global => NscGlobal}

abstract class PrepSanityCheck[G <: NscGlobal with Singleton](
    override val global: G)
    extends NirPhase[G](global)
    with NirCheckStat[G]
    with NirCheckExpr[G] {

  import global._
  import nirAddons._

  val phaseName: String = "sanitycheck"

  override def description: String = "sanity check ASTs for NIR"

  override def newPhase(prev: Phase): StdPhase =
    new SanityCheckPhase(prev)

  protected val curClassSym  = new util.ScopedVar[Symbol]
  protected val curMethodSym = new util.ScopedVar[Symbol]
  protected val curValSym    = new util.ScopedVar[Symbol]

  protected val Ok: Unit = ()

  class SanityCheckPhase(prev: Phase) extends StdPhase(prev) {

    override def run(): Unit = {
      nirPrimitives.initPrepNativePrimitives()
      super.run()
    }

    override def apply(cunit: CompilationUnit): Unit = {
      def loop(tree: Tree): Unit = tree match {
        case EmptyTree            => Ok
        case _: Import            => Ok
        case PackageDef(_, stats) => stats.foreach(loop)
        case id: ImplDef          => checkClassOrModule(id)
      }

      loop(cunit.body)
    }

  }

  protected def symToKindPlural(sym: Symbol): String =
    if (sym.isClass)
      if (sym.asClass.isTrait) "traits" else "classes"
    else "objects"

  protected def symToKind(sym: Symbol): String =
    if (sym.isClass)
      if (sym.asClass.isTrait) "trait" else "class"
    else "object"

  protected def freeLocalVars(tree: Tree): List[Symbol] = {
    new FreeVarTraverser().apply(tree)
  }

  // Capture free variables and methods
  // Inspired by LambdaLift phase in scalac
  private class FreeVarTraverser extends Traverser {

    private val localVars = new ScopedVar[List[Symbol]]
    private val freeVars  = List.newBuilder[Symbol]

    def apply(tree: Tree): List[Symbol] = {
      freeVars.clear()
      ScopedVar.scoped(localVars := Nil) {
        traverse(tree)
      }
      freeVars.result()
    }

    override def traverse(tree: Tree): Unit = {
      val sym = tree.symbol
      tree match {
        case ClassDef(_, _, _, _) =>
          super.traverse(tree)

        case DefDef(_, _, _, vparams, _, _) =>
          withLocalSyms(vparams.flatten.map(_.symbol)) {
            super.traverse(tree)
          }

        case Block(stats, last) =>
          (stats ++ (last :: Nil)).foldLeft(List.empty[Symbol]) {
            case (syms, stat) =>
              withLocalSyms(syms) {
                traverse(stat)
              }
              stat match {
                case _: ValDef | _: DefDef | _: ClassDef | _: ModuleDef =>
                  stat.symbol :: syms
                case _ => syms
              }
          }

        case Ident(name) =>
          if (sym == NoSymbol) {
            assert(name == nme.WILDCARD, s"expected wildcard, but got $name")
          } else if (sym.isLocalToBlock && !isLocal(sym)) {
            if (sym.isMethod) {
              freeVars += sym
            } else if (sym.isTerm) {
              freeVars += sym
            }
          }
          super.traverse(tree)

        case Function(vparams, _) =>
          withLocalSyms(vparams.map(_.symbol)) {
            super.traverse(tree)
          }

        case Select(This(_), _) if sym.isModule =>
          super.traverse(tree)

        case Select(receiver @ This(_), _) if !isLocal(receiver.symbol) =>
          freeVars += sym

        case Select(_, _) =>
          if (sym.isConstructor &&
              sym.owner.isLocalToBlock &&
              !isLocal(sym.owner)) {
            freeVars += sym.owner
          }
          super.traverse(tree)

        case _ => super.traverse(tree)
      }
    }

    private def isLocal(sym: Symbol) = localVars.get.contains(sym)

    private def withLocalSyms[T](syms: List[Symbol])(op: => T): T = {
      ScopedVar.scoped(
        localVars := localVars ++ syms
      )(op)
    }
  }

}
