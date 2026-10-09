package scala.scalanative.nscplugin

import dotty.tools.*
import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.*
import dotty.tools.dotc.core.Constants.*
import dotty.tools.dotc.core.Contexts.{Context, ctx}
import dotty.tools.dotc.core.Flags.*
import dotty.tools.dotc.core.NameOps.*
import dotty.tools.dotc.core.Names.*
import dotty.tools.dotc.core.StdNames.nme
import dotty.tools.dotc.plugins.PluginPhase
import dotty.tools.dotc.report
import dotty.tools.dotc.util.Property.StickyKey

import scala.scalanative.nscplugin.CompilerCompat.SymUtils.isScalaStatic

// scalafmt: { maxColumn = 160 }
private[nscplugin] object VarHandleInterop {
  import Symbols.*
  import Types.*

  // TASTy retains the proven Scala var symbol, but not necessarily its backing
  // field in Type.fields after erasure. Keep lookup's symbol for code generation.
  object ResolvedField extends StickyKey[Symbol]

  final case class AccessMode(
      operation: Symbol,
      memoryOrder: Symbol,
      bitwiseOperation: Option[Symbol] = None
  )

  final case class PrimitiveConversions(wideningMethod: TermName, unboxingMethod: TermName)

  enum VariableKind(val memberName: String) {
    case Boolean extends VariableKind("Boolean")
    case Byte extends VariableKind("Byte")
    case Short extends VariableKind("Short")
    case Char extends VariableKind("Char")
    case Int extends VariableKind("Int")
    case Long extends VariableKind("Long")
    case Float extends VariableKind("Float")
    case Double extends VariableKind("Double")
    case Reference extends VariableKind("Reference")
  }

  /** Lazily resolved symbols owned by one interop phase instance. */
  class VarHandleMetadata(using Context) {
    private val d = NirDefinitions.get

    lazy val fieldBindingClass: ClassSymbol = requiredClass("scala.scalanative.runtime.NativeVarHandle.FieldBinding")

    lazy val primitiveKinds: Map[Symbol, VariableKind] = Map(
      defn.BooleanClass -> VariableKind.Boolean,
      defn.ByteClass -> VariableKind.Byte,
      defn.ShortClass -> VariableKind.Short,
      defn.CharClass -> VariableKind.Char,
      defn.IntClass -> VariableKind.Int,
      defn.LongClass -> VariableKind.Long,
      defn.FloatClass -> VariableKind.Float,
      defn.DoubleClass -> VariableKind.Double
    )

    lazy val classTokens = List(
      defn.BoxedBooleanClass -> defn.BooleanType,
      defn.BoxedByteClass -> defn.ByteType,
      defn.BoxedShortClass -> defn.ShortType,
      defn.BoxedCharClass -> defn.CharType,
      defn.BoxedIntClass -> defn.IntType,
      defn.BoxedLongClass -> defn.LongType,
      defn.BoxedFloatClass -> defn.FloatType,
      defn.BoxedDoubleClass -> defn.DoubleType
    ).flatMap { (wrapper, primitive) =>
      List(wrapper.info.member(termName("TYPE")).symbol, wrapper.companionModule.info.member(termName("TYPE")).symbol)
        .filter(_.exists)
        .map(_ -> primitive)
    }.toMap

    lazy val wideningTargets: Map[Symbol, List[Symbol]] = {
      val numeric =
        List(defn.ByteClass, defn.ShortClass, defn.IntClass, defn.LongClass, defn.FloatClass, defn.DoubleClass)
      numeric.zipWithIndex.map { (symbol, index) => symbol -> numeric.drop(index + 1) }.toMap +
        (defn.CharClass -> numeric.drop(2))
    }

    lazy val conversionNames: Map[Symbol, PrimitiveConversions] = Map(
      defn.BooleanClass -> PrimitiveConversions(termName("toBoolean"), termName("booleanValue")),
      defn.ByteClass -> PrimitiveConversions(termName("toByte"), termName("byteValue")),
      defn.ShortClass -> PrimitiveConversions(termName("toShort"), termName("shortValue")),
      defn.CharClass -> PrimitiveConversions(termName("toChar"), termName("charValue")),
      defn.IntClass -> PrimitiveConversions(termName("toInt"), termName("intValue")),
      defn.LongClass -> PrimitiveConversions(termName("toLong"), termName("longValue")),
      defn.FloatClass -> PrimitiveConversions(termName("toFloat"), termName("floatValue")),
      defn.DoubleClass -> PrimitiveConversions(termName("toDouble"), termName("doubleValue"))
    )

    lazy val boxingMethods = defn.BoxesRunTimeModule.info.decls.toList
      .filter { method =>
        method.is(Method) && (method.info.paramInfoss.flatten match {
          case List(input) =>
            primitiveKinds.contains(input.typeSymbol) &&
              method.info.finalResultType <:< defn.ObjectType
          case _ => false
        })
      }
      .map(method => method.info.paramInfoss.head.head.typeSymbol -> method)
      .toMap

    // Signature-polymorphic calls have synthetic symbols. The phase proves
    // their owner is VarHandle before dispatching by the declared member name.
    lazy val accessModes: Map[Name, AccessMode] = List(
      ("get", AccessMode(d.NativeVarHandle_get, d.VarHandleMemoryOrderPlain)),
      ("getOpaque", AccessMode(d.NativeVarHandle_get, d.VarHandleMemoryOrderPlain)),
      ("getVolatile", AccessMode(d.NativeVarHandle_get, d.VarHandleMemoryOrderVolatile)),
      ("getAcquire", AccessMode(d.NativeVarHandle_get, d.VarHandleMemoryOrderAcquire)),
      ("set", AccessMode(d.NativeVarHandle_set, d.VarHandleMemoryOrderPlain)),
      ("setOpaque", AccessMode(d.NativeVarHandle_set, d.VarHandleMemoryOrderPlain)),
      ("setVolatile", AccessMode(d.NativeVarHandle_set, d.VarHandleMemoryOrderVolatile)),
      ("setRelease", AccessMode(d.NativeVarHandle_set, d.VarHandleMemoryOrderRelease)),
      ("compareAndSet", AccessMode(d.NativeVarHandle_compare, d.VarHandleMemoryOrderVolatile)),
      ("weakCompareAndSet", AccessMode(d.NativeVarHandle_weakCompare, d.VarHandleMemoryOrderVolatile)),
      ("weakCompareAndSetPlain", AccessMode(d.NativeVarHandle_weakCompare, d.VarHandleMemoryOrderPlain)),
      ("weakCompareAndSetAcquire", AccessMode(d.NativeVarHandle_weakCompare, d.VarHandleMemoryOrderAcquire)),
      ("weakCompareAndSetRelease", AccessMode(d.NativeVarHandle_weakCompare, d.VarHandleMemoryOrderRelease)),
      ("compareAndExchange", AccessMode(d.NativeVarHandle_compareExchange, d.VarHandleMemoryOrderVolatile)),
      ("compareAndExchangeAcquire", AccessMode(d.NativeVarHandle_compareExchange, d.VarHandleMemoryOrderAcquire)),
      ("compareAndExchangeRelease", AccessMode(d.NativeVarHandle_compareExchange, d.VarHandleMemoryOrderRelease)),
      ("getAndSet", AccessMode(d.NativeVarHandle_exchange, d.VarHandleMemoryOrderVolatile)),
      ("getAndSetAcquire", AccessMode(d.NativeVarHandle_exchange, d.VarHandleMemoryOrderAcquire)),
      ("getAndSetRelease", AccessMode(d.NativeVarHandle_exchange, d.VarHandleMemoryOrderRelease)),
      ("getAndAdd", AccessMode(d.NativeVarHandle_add, d.VarHandleMemoryOrderVolatile)),
      ("getAndAddAcquire", AccessMode(d.NativeVarHandle_add, d.VarHandleMemoryOrderAcquire)),
      ("getAndAddRelease", AccessMode(d.NativeVarHandle_add, d.VarHandleMemoryOrderRelease)),
      (
        "getAndBitwiseOr",
        AccessMode(d.NativeVarHandle_bitwise, d.VarHandleMemoryOrderVolatile, Some(d.VarHandleBitwiseOperationOr))
      ),
      (
        "getAndBitwiseOrAcquire",
        AccessMode(d.NativeVarHandle_bitwise, d.VarHandleMemoryOrderAcquire, Some(d.VarHandleBitwiseOperationOr))
      ),
      (
        "getAndBitwiseOrRelease",
        AccessMode(d.NativeVarHandle_bitwise, d.VarHandleMemoryOrderRelease, Some(d.VarHandleBitwiseOperationOr))
      ),
      (
        "getAndBitwiseAnd",
        AccessMode(d.NativeVarHandle_bitwise, d.VarHandleMemoryOrderVolatile, Some(d.VarHandleBitwiseOperationAnd))
      ),
      (
        "getAndBitwiseAndAcquire",
        AccessMode(d.NativeVarHandle_bitwise, d.VarHandleMemoryOrderAcquire, Some(d.VarHandleBitwiseOperationAnd))
      ),
      (
        "getAndBitwiseAndRelease",
        AccessMode(d.NativeVarHandle_bitwise, d.VarHandleMemoryOrderRelease, Some(d.VarHandleBitwiseOperationAnd))
      ),
      (
        "getAndBitwiseXor",
        AccessMode(d.NativeVarHandle_bitwise, d.VarHandleMemoryOrderVolatile, Some(d.VarHandleBitwiseOperationXor))
      ),
      (
        "getAndBitwiseXorAcquire",
        AccessMode(d.NativeVarHandle_bitwise, d.VarHandleMemoryOrderAcquire, Some(d.VarHandleBitwiseOperationXor))
      ),
      (
        "getAndBitwiseXorRelease",
        AccessMode(d.NativeVarHandle_bitwise, d.VarHandleMemoryOrderRelease, Some(d.VarHandleBitwiseOperationXor))
      )
    ).map { (name, mode) => d.VarHandleClass.requiredMethod(name).name -> mode }.toMap

    lazy val operations: Map[Symbol, String] = List(
      d.NativeVarHandle_get -> "get",
      d.NativeVarHandle_set -> "set",
      d.NativeVarHandle_compare -> "compare",
      d.NativeVarHandle_weakCompare -> "weakCompare",
      d.NativeVarHandle_compareExchange -> "compareExchange",
      d.NativeVarHandle_exchange -> "exchange",
      d.NativeVarHandle_add -> "add",
      d.NativeVarHandle_bitwise -> "bitwise"
    ).toMap

    lazy val typedMethods = (for {
      (operation, prefix) <- operations.toList
      kind <- primitiveKinds.values.toList :+ VariableKind.Reference
    } yield (operation, kind) -> d.NativeVarHandleClass.requiredMethod(prefix + kind.memberName)).toMap
  }

}

