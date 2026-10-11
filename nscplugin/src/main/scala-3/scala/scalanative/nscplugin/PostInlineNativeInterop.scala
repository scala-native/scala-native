package scala.scalanative.nscplugin

import dotty.tools._
import dotty.tools.dotc._
import dotty.tools.dotc.ast.tpd._
import dotty.tools.dotc.plugins.PluginPhase
import dotty.tools.dotc.transform.SeqLiterals

import scala.scalanative.nscplugin.CompilerCompat.SymUtils.{
  isScalaStatic, isVolatile, setter
}

import NirGenUtil.ContextCached

/** This phase does:
 *    - handle TypeApply -> Apply conversion for intrinsic methods
 */
object PostInlineNativeInterop {
  val name = "scalanative-prepareInterop-postinline"
  private val LayoutEvidence =
    new dotc.util.Property.Key[Map[dotc.core.Types.Type, dotc.ast.tpd.Tree]]
}

class PostInlineNativeInterop extends PluginPhase with VarHandleInterop {

  import core.Constants._
  import core.Contexts._
  import core.Definitions
  import core.Flags._
  import core.NameOps._
  import core.Names._
  import core.StdNames._
  import core.Symbols._
  import core.Types._

  override val runsAfter = Set(transform.Inlining.name, PrepNativeInterop.name)
  override val runsBefore = Set(transform.FirstTransform.name)
  override def allowsImplicitSearch: Boolean = true
  val phaseName = PostInlineNativeInterop.name
  override def description: String = "prepare ASTs for Native interop"

  override def prepareForApply(tree: Apply)(using Context): Context =
    tree match {
      case Apply(TypeApply(fun, List(layoutType, _)), List(tag, _))
          if fun.symbol == defnNir.IntrinsicsInternal_withLayoutTag =>
        val evidence = ctx
          .property(PostInlineNativeInterop.LayoutEvidence)
          .getOrElse(Map.empty)
        ctx.fresh.setProperty(
          PostInlineNativeInterop.LayoutEvidence,
          evidence.updated(layoutType.tpe, tag)
        )
      case _ => ctx
    }

  override def prepareForBlock(tree: Block)(using Context): Context = {
    tree.stats.foreach(discardVarHandleResult)
    layoutScope(tree.stats)
  }

  override def prepareForDefDef(tree: DefDef)(using Context): Context =
    layoutScope(tree.paramss.flatten)

  override def prepareForInlined(tree: Inlined)(using Context): Context =
    layoutScope(tree.bindings)

  private def layoutScope(trees: List[Tree])(using Context): Context = {
    // Post-typer traversal retains owners and imports, but not local implicit scopes.
    val scope = core.Scopes.newScope
    trees.foreach {
      case tree: DefTree if tree.symbol.exists => scope.enter(tree.symbol)
      case _                                   => ()
    }
    ctx.fresh.setScope(scope)
  }

  private def tagLayout(tpe: Type, member: String, tree: Tree)(using
      Context
  ): Tree = {
    val tagClass = requiredClass("scala.scalanative.unsafe.Tag")
    val expected = tagClass.typeRef.appliedTo(tpe)
    // Mini-phase transform hooks execute in the next phase's context.
    val searchCtx = ctx.withPhase(this)
    val evidence = ctx
      .property(PostInlineNativeInterop.LayoutEvidence)
      .flatMap(_.get(tpe))
      .getOrElse(
        searchCtx.typer.inferImplicitArg(expected, tree.span)(using searchCtx)
      )
    if (evidence.tpe.isError) {
      report.error(
        s"Cannot determine native layout of ${tpe.show}: an implicit ${expected.show} is required",
        tree.srcPos
      )
      tree
    } else {
      // This phase runs after inlining; newly resolved inline givens still need expansion.
      val inliner = new TreeMap {
        override def transform(tree: Tree)(using Context): Tree = {
          val transformed = super.transform(tree)
          if (CompilerCompat.Inlines.needsInlining(transformed))
            CompilerCompat.Inlines.inlineCall(transformed)
          else transformed
        }
      }
      val expandedEvidence = transformAllDeep(
        inliner.transform(evidence)(using ctx.withPhase(ctx.base.inliningPhase))
      )
      ref(defnNir.Intrinsics_castIntToRawSizeUnsigned)
        .appliedTo(expandedEvidence.select(termName(member)))
        .withSpan(tree.span)
    }
  }

