package scala.scalanative.nscplugin.check

import scala.scalanative.nscplugin.NirPhase
import scala.scalanative.util
import scala.tools.nsc.{Phase, Global => NscGlobal}

abstract class PreNirSanityCheck[G <: NscGlobal with Singleton](
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
    val trav = new FreeVarTraverser
    trav.traverse(tree)
    trav.allFreeVars
  }

  // Capture free variables and methods
  // Inspired by LambdaLift phase in scalac
  private class FreeVarTraverser extends Traverser {

    private var localVars: List[Symbol] = Nil
    private var freeVars: List[Symbol]  = Nil

    def allFreeVars: List[Symbol] = freeVars

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
          // special case for defs
          (stats ++ (last :: Nil)).foldLeft(Nil: List[Symbol])({
            case (syms, stat: ValDef) =>
              withLocalSyms(syms) {
                traverse(stat)
              }
              stat.symbol :: syms
            case (syms, stat: DefDef) =>
              withLocalSyms(syms) {
                traverse(stat)
              }
              stat.symbol :: syms
            case (syms, stat: ModuleDef) =>
              withLocalSyms(syms) {
                traverse(stat)
              }
              stat.symbol :: syms
            case (syms, stat: ClassDef) =>
              withLocalSyms(syms) {
                traverse(stat)
              }
              stat.symbol :: syms
            case (syms, stat) =>
              withLocalSyms(syms) {
                traverse(stat)
              }
              syms
          })
        case Ident(name) =>
          if (sym == NoSymbol) {
            assert(name == nme.WILDCARD, s"expected wildcard, but got $name")
          } else if (sym.isLocalToBlock && !localVars.contains(sym)) {
            if (sym.isTerm && !sym.isMethod) {
              freeVars = sym :: freeVars
            } else if (sym.isMethod) {
              freeVars = sym :: freeVars
            }
          }
          super.traverse(tree)
        case Function(vparams, body) =>
          withLocalSyms(vparams.map(_.symbol)) {
            super.traverse(tree)
          }
        case Select(This(_), _) if sym.isModule =>
          super.traverse(tree)
        case Select(receiver @ This(_), _)
            if !localVars.contains(receiver.symbol) =>
          freeVars = sym :: freeVars
        case Select(_, _) =>
          if (sym.isConstructor &&
              sym.owner.isLocalToBlock &&
              !localVars.contains(sym.owner)) {
            freeVars = sym.owner :: freeVars
          }
          super.traverse(tree)
        case _ =>
          super.traverse(tree)
      }
    }

    private def logicallyEnclosingMember(sym: Symbol): Symbol = {
      if (sym.isLocalDummy) {
        val enclClass = sym.enclClass
        //if (enclClass.isSubClass(DelayedInitClass))
        //  delayedInitDummies.getOrElseUpdate(enclClass, enclClass.newMethod(nme.delayedInit))
        //else
        enclClass.primaryConstructor
      } else if (sym.isMethod || sym.isClass || sym == NoSymbol) sym
      else logicallyEnclosingMember(sym.owner)
    }

    private def withLocalSyms[T](syms: List[Symbol])(op: => T): T = {
      val localVars0 = localVars
      localVars = localVars ++ syms
      val res = op
      localVars = localVars0
      res
    }
  }

}