/** Compiler lowering for field VarHandle operations. */
private[nscplugin] trait VarHandleInterop extends NativeInteropUtil {
  self: PluginPhase =>

  import Symbols.*
  import Types.*
  import VarHandleInterop.*

  private var phaseMetadata: Option[VarHandleMetadata] = None

  private def varHandleMetadata(using Context): VarHandleMetadata =
    phaseMetadata.getOrElse {
      val metadata = new VarHandleMetadata
      phaseMetadata = Some(metadata)
      metadata
    }

  private def variableKind(tpe: Type)(using Context)(using metadata: VarHandleMetadata): VariableKind =
    metadata.primitiveKinds.getOrElse(tpe.widenDealias.typeSymbol, VariableKind.Reference)

  private def boxingMethod(tpe: Type)(using Context)(using metadata: VarHandleMetadata): Symbol =
    metadata.boxingMethods(tpe.widenDealias.typeSymbol)

  private def wideningMethod(source: Type, target: Type)(using
      Context
  )(using metadata: VarHandleMetadata): Option[Symbol] = {
    val sourceSymbol = source.widenDealias.typeSymbol
    val targetSymbol = target.widenDealias.typeSymbol
    if metadata.wideningTargets.getOrElse(sourceSymbol, Nil).contains(targetSymbol) then
      Some(sourceSymbol.requiredMethod(metadata.conversionNames(targetSymbol).wideningMethod))
    else None
  }

  private object DiscardedResult extends dotty.tools.dotc.util.Property.StickyKey[Unit]

  /** Mark only result-producing paths, never coordinates, operands or bindings.
   */
  protected def discardVarHandleResult(tree: Tree)(using Context): Unit =
    tree match {
      case app: Apply if defnNir.VarHandleAvailable && app.fun.symbol.exists && app.fun.symbol.owner == defnNir.VarHandleClass =>
        app.putAttachment(DiscardedResult, ())
      case Block(_, expr)      => discardVarHandleResult(expr)
      case If(_, thenp, elsep) =>
        discardVarHandleResult(thenp)
        discardVarHandleResult(elsep)
      case Match(_, cases)     => cases.foreach(c => discardVarHandleResult(c.body))
      case Try(expr, cases, _) =>
        discardVarHandleResult(expr)
        cases.foreach(c => discardVarHandleResult(c.body))
      case Inlined(_, _, expansion)                      => discardVarHandleResult(expansion)
      case Typed(expr, tpt) if tpt.tpe =:= defn.UnitType =>
        discardVarHandleResult(expr)
      case _ => ()
    }

  /** Read a final constant from the shared runtime protocol at compile time. */
  private def constant(symbol: Symbol)(using Context): Int =
    symbol.info.finalResultType match {
      case ConstantType(value) => value.intValue
      case other               =>
        throw new IllegalStateException(
          s"VarHandle protocol constant ${symbol.show} has non-constant type $other"
        )
    }

  private def createVarHandle(
      tpe: Type,
      binding: Tree,
      coordinateClass: Tree,
      d: NirDefinitions
  )(using Context): Tree = {
    // Opaque aliases can hide a primitive field type outside its companion.
    // Select the implementation using the field's physical representation.
    val factory = TypeErasure.erasure(tpe).typeSymbol match {
      case sym if sym == defn.BooleanClass =>
        d.RuntimeVarHandle_createBooleanHandle
      case sym if sym == defn.ByteClass   => d.RuntimeVarHandle_createByteHandle
      case sym if sym == defn.ShortClass  => d.RuntimeVarHandle_createShortHandle
      case sym if sym == defn.CharClass   => d.RuntimeVarHandle_createCharHandle
      case sym if sym == defn.IntClass    => d.RuntimeVarHandle_createIntHandle
      case sym if sym == defn.LongClass   => d.RuntimeVarHandle_createLongHandle
      case sym if sym == defn.FloatClass  => d.RuntimeVarHandle_createFloatHandle
      case sym if sym == defn.DoubleClass =>
        d.RuntimeVarHandle_createDoubleHandle
      case _ => d.RuntimeVarHandle_createReferenceHandle
    }

    val args = List(binding, coordinateClass) ++
      (if factory == d.RuntimeVarHandle_createReferenceHandle then List(Literal(Constant(tpe)))
       else Nil)
    // The Native _VarHandle definition and the JDK VarHandle API have the same
    // NIR name. Expose the API type here without a runtime checked cast.
    Apply(ref(factory), args).withType(d.VarHandleClass.typeRef)
  }

  private def classLiteral(tree: Tree)(using Context)(using metadata: VarHandleMetadata): Option[Type] = tree match {
    case Literal(c) if c.tag == ClazzTag                                => Some(c.typeValue)
    case TypeApply(fun, List(tpe)) if fun.symbol == defn.Predef_classOf =>
      Some(tpe.tpe.widenDealias)
    case Typed(inner, _)   => classLiteral(inner)
    case selection: Select =>
      // Only the actual wrapper TYPE symbols denote primitive class tokens.
      // A precise Class[T] type is not proof of a literal value.
      metadata.classTokens.get(selection.symbol)
    case _ => None
  }

  private def provenLookup(tree: Tree)(using Context)(using metadata: VarHandleMetadata): Option[Symbol] = tree match {
    case Apply(fun, Nil) if fun.symbol == defnNir.MethodHandles_lookup                           => Some(ctx.owner.enclosingClass)
    case Apply(fun, List(target, caller)) if fun.symbol == defnNir.MethodHandles_privateLookupIn =>
      for {
        _ <- provenLookup(caller)
        tpe <- classLiteral(target)
        if tpe.typeSymbol.isClass && !tpe.typeSymbol.isPrimitiveValueClass
      } yield tpe.typeSymbol
    case Typed(inner, _)        => provenLookup(inner)
    case Inlined(_, Nil, inner) => provenLookup(inner)
    case _                      => None
  }

  /* A VarHandle lookup is intentionally not reflective on Native: both the
   * declaring class and member name must be present in this tree.  The handle
   * retains a direct raw-field binding, so it can be stored and passed around
   * like its JVM counterpart. */
  protected def rewriteVarHandleLookup(
      app: Apply,
      args: List[Tree],
      isStatic: Boolean
  )(using Context): Tree = {
    val defnNir = this.defnNir
    given VarHandleMetadata = varHandleMetadata

    def fail(message: String, tree: Tree = app): Tree = {
      report.error(message, tree.srcPos)
      app
    }

    val lookupClass = (app.fun match {
      case Select(lookup, _) => provenLookup(lookup)
      case _                 => None
    }) match {
      case Some(owner) => owner
      case None        => return fail("VarHandle requires a direct MethodHandles.lookup() with proven lookup privileges")
    }

    def matchesName(symbol: Symbol, name: String): Boolean =
      symbol.name.mangledString == name || symbol.name.toString == name ||
        symbol.name.unexpandedName.toString == name

    if args.size != 3 then return fail("VarHandle lookup requires class, field name, and field type")

    val targetType = classLiteral(args.head) match {
      case Some(tpe) => tpe
      case None      =>
        return fail(
          "VarHandle class must be a literal classOf[T] expression",
          args.head
        )
    }

    val fieldName = args(1) match {
      case Literal(c) if c.tag == StringTag => c.stringValue
      case _                                =>
        return fail("VarHandle field name must be a literal string", args(1))
    }

    val expectedType = classLiteral(args(2)) match {
      case Some(tpe) => tpe
      case None      =>
        return fail(
          "VarHandle field type must be a literal classOf[T] expression",
          args(2)
        )
    }

    def namedField(members: List[Symbol]) =
      members
        .collectFirst {
          case symbol if !symbol.is(Method) && matchesName(symbol, fieldName) => symbol.asSymDenotation
        }
        .orElse(members.collectFirst {
          case symbol if symbol.is(Accessor) && matchesName(symbol, fieldName) =>
            symbol.asSymDenotation.underlyingSymbol.asSymDenotation
        })

    val instanceField = namedField(targetType.baseClasses.flatMap(_.asClass.info.decls.toList))
    val companionField = namedField(targetType.typeSymbol.companionModule.info.decls.toList)
    // The fallback is only for the static/instance mismatch diagnostic below.
    // Never let an unrelated companion member shadow an instance field.
    val fieldOpt = if isStatic then companionField.orElse(instanceField)
    else instanceField.orElse(companionField)
    val field = fieldOpt match {
      case Some(field) => field
      case None        => return fail(s"${targetType.typeSymbol.show} does not contain field $fieldName")
    }

    val fieldSym = field.symbol
    val staticField = fieldSym.is(JavaStatic) || fieldSym.isScalaStatic
    if (isStatic != staticField) then
      return fail(
        s"VarHandle ${if isStatic then "static" else "instance"} lookup cannot target ${if staticField then "static" else "instance"} field $fieldName"
      )
    if !fieldSym.is(Mutable) then return fail(s"VarHandle requires a mutable field $fieldName")

    val fieldType = field.info.resultType.widenDealias
    if !(TypeErasure.erasure(fieldType) =:= TypeErasure.erasure(
          expectedType
        )) then
      return fail(
        s"VarHandle type does not match field $fieldName, expected $expectedType but got $fieldType"
      )

    val caller = lookupClass
    // A cross-package protected lookup has the lookup class as its receiver
    // constraint, even when the requested declaring class is a superclass.
    val coordinateType =
      if fieldSym.is(JavaDefined) && fieldSym.is(Protected) &&
          caller.enclosingPackageClass != fieldSym.owner.enclosingPackageClass &&
          (caller.typeRef <:< targetType) then caller.typeRef
      else targetType

    def accessible(symbol: Symbol): Boolean =
      // Scala instance vars acquire private JVM backing fields even when their
      // getters/setters are public. That separation has not happened yet here.
      symbol.isAccessibleFrom(coordinateType)(using ctx.withOwner(lookupClass)) &&
        ((!symbol.is(Private) && (symbol.is(JavaDefined) || staticField)) ||
        symbol.owner == lookupClass)
    if !accessible(fieldSym) then
      return fail(
        s"VarHandle cannot access field $fieldName: it is private to ${fieldSym.owner.show} " +
          s"but used from ${ctx.owner.enclosingClass.show}"
      )

    val rawPtr = (target: Tree) =>
      Apply(
        TypeApply(
          ref(defnNir.Intrinsics_classFieldRawPtr),
          List(TypeTree(targetType))
        ),
        List(target, Literal(Constant(fieldName)))
      ).withAttachment(VarHandleInterop.ResolvedField, fieldSym)
    val bindingType = varHandleMetadata.fieldBindingClass.typeRef
    val lambdaType = MethodType(List(termName("varHandleTarget")))(
      _ => List(defn.ObjectType),
      _ => defnNir.RawPtrClass.typeRef
    )
    def binding(body: List[Tree] => Tree): Tree = {
      val method = newAnonFun(ctx.owner, lambdaType)
      Closure(method, params => body(params.head).changeOwner(ctx.owner, method), targetType = bindingType)
    }
    if isStatic then {
      val module = targetType.typeSymbol.companionModule
      if module == NoSymbol then
        return fail(
          s"VarHandle cannot resolve static field owner for $fieldName"
        )
      val staticRawPtr = Apply(
        TypeApply(
          ref(defnNir.Intrinsics_classFieldRawPtr),
          List(TypeTree(targetType))
        ),
        List(
          TypeApply(
            Select(Literal(Constant(null)), nme.asInstanceOf_),
            List(TypeTree(targetType))
          ),
          Literal(Constant(fieldName))
        )
      ).withAttachment(NirDefinitions.NonErasedType, targetType)
      val fieldBinding = binding(_ => staticRawPtr)
      createVarHandle(
        fieldType,
        fieldBinding,
        Literal(Constant(null)),
        defnNir
      )
    } else {
      val fieldBinding = binding(params =>
        rawPtr(
          TypeApply(
            Select(params.head, nme.asInstanceOf_),
            List(TypeTree(coordinateType))
          )
        )
      )
      createVarHandle(
        fieldType,
        fieldBinding,
        Literal(Constant(coordinateType)),
        defnNir
      )
    }
  }

  protected def rewriteVarHandleAccess(
      app: Apply,
      accessMethod: Symbol,
      callArgs: List[Tree]
  )(using Context): Tree = {
    val d = defnNir
    given metadata: VarHandleMetadata = varHandleMetadata
    val name = accessMethod.name.toString
    import Constants.*
    import Names.*

    // Older Scala 3 compilers type these as ordinary Java varargs calls.
    // Unpack only a literal varargs pack, not an array-valued coordinate or operand.
    val args =
      if accessMethod.info.paramInfoss.flatten.lastOption.exists(_.isRepeatedParam) then
        callArgs match {
          case List(SeqLiteral(elements, _))           => elements
          case List(Typed(SeqLiteral(elements, _), _)) => elements
          case _                                       => callArgs
        }
      else callArgs

    def boxed(tree: Tree): Tree = TypeApply(
      Select(tree, nme.asInstanceOf_),
      List(TypeTree(defn.ObjectType))
    )

    def cast(tree: Tree): Tree = TypeApply(
      Select(tree, nme.asInstanceOf_),
      List(TypeTree(app.tpe.widenDealias))
    )

    def fail(message: String): Tree = { report.error(message, app.srcPos); app }

    val receiver = app.fun match {
      case Select(q, _) => q;
      case _            => return fail(s"VarHandle.$name has an unsupported call shape")
    }

    def invoke(mode: AccessMode): Tree = {
      val method = mode.operation
      def protocolValue(symbol: Symbol): Tree = Literal(Constant(constant(symbol)))
      val extra = mode.bitwiseOperation.toList.map(protocolValue) ::: List(protocolValue(mode.memoryOrder))
      // The first protocol parameter is the receiver coordinate; the trailing
      // parameters are compile-time-selected access-mode arguments.
      val operandCount =
        method.info.paramInfoss.flatten.tail.dropRight(extra.size).size
      val (coordinate, operands) =
        if args.size == operandCount then (Literal(Constant(null)), args)
        else if args.nonEmpty && args.tail.size == operandCount then (boxed(args.head), args.tail)
        else return fail(s"VarHandle.$name has an invalid coordinate/value arity")
      val bindings = scala.collection.mutable.ListBuffer.empty[Tree]

      def save(tree: Tree, name: String): Tree = {
        val symbol = newSymbol(
          summon[Context].owner,
          termName(name),
          Flags.Synthetic,
          tree.tpe.widenDealias
        ).asTerm
        bindings += ValDef(symbol, tree)
        ref(symbol)
      }

      val savedReceiver = save(receiver, "varHandleReceiver")
      val savedCoordinate = save(coordinate, "varHandleCoordinate")
      val savedOperands = operands.zipWithIndex.map { (operand, index) =>
        save(operand, s"varHandleOperand$index")
      }

      val access = TypeApply(
        Select(savedReceiver, nme.asInstanceOf_),
        List(TypeTree(d.NativeVarHandleClass.typeRef))
      )

      val variableType =
        operands.headOption.fold(app.tpe.widenDealias)(_.tpe.widenDealias)
      val operation = metadata.operations(method)
      val typedMethod = metadata.typedMethods((method, variableKind(variableType)))
      val returnsValue = method.info.finalResultType =:= defn.IntType
      val discardedResult =
        app.hasAttachment(DiscardedResult) || (app.tpe =:= defn.UnitType)
      val resultWidening =
        if returnsValue && !discardedResult then wideningMethod(variableType, app.tpe)
        else None
      val resultBoxing =
        if returnsValue && !discardedResult && variableKind(
              variableType
            ) != VariableKind.Reference && variableKind(app.tpe) == VariableKind.Reference then
          Some(boxingMethod(variableType)).filter(_.info.finalResultType <:< app.tpe.widenDealias)
        else None
      val incompatibleOperands =
        operands.exists(t => !(t.tpe.widenDealias =:= variableType))
      val incompatibleResult = returnsValue && !discardedResult &&
        variableKind(app.tpe) != variableKind(
          variableType
        ) && resultWidening.isEmpty && resultBoxing.isEmpty

      def adapted: Tree = {
        def boxOperand(tree: Tree): Tree = if variableKind(tree.tpe) == VariableKind.Reference then boxed(tree)
        else {
          val method = boxingMethod(tree.tpe)
          Apply(
            Select(ref(defn.BoxesRunTimeModule), method.name),
            List(tree)
          )
        }

        def classLiteral(tree: Option[Tree]): Tree = Literal(
          Constant(tree.fold(defn.UnitType)(_.tpe.widenDealias))
        )

        val twoOperands = savedOperands.size == 2
        val expected = if twoOperands then savedOperands.headOption else None
        val value = savedOperands.lastOption
        val operationSymbol =
          d.NativeVarHandleAccessOperationModule.requiredMethod(
            operation.head.toUpper.toString + operation.tail
          )
        val call = Apply(
          Select(
            access,
            d.NativeVarHandleClass.requiredMethod("invokeAdapted").name
          ),
          List(
            Literal(Constant(constant(operationSymbol))),
            savedCoordinate,
            expected.fold[Tree](Literal(Constant(null)))(boxOperand),
            value.fold[Tree](Literal(Constant(null)))(boxOperand),
            classLiteral(expected),
            classLiteral(value),
            Literal(Constant(if discardedResult then defn.UnitType
            else app.tpe.widenDealias)),
            protocolValue(mode.memoryOrder),
            protocolValue(mode.bitwiseOperation.getOrElse(d.VarHandleBitwiseOperationOr))
          )
        )

        if app.tpe =:= defn.UnitType then Block(List(call), Literal(Constant(())))
        else if discardedResult && variableKind(app.tpe) == VariableKind.Reference then Block(List(call), Literal(Constant(null)))
        else if variableKind(app.tpe) == VariableKind.Reference then cast(call)
        else {
          val resultType = app.tpe.widenDealias
          val wrapperType = boxingMethod(resultType).info.finalResultType
          val method = wrapperType.typeSymbol.requiredMethod(
            metadata.conversionNames(resultType.typeSymbol).unboxingMethod
          )

          Apply(
            Select(
              TypeApply(
                Select(call, nme.asInstanceOf_),
                List(TypeTree(wrapperType))
              ),
              method.name
            ),
            Nil
          )
        }
      }

      val typedAccess = if incompatibleOperands || incompatibleResult then adapted
      else {
        val values = if variableKind(variableType) == VariableKind.Reference then savedOperands.map(boxed)
        else savedOperands
        val boxedRead =
          method == d.NativeVarHandle_get && variableKind(app.tpe) == VariableKind.Reference
        val call = Apply(
          Select(
            access,
            if boxedRead then d.NativeVarHandleClass.requiredMethod("getBoxedReference").name
            else typedMethod.name
          ),
          savedCoordinate :: values
            ::: Option.when(boxedRead)(Literal(Constant(app.tpe.widenDealias))).toList
            ::: extra
        )

        if app.tpe =:= defn.UnitType then Block(List(call), Literal(Constant(())))
        // Preserve the enclosing expression's reference-typed joins without
        // boxing the primitive witness that the source discards.
        else if discardedResult && variableKind(app.tpe) == VariableKind.Reference then Block(List(call), Literal(Constant(null)))
        else if variableKind(variableType) == VariableKind.Reference then cast(call)
        else if resultBoxing.nonEmpty then
          cast(
            Apply(
              Select(
                ref(defn.BoxesRunTimeModule),
                resultBoxing.get.name
              ),
              List(call)
            )
          )
        else
          resultWidening.fold(call) { conversion =>
            Select(call, conversion.name)
          }
      }

      def declaredType(tree: Option[Tree]): Tree =
        Literal(Constant(tree.fold(defn.UnitType)(_.tpe.widenDealias)))

      val operationSymbol = d.NativeVarHandleAccessOperationModule.requiredMethod(
        operation.head.toUpper.toString + operation.tail
      )
      // The handle chooses invocation behavior at runtime. Validate before either access path.
      val validation = Apply(
        Select(access, d.NativeVarHandleClass.requiredMethod("validateInvocation").name),
        List(
          protocolValue(operationSymbol),
          if args.size == operandCount then Literal(Constant(null)) else declaredType(args.headOption),
          declaredType(if operands.size == 2 then operands.headOption else None),
          declaredType(operands.lastOption),
          Literal(Constant(if returnsValue && discardedResult then defn.UnitType else app.tpe.widenDealias))
        )
      )
      Block(
        bindings.toList :+ validation,
        if operands.isEmpty then typedAccess
        else if incompatibleOperands || incompatibleResult then typedAccess
        else
          If(
            Apply(
              Select(
                access,
                d.NativeVarHandleClass
                  .requiredMethod("isExactVariableType")
                  .name
              ),
              List(Literal(Constant(variableType)))
            ),
            typedAccess,
            adapted
          )
      )
    }

    metadata.accessModes.get(accessMethod.name) match {
      case Some(mode) => invoke(mode)
      case None       => app
    }
  }
}
