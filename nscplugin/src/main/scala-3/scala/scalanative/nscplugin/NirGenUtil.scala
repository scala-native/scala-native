package scala.scalanative.nscplugin

import dotty.tools.dotc.ast.tpd
import tpd._
import dotty.tools.dotc.core
import core.Symbols._
import core.Contexts._
import core.Types._
import core.Flags._
import scalanative.util.unsupported
import scalanative.util.ScopedVar.scoped
import scalanative.nir.Fresh
import dotty.tools.dotc.core.Phases
trait NirGenUtil(using Context) { self: NirCodeGen =>

  private lazy val materializeClassTagTypes: Map[Symbol, Symbol] = Map(
    defnNir.ByteClassTag -> defn.ByteClass,
    defnNir.ShortClassTag -> defn.ShortClass,
    defnNir.CharClassTag -> defn.CharClass,
    defnNir.IntClassTag -> defn.IntClass,
    defnNir.LongClassTag -> defn.LongClass,
    defnNir.FloatClassTag -> defn.FloatClass,
    defnNir.DoubleClassTag -> defn.DoubleClass,
    defnNir.BooleanClassTag -> defn.BooleanClass,
    defnNir.UnitClassTag -> defn.UnitClass,
    defnNir.AnyClassTag -> defn.AnyClass,
    defnNir.ObjectClassTag -> defn.ObjectClass,
    defnNir.AnyValClassTag -> defn.ObjectClass,
    defnNir.AnyRefClassTag -> defn.ObjectClass,
    defnNir.NothingClassTag -> defn.NothingClass,
    defnNir.NullClassTag -> defn.NullClass
  )

  private lazy val materializePrimitiveTypeMethodTypes = Map(
    defnNir.UnsafeTag_materializeUnitTag -> defn.UnitClass,
    defnNir.UnsafeTag_materializeByteTag -> defn.ByteClass,
    defnNir.UnsafeTag_materializeBooleanTag -> defn.BooleanClass,
    defnNir.UnsafeTag_materializeCharTag -> defn.CharClass,
    defnNir.UnsafeTag_materializeShortTag -> defn.ShortClass,
    defnNir.UnsafeTag_materializeIntTag -> defn.IntClass,
    defnNir.UnsafeTag_materializeLongTag -> defn.LongClass,
    defnNir.UnsafeTag_materializeFloatTag -> defn.FloatClass,
    defnNir.UnsafeTag_materializeDoubleTag -> defn.DoubleClass,
    defnNir.UnsafeTag_materializeUByteTag -> defnNir.UByteClass,
    defnNir.UnsafeTag_materializeUShortTag -> defnNir.UShortClass,
    defnNir.UnsafeTag_materializeUIntTag -> defnNir.UIntClass,
    defnNir.UnsafeTag_materializeULongTag -> defnNir.ULongClass,
    defnNir.UnsafeTag_materializePtrTag -> defnNir.PtrClass
  )

  protected def desugarTree(tree: Tree): Tree = {
    tree match {
      case ident: Ident => tpd.desugarIdent(ident)
      case _            => tree
    }
  }

  protected def qualifierOf(fun: Tree): Tree = {
    fun match {
      case fun: Ident =>
        fun.tpe match {
          case TermRef(prefix: TermRef, _)  => tpd.ref(prefix)
          case TermRef(prefix: ThisType, _) => tpd.This(prefix.cls)
        }
      case Select(qualifier, _) => qualifier
      case TypeApply(fun, _)    => qualifierOf(fun)
    }
  }

  protected def unwrapClassTagOption(tree: Tree): Option[Symbol] =
    tree match {
      case Apply(ref: RefTree, args) =>
        val s = ref.symbol
        materializeClassTagTypes.get(s).orElse {
          if s == defnNir.ClassTagApply then
            val Literal(const) = args.head: @unchecked
            Some(const.typeValue.typeSymbol)
          else None
        }
      case _ => None
    }

  protected def unwrapTagOption(tree: Tree): Option[SimpleType] = {
    tree match {
      case Apply(ref: RefTree, args) =>
        val s = ref.symbol
        def allsts = {
          val sts = args.flatMap(unwrapTagOption(_).toSeq)
          if (sts.length == args.length) Some(sts) else None
        }
        def just(sym: Symbol) = Some(SimpleType(sym))
        def wrap(sym: Symbol) = allsts.map(SimpleType(sym, _))
        def optIndexOf(methods: Seq[Symbol], classes: Seq[Symbol]) =
          if (methods.contains(s)) Some(classes(methods.indexOf(s)))
          else None

        def resolveMaterializedTree = {
          if s == defnNir.UnsafeTag_materializeClassTag then
            just(unwrapClassTag(args.head))
          else if s == defnNir.UnsafeTag_materializeCArrayTag then
            wrap(defnNir.CArrayClass)
          else {
            def asCStruct = optIndexOf(
              defnNir.UnsafeTag_materializeCStructTags,
              defnNir.CStructClasses
            ).flatMap(wrap)

            def asNatBase = optIndexOf(
              defnNir.UnsafeTag_materializeNatBaseTags,
              defnNir.NatBaseClasses
            ).flatMap(just)

            def asNatDigit = optIndexOf(
              defnNir.UnsafeTag_materializeNatDigitTags,
              defnNir.NatDigitClasses
            ).flatMap(wrap)

            asCStruct.orElse(asNatBase).orElse(asNatDigit)
          }
        }

        def resolveGiven = Option.when(s.is(Given)) {
          atPhase(Phases.postTyperPhase) {
            val givenTpe = s.denot.info.argInfos.head
            // make sure to dealias the type here,
            // information about underlying opaque type would not be available after erasue
            fromType(givenTpe.widenDealias)
          }
        }

        materializePrimitiveTypeMethodTypes
          .get(s)
          .flatMap(just(_))
          .orElse(resolveMaterializedTree)
          .orElse(resolveGiven)

      case _ => None
    }
  }

  protected def unwrapTag(tree: Tree): SimpleType =
    unwrapTagOption(tree).getOrElse {
      unsupported(s"can't recover compile-time tag from $tree")
    }

  protected def unwrapClassTag(tree: Tree): Symbol =
    unwrapClassTagOption(tree).getOrElse {
      unsupported(s"can't recover compile-time tag from $tree")
    }

  protected def withFreshExprBuffer[R](f: ExprBuffer ?=> R): R = {
    scoped(
      curFresh := Fresh()
    ) {
      val buffer = new ExprBuffer(using curFresh)
      f(using buffer)
    }
  }
}

object NirGenUtil {
  class ContextCached[T](init: Context ?=> T) {
    private var lastContext: Context = _
    private var cached: T = _

    def get(using Context): T = {
      if (lastContext != ctx) {
        cached = init
        lastContext = ctx
      }
      cached
    }
  }
}