  override def transformApply(tree: Apply)(using Context): Tree = {
    // Inline definitions are also visited when compiling their defining library.
    // Resolve their layout only after expansion at a call site.
    if (CompilerCompat.Inlines.inInlineMethod) return tree
    val defnNir = this.defnNir
    if (tree.fun.symbol == defnNir.IntrinsicsInternal_withLayoutTag)
      return tree.args(1)
    def dealiasTypeMapper = DealiasTypeMapper()

    // Attach exact type information to the AST to preserve the type information
    // during the type erase phase and refer to it in the NIR generation phase.
    tree match
      case app @ Apply(fun, args)
          if defnNir.VarHandleAvailable && fun.symbol == defnNir.MethodHandlesLookup_findVarHandle =>
        rewriteVarHandleLookup(app, args, isStatic = false)

      case app @ Apply(fun, args)
          if defnNir.VarHandleAvailable && fun.symbol == defnNir.MethodHandlesLookup_findStaticVarHandle =>
        rewriteVarHandleLookup(app, args, isStatic = true)

      case app @ Apply(fun, args)
          if defnNir.VarHandleAvailable && fun.symbol.exists && fun.symbol.owner == defnNir.VarHandleClass =>
        rewriteVarHandleAccess(
          app,
          fun.symbol,
          args
        )

      case app @ Apply(fun, args)
          if fun.symbol == defnNir.AtomicIntegerFieldUpdater_newUpdater =>
        rewriteAtomicFieldUpdater(
          app = app,
          args = args,
          factory = defnNir.AtomicFieldUpdater_createIntegerFieldUpdater,
          expectedPrimitiveType = Some(defn.IntType)
        )

      case app @ Apply(fun, args)
          if fun.symbol == defnNir.AtomicLongFieldUpdater_newUpdater =>
        rewriteAtomicFieldUpdater(
          app = app,
          args = args,
          factory = defnNir.AtomicFieldUpdater_createLongFieldUpdater,
          expectedPrimitiveType = Some(defn.LongType)
        )

      case app @ Apply(fun, args)
          if fun.symbol == defnNir.AtomicReferenceFieldUpdater_newUpdater =>
        rewriteAtomicFieldUpdater(
          app = app,
          args = args,
          factory = defnNir.AtomicFieldUpdater_createReferenceFieldUpdater,
          expectedPrimitiveType = None
        )

      case app @ Apply(TypeApply(fun, tArgs), List(lambda))
          if defnNir.CFuncPtr_fromScalaFunction.contains(fun.symbol) =>
        val tys = tArgs.map(t => dealiasTypeMapper(t.tpe))
        lambda
          .foreachSubTree {
            case tree @ Select(This(_), _)
                if !tree.symbol.owner.isStaticOwner =>
              report.error(
                s"CFuncPtr lambda can only refer to statically reachable symbols, but it's using ${tree.symbol.showLocated}",
                tree.srcPos
              )
            case _ => ()
          }

        app.withAttachment(NirDefinitions.NonErasedTypes, tys)

      case Apply(fun, args) if defnNir.CFuncPtr_apply.contains(fun.symbol) =>
        val paramTypes =
          args.map(a => dealiasTypeMapper(a.tpe)) :+
            dealiasTypeMapper(tree.tpe.finalResultType)
        fun match {
          case Select(Inlined(_, _, ext), _) =>
            // Apply(Select(Inlined(_,_,_),_),_) would not preserve the attachment, use the receiver as a carrier
            fun.putAttachment(NirDefinitions.NonErasedTypes, paramTypes)
            tree
          case _ => ()
        }
        tree.withAttachment(NirDefinitions.NonErasedTypes, paramTypes)

      case Apply(fun, args)
          if defnNir.Intrinsics_stackallocAlts.contains(fun.symbol) =>
        val originalType = fun.asInstanceOf[TypeApply].args.head.tpe
        val layoutType = dealiasTypeMapper(originalType)
        if (layoutType.isAny || layoutType.isNothingType || layoutType.isNullType) {
          report.error(
            s"Stackalloc requires concrete type but ${layoutType.show} found",
            tree.srcPos
          )
          return tree
        }
        if (!hasLayout(layoutType)) {
          val elemSize = tagLayout(originalType, "size", tree)
          if (elemSize eq tree) return tree
          val count = args.headOption.getOrElse(
            ref(defnNir.Intrinsics_castIntToRawSizeUnsigned).appliedTo(
              Literal(Constant(1))
            )
          )
          // Equivalent to the:
          // q"""{
          //   val nativeElements = $count
          //   val nativeByteCount = Intrinsics.castLongToRawSize(
          //     Intrinsics.castRawSizeToLongUnsigned($elemSize) *
          //       Intrinsics.castRawSizeToLongUnsigned(nativeElements)
          //   )
          //   Intrinsics.stackalloc[Byte](nativeByteCount)
          // }"""
          // format: off
          val toLong = defnNir.IntrinsicsModule.requiredMethod("castRawSizeToLongUnsigned")
          val elements = newSymbol(ctx.owner, termName("nativeElements"), Synthetic, count.tpe.widen).asTerm
          val bytes = ref(toLong).appliedTo(elemSize).select(defn.Long_*).appliedTo(ref(toLong).appliedTo(ref(elements)))
          val rawBytes = ref(defnNir.IntrinsicsModule.requiredMethod("castLongToRawSize")).appliedTo(bytes)
          val byteCount = newSymbol(ctx.owner, termName("nativeByteCount"), Synthetic, rawBytes.tpe).asTerm
          val allocation = ref(defnNir.Intrinsics_stackallocAlts.find(_.info.paramInfoss.flatten.nonEmpty).get)
            .appliedToType(defn.ByteType).appliedTo(ref(byteCount))
            .withAttachment(NirDefinitions.NonErasedType, defn.ByteType).withSpan(tree.span)
          // Bind the byte count to keep getUnboxedSize from stripping conversions inside the product.
          return Block(List(ValDef(elements, count), ValDef(byteCount, rawBytes)), allocation).withSpan(tree.span)
          // format: on
        }
        val tpe = fun match {
          case TypeApply(_, Seq(argTpe)) => dealiasTypeMapper(argTpe.tpe)
        }
        tree.withAttachment(NirDefinitions.NonErasedType, tpe)

      case Apply(fun, args)
          if fun.symbol.isExtern && fun.symbol.usesVariadicArgs =>
        args
          .collectFirst {
            case SeqLiteral(args, _)           => args
            case Typed(SeqLiteral(args, _), _) => args
          }
          .toList
          .flatten
          .foreach { varArg =>
            varArg.pushAttachment(
              NirDefinitions.NonErasedType,
              varArg.typeOpt.widenDealias
            )
          }
        tree

      case _ => tree

  }

  /* Rewrites generic java.util.concurrent.atomic.Atomic*FieldUpdater.newUpdater to call to use intrinsic implementaiton:
   * scala.scalanative.runtime.AtomicFieldUpdater.createIntegerFieldUpdater[T]((atomicFieldTarget: T) =>
   *   fromRawPtr[Int]:
   *    Intrinsics.classFieldRawPtr[Test](atomicFieldTarget, "x")
   */
  private def rewriteAtomicFieldUpdater(
      app: Apply,
      args: List[Tree],
      factory: Symbol,
      expectedPrimitiveType: Option[Type]
  )(using Context): Tree = {
    def fail(message: String, tree: Tree = app): Tree = {
      report.error(message, tree.srcPos)
      app
    }
    val getClass = defn.ObjectClass.requiredMethod(nme.getClass_)
    def classLiteral(tree: Tree): Option[Type] = tree match {
      // classOf[T]
      case Literal(c) if c.tag == ClazzTag => Some(c.typeValue)
      // classOf[T]
      case TypeApply(fun, List(tpe)) if fun.symbol == defn.Predef_classOf =>
        Some(tpe.tpe.widenDealias)
      // receiver.getClass
      case Apply(TypeApply(Select(receiver, _), _), Nil)
          if tree.symbol == getClass =>
        Some(receiver.tpe.widenDealias)
      // receiver.getClass (with the compiler-inserted Class[_] cast)
      case TypeApply(Select(getClassCall @ Apply(_, Nil), _), _) =>
        classLiteral(getClassCall)
      // (classOf[T]: Class[_]) or (receiver.getClass: Class[_])
      case Typed(inner, _) => classLiteral(inner)
      // Any other expression is not statically resolvable.
      case _ => None
    }

    def matchesName(symbol: Symbol, name: String) =
      symbol.name.mangledString == name || symbol.name.toString == name ||
        symbol.name.unexpandedName.toString == name

    val fieldName = args.lastOption.flatMap {
      case Literal(c) if c.tag == StringTag => Some(c.stringValue)
      case _                                => None
    }.match {
      case Some(name) => name
      case None       =>
        return fail(
          "Atomic field updater field name must be a literal string",
          args.last
        )
    }
    val targetType = args.headOption
      .flatMap(classLiteral)
      .match {
        case Some(tpe) => tpe
        case None      =>
          return fail(
            "Atomic field updater class must be a literal classOf[T] expression or getClass on a statically typed value",
            args.head
          )
      }
    val members = targetType.baseClasses.flatMap(_.asClass.info.decls.toList) ++
      targetType.typeSymbol.companionModule.info.decls.toList
    val field = targetType.baseClasses
      .flatMap(base => targetType.baseType(base).fields)
      .find(field => matchesName(field.symbol, fieldName))
      .orElse(members.collectFirst {
        case field if !field.is(Method) && matchesName(field, fieldName) =>
          field.asSymDenotation
      })
      .orElse(members.collectFirst {
        case accessor
            if accessor.is(Accessor) && matchesName(accessor, fieldName) =>
          accessor.asSymDenotation.underlyingSymbol.asSymDenotation
      })
      .match {
        case Some(field) => field
        case None        =>
          return fail(
            s"${targetType.typeSymbol.show} does not contain field $fieldName"
          )
      }

    val fieldSym = field.symbol
    if fieldSym.is(JavaStatic) || fieldSym.isScalaStatic then
      return fail(s"Atomic field updater cannot target static field $fieldName")
    if !fieldSym.is(Mutable) || !fieldSym.isVolatile then
      return fail(
        s"Atomic field updater requires a volatile mutable field $fieldName"
      )
    val expectedType = expectedPrimitiveType
      .orElse(args.lift(1).flatMap(classLiteral))
      .match {
        case Some(tpe) => tpe
        case None      =>
          return fail(
            "Atomic reference field updater value class must be a literal classOf[T] expression",
            args(1)
          )
      }
    val fieldType = field.info.resultType.widenDealias
    if !(core.TypeErasure
          .erasure(fieldType) =:= core.TypeErasure.erasure(expectedType)) then
      return fail(
        s"Atomic field updater type does not match field $fieldName, expected $expectedType but got $fieldType"
      )

    val accessors = members.filter(member =>
      member.is(Accessor) && matchesName(member, fieldName)
    )
    def accessible(symbol: Symbol): Boolean =
      !symbol.is(Private) || symbol.owner == ctx.owner.enclosingClass
    if !accessible(fieldSym) && !accessors.exists(accessible) then
      return fail(
        s"Atomic field updater cannot access field $fieldName: it is private to ${fieldSym.owner.show} but used from ${ctx.owner.enclosingClass.show}"
      )

    val lambdaType = MethodType(List(termName("atomicFieldTarget")))(
      _ => List(defn.ObjectType),
      _ => defn.ObjectType
    )
    val lambda = Lambda(
      lambdaType,
      params =>
        Apply(
          TypeApply(
            ref(defnNir.RuntimePackage_fromRawPtr),
            List(TypeTree(expectedType))
          ),
          List(
            Apply(
              TypeApply(
                ref(defnNir.Intrinsics_classFieldRawPtr),
                List(TypeTree(targetType))
              ),
              List(
                TypeApply(
                  Select(params.head, nme.asInstanceOf_),
                  List(TypeTree(targetType))
                ),
                Literal(Constant(fieldName))
              )
            )
          )
        )
    )
    val factoryTypeArgs =
      if expectedPrimitiveType.isDefined then List(targetType)
      else List(targetType, expectedType)
    Apply(
      TypeApply(ref(factory), factoryTypeArgs.map(TypeTree(_))),
      List(lambda)
    )
  }

  override def transformTypeApply(tree: TypeApply)(using Context): Tree = {
    if (CompilerCompat.Inlines.inInlineMethod) return tree
    val TypeApply(fun, tArgs) = tree
    val defnNir = this.defnNir
    def dealiasTypeMapper = DealiasTypeMapper()

    // sizeOf[T] -> sizeOf(classOf[T])
    fun.symbol match
      case defnNir.Intrinsics_sizeOf =>
        val tpe = dealiasTypeMapper(tArgs.head.tpe)
        if (!hasLayout(tpe)) return tagLayout(tArgs.head.tpe, "size", tree)
        cpy
          .Apply(tree)(
            ref(defnNir.IntrinsicsInternal_sizeOf),
            List(Literal(Constant(tpe)))
          )
          .withAttachment(NirDefinitions.NonErasedType, tpe)

      // alignmentOf[T] -> alignmentOf(classOf[T])
      case defnNir.Intrinsics_alignmentOf =>
        val tpe = dealiasTypeMapper(tArgs.head.tpe)
        if (!hasLayout(tpe)) return tagLayout(tArgs.head.tpe, "alignment", tree)
        cpy
          .Apply(tree)(
            ref(defnNir.IntrinsicsInternal_alignmentOf),
            List(Literal(Constant(tpe)))
          )
          .withAttachment(NirDefinitions.NonErasedType, tpe)

      case _ => tree
  }

}
