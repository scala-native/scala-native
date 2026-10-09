package scala.scalanative
package nscplugin

import scala.tools.nsc.Global

// scalafmt: { maxColumn = 160 }
/** Compiler lowering for field VarHandle lookup and access. */
private[nscplugin] trait VarHandleInterop[G <: Global with Singleton] {
  self: NirPhase[G] =>

  import global._
  import global.definitions._

  import nirAddons._
  import nirAddons.nirDefinitions._

  protected lazy val VarHandleClass = rootMirror.getClassIfDefined("java.lang.invoke.VarHandle")
  protected lazy val VarHandleLookupClass = rootMirror.getRequiredClass("java.lang.invoke.MethodHandles.Lookup")
  protected lazy val FindVarHandle = VarHandleLookupClass.info.member(newTermName("findVarHandle"))
  protected lazy val FindStaticVarHandle = VarHandleLookupClass.info.member(newTermName("findStaticVarHandle"))
  private lazy val MethodHandlesModule = rootMirror.getRequiredModule("java.lang.invoke.MethodHandles")
  private lazy val LookupMethod = MethodHandlesModule.info.member(newTermName("lookup"))
  private lazy val PrivateLookupMethod = MethodHandlesModule.info.member(newTermName("privateLookupIn"))
  private lazy val NativeVarHandleClass = rootMirror.getRequiredClass("scala.scalanative.runtime.NativeVarHandle")
  private lazy val VarHandleFactoryModule = rootMirror.getRequiredModule("scala.scalanative.runtime.VarHandle")

  private def protocolConstant(group: String, name: String): Tree = {
    val module = rootMirror.getRequiredModule(
      "scala.scalanative.runtime.NativeVarHandle." + group
    )
    getMember(module, newTermName(name)).info.finalResultType match {
      case ConstantType(value) => Literal(value)
      case other               => abort("non-constant VarHandle protocol: " + other)
    }
  }

  private final class AccessMode(
      val operation: VarHandleProtocol.Operation,
      val memoryOrder: VarHandleProtocol.MemoryOrder,
      val bitwiseOperation: Option[VarHandleProtocol.BitwiseOperation] = None
  )

  private final class PrimitiveConversions(val wideningMethod: TermName, val unboxingMethod: TermName)

  private object VarHandleProtocol {
    sealed abstract class VariableKind(val memberName: String)
    object VariableKind {
      case object Boolean extends VariableKind("Boolean")
      case object Byte extends VariableKind("Byte")
      case object Short extends VariableKind("Short")
      case object Char extends VariableKind("Char")
      case object Int extends VariableKind("Int")
      case object Long extends VariableKind("Long")
      case object Float extends VariableKind("Float")
      case object Double extends VariableKind("Double")
      case object Reference extends VariableKind("Reference")
    }

    sealed abstract class Operation(val methodPrefix: String) {
      def method(kind: VariableKind): Symbol =
        getMember(
          NativeVarHandleClass,
          newTermName(methodPrefix + kind.memberName)
        )
    }
    object Operation {
      case object Get extends Operation("get")
      case object Set extends Operation("set")
      case object Compare extends Operation("compare")
      case object WeakCompare extends Operation("weakCompare")
      case object CompareExchange extends Operation("compareExchange")
      case object Exchange extends Operation("exchange")
      case object Add extends Operation("add")
      case object Bitwise extends Operation("bitwise")
    }

    sealed abstract class MemoryOrder(val memberName: String) {
      def tree: Tree = protocolConstant("MemoryOrder", memberName)
    }
    object MemoryOrder {
      case object Plain extends MemoryOrder("Plain")
      case object Volatile extends MemoryOrder("Volatile")
      case object Acquire extends MemoryOrder("Acquire")
      case object Release extends MemoryOrder("Release")
    }

    sealed abstract class BitwiseOperation(val memberName: String) {
      def tree: Tree = protocolConstant("BitwiseOperation", memberName)
    }
    object BitwiseOperation {
      case object Or extends BitwiseOperation("Or")
      case object And extends BitwiseOperation("And")
      case object Xor extends BitwiseOperation("Xor")
    }
  }

  // Signature-polymorphic calls have synthetic symbols. The phase proves
  // their owner is VarHandle before dispatching by the declared member name.
  private lazy val accessModes: Map[Name, AccessMode] = {
    import VarHandleProtocol._
    import Operation._
    import MemoryOrder._
    List(
      ("get", new AccessMode(Get, Plain)),
      ("getOpaque", new AccessMode(Get, Plain)),
      ("getVolatile", new AccessMode(Get, Volatile)),
      ("getAcquire", new AccessMode(Get, Acquire)),
      ("set", new AccessMode(Set, Plain)),
      ("setOpaque", new AccessMode(Set, Plain)),
      ("setVolatile", new AccessMode(Set, Volatile)),
      ("setRelease", new AccessMode(Set, Release)),
      ("compareAndSet", new AccessMode(Compare, Volatile)),
      ("weakCompareAndSet", new AccessMode(WeakCompare, Volatile)),
      ("weakCompareAndSetPlain", new AccessMode(WeakCompare, Plain)),
      ("weakCompareAndSetAcquire", new AccessMode(WeakCompare, Acquire)),
      ("weakCompareAndSetRelease", new AccessMode(WeakCompare, Release)),
      ("compareAndExchange", new AccessMode(CompareExchange, Volatile)),
      ("compareAndExchangeAcquire", new AccessMode(CompareExchange, Acquire)),
      ("compareAndExchangeRelease", new AccessMode(CompareExchange, Release)),
      ("getAndSet", new AccessMode(Exchange, Volatile)),
      ("getAndSetAcquire", new AccessMode(Exchange, Acquire)),
      ("getAndSetRelease", new AccessMode(Exchange, Release)),
      ("getAndAdd", new AccessMode(Add, Volatile)),
      ("getAndAddAcquire", new AccessMode(Add, Acquire)),
      ("getAndAddRelease", new AccessMode(Add, Release)),
      ("getAndBitwiseOr", new AccessMode(Bitwise, Volatile, Some(BitwiseOperation.Or))),
      ("getAndBitwiseOrAcquire", new AccessMode(Bitwise, Acquire, Some(BitwiseOperation.Or))),
      ("getAndBitwiseOrRelease", new AccessMode(Bitwise, Release, Some(BitwiseOperation.Or))),
      ("getAndBitwiseAnd", new AccessMode(Bitwise, Volatile, Some(BitwiseOperation.And))),
      ("getAndBitwiseAndAcquire", new AccessMode(Bitwise, Acquire, Some(BitwiseOperation.And))),
      ("getAndBitwiseAndRelease", new AccessMode(Bitwise, Release, Some(BitwiseOperation.And))),
      ("getAndBitwiseXor", new AccessMode(Bitwise, Volatile, Some(BitwiseOperation.Xor))),
      ("getAndBitwiseXorAcquire", new AccessMode(Bitwise, Acquire, Some(BitwiseOperation.Xor))),
      ("getAndBitwiseXorRelease", new AccessMode(Bitwise, Release, Some(BitwiseOperation.Xor)))
    ).map { case (name, mode) => getMember(VarHandleClass, newTermName(name)).name -> mode }.toMap
  }

  private lazy val primitiveKinds: Map[Symbol, VarHandleProtocol.VariableKind] = {
    import VarHandleProtocol.VariableKind._
    Map(
      BooleanClass -> Boolean,
      ByteClass -> Byte,
      ShortClass -> Short,
      CharClass -> Char,
      IntClass -> Int,
      LongClass -> Long,
      FloatClass -> Float,
      DoubleClass -> Double
    )
  }

  private lazy val primitiveClassTokens = List(
    "Boolean" -> BooleanTpe,
    "Byte" -> ByteTpe,
    "Short" -> ShortTpe,
    "Character" -> CharTpe,
    "Integer" -> IntTpe,
    "Long" -> LongTpe,
    "Float" -> FloatTpe,
    "Double" -> DoubleTpe
  ).map {
    case (name, tpe) =>
      getMember(rootMirror.getRequiredModule("java.lang." + name), newTermName("TYPE")) -> tpe
  }.toMap

  private def variableKind(tpe: Type): VarHandleProtocol.VariableKind =
    primitiveKinds.getOrElse(tpe.dealiasWiden.typeSymbol, VarHandleProtocol.VariableKind.Reference)

  private lazy val boxingMethods = BoxesRunTimeModule.info.decls
    .filter { method =>
      method.isMethod && (method.info.paramLists.flatten match {
        case List(input) =>
          primitiveKinds.contains(input.info.typeSymbol) &&
            method.info.finalResultType <:< ObjectTpe
        case _ => false
      })
    }
    .map(method => method.info.paramLists.head.head.info.typeSymbol -> method)
    .toMap

  private def boxingMethod(tpe: Type): Symbol = boxingMethods(tpe.dealiasWiden.typeSymbol)

  private lazy val conversionNames: Map[Symbol, PrimitiveConversions] = Map(
    BooleanClass -> new PrimitiveConversions(newTermName("toBoolean"), newTermName("booleanValue")),
    ByteClass -> new PrimitiveConversions(newTermName("toByte"), newTermName("byteValue")),
    ShortClass -> new PrimitiveConversions(newTermName("toShort"), newTermName("shortValue")),
    CharClass -> new PrimitiveConversions(newTermName("toChar"), newTermName("charValue")),
    IntClass -> new PrimitiveConversions(newTermName("toInt"), newTermName("intValue")),
    LongClass -> new PrimitiveConversions(newTermName("toLong"), newTermName("longValue")),
    FloatClass -> new PrimitiveConversions(newTermName("toFloat"), newTermName("floatValue")),
    DoubleClass -> new PrimitiveConversions(newTermName("toDouble"), newTermName("doubleValue"))
  )

  private lazy val wideningTargets: Map[Symbol, List[Symbol]] = {
    val numeric: List[Symbol] = List(ByteClass, ShortClass, IntClass, LongClass, FloatClass, DoubleClass)
    numeric.zipWithIndex.map { case (symbol, index) => symbol -> numeric.drop(index + 1) }.toMap +
      (CharClass -> numeric.drop(2))
  }

  private def wideningMethod(source: Type, target: Type): Option[Symbol] = {
    val sourceSymbol = source.dealiasWiden.typeSymbol
    val targetSymbol = target.dealiasWiden.typeSymbol
    if (wideningTargets.getOrElse(sourceSymbol, Nil).contains(targetSymbol))
      Some(getMember(sourceSymbol, conversionNames(targetSymbol).wideningMethod))
    else None
  }

  protected trait VarHandleTransformer extends Transformer {
    protected def unit: CompilationUnit

    private case object DiscardedVarHandleResult

    /** Follow discarded result paths without marking arguments or bindings. */
    protected def discardVarHandleResult(tree: Tree): Unit = tree match {
      case app @ Apply(fun, _) if fun.symbol.owner == VarHandleClass =>
        app.updateAttachment(DiscardedVarHandleResult)
      // Scala 2.12 has already lowered matches into synthetic case labels.
      // Only arguments to this exact join label are discarded result paths.
      case Block(stats, last: LabelDef) if treeInfo.hasSynthCaseSymbol(last) =>
        val resultLabel = last.symbol
        val joins = new Traverser {
          override def traverse(tree: Tree): Unit = tree match {
            case Apply(fun, args) if fun.symbol == resultLabel =>
              args.foreach(discardVarHandleResult)
            case _ => super.traverse(tree)
          }
        }

        stats.foreach(joins.traverse)
      case Block(_, expr)      => discardVarHandleResult(expr)
      case If(_, thenp, elsep) =>
        discardVarHandleResult(thenp)
        discardVarHandleResult(elsep)
      case Match(_, cases)     => cases.foreach(c => discardVarHandleResult(c.body))
      case Try(expr, cases, _) =>
        discardVarHandleResult(expr)
        cases.foreach(c => discardVarHandleResult(c.body))
      case Typed(expr, tpt) if tpt.tpe =:= UnitTpe =>
        discardVarHandleResult(expr)
      case _ => ()
    }

    protected def rewriteVarHandleLookup(
        app: Apply,
        args: List[Tree],
        isStatic: Boolean
    ): Tree = {
      def fail(message: String): Tree = {
        reporter.error(app.pos, message); app
      }

      def provenLookup(tree: Tree): Option[Symbol] = tree match {
        case Apply(fun, Nil) if fun.symbol == LookupMethod => Some(currentOwner.enclClass)
        case Apply(fun, List(Literal(Constant(tpe: Type)), caller))
            if fun.symbol == PrivateLookupMethod && tpe.typeSymbol.isClass &&
              !primitiveKinds.contains(tpe.typeSymbol) =>
          provenLookup(caller).map(_ => tpe.typeSymbol)
        case Typed(inner, _) => provenLookup(inner)
        case _               => None
      }

      val lookupClass = (app.fun match {
        case Select(lookup, _) => provenLookup(lookup)
        case _                 => None
      }) match {
        case Some(owner) => owner
        case None        => return fail("VarHandle requires a direct MethodHandles.lookup() with proven lookup privileges")
      }

      val target = args.headOption match {
        case Some(Literal(Constant(tpe: Type))) => tpe.dealiasWiden
        case _                                  =>
          return fail("VarHandle owner must be a literal classOf[T] expression")
      }

      val name = args.lift(1) match {
        case Some(Literal(Constant(value: String))) => value
        case _                                      => return fail("VarHandle field name must be a literal string")
      }

      val expected = args.lift(2) match {
        case Some(Literal(Constant(tpe: Type))) => tpe.dealiasWiden
        case Some(selection: Select)            =>
          primitiveClassTokens.getOrElse(
            selection.symbol,
            return fail("VarHandle variable type must be a literal class")
          )
        case _ => return fail("VarHandle variable type must be a literal class")
      }

      val field = target.baseClasses.iterator
        .flatMap(_.info.decls.iterator)
        .find(s => s.isField && s.nameString == name)
        .getOrElse(
          return fail(s"${target.typeSymbol} does not contain field $name")
        )
      val staticField = field.isStaticMember && !field.owner.isModuleClass
      if (isStatic != staticField)
        return fail(s"VarHandle lookup cannot target ${if (staticField) "static"
          else "instance"} field $name")
      if (!field.isVariable)
        return fail(s"VarHandle requires a mutable field $name")

      val fieldType = field.tpe.asSeenFrom(target, field.owner).finalResultType
      if (!(fieldType.erasure =:= expected.erasure))
        return fail(s"VarHandle type does not match field $name")

      val caller = lookupClass
      val coordinateType =
        if (field.isJavaDefined && field.isProtected &&
            caller.enclosingPackageClass != field.owner.enclosingPackageClass &&
            (caller.tpe <:< target)) caller.tpe
        else target

      def accessible(s: Symbol): Boolean =
        (s.owner == lookupClass || !s.isJavaDefined ||
          (s.isProtected && (caller.tpe <:< s.owner.tpe)) ||
          typer.atOwner(lookupClass).context.isAccessible(s, coordinateType)) &&
          (!s.isPrivate || s.owner == lookupClass)
      if (!accessible(field))
        return fail(s"VarHandle cannot access field $name")

      val paramName = unit.freshTermName("varHandleTarget")
      val param =
        ValDef(Modifiers(Flag.PARAM), paramName, TypeTree(ObjectTpe), EmptyTree)
      val coordinateReceiver = TypeApply(
        Select(
          if (isStatic) Literal(Constant(null)) else Ident(paramName),
          nme.asInstanceOf_
        ),
        List(TypeTree(coordinateType))
      )
      val receiver = TypeApply(
        Select(coordinateReceiver, nme.asInstanceOf_),
        List(TypeTree(field.owner.tpe))
      )

      val raw = Apply(
        gen.mkAttributedRef(ClassFieldRawPtrMethod),
        List(receiver, Literal(Constant(name)))
      )

      val kind = variableKind(fieldType)
      // The SAM's concrete RawPtr result avoids erased Function1 boxing.
      val binding = Function(List(param), raw)
      val factory = getMember(
        VarHandleFactoryModule,
        newTermName("create" + kind.memberName + "Handle")
      )

      val coordinateClass = if (isStatic) Literal(Constant(null)) else Literal(Constant(coordinateType))
      val variableClass =
        if (kind == VarHandleProtocol.VariableKind.Reference) List(Literal(Constant(fieldType))) else Nil
      val factoryArgs = List(binding, coordinateClass) ::: variableClass
      typer
        .atOwner(currentOwner)
        .typed(Apply(gen.mkAttributedRef(factory), factoryArgs))
        // _VarHandle is renamed to the JDK API class in NIR generation.
        // This is a compile-time view, not a runtime checked cast.
        .setType(app.tpe)
    }

    protected def rewriteVarHandleAccess(
        app: Apply,
        receiver: Tree,
        method: Symbol,
        args: List[Tree]
    ): Tree = {
      import VarHandleProtocol._
      import Operation._
      import MemoryOrder._
      val name = method.nameString
      val mode = accessModes.getOrElse(method.name, return super.transform(app))
      val operation = mode.operation

      val extras = mode.bitwiseOperation.toList.map(_.tree) ::: List(mode.memoryOrder.tree)
      val prototype = operation.method(VariableKind.Int)
      val operandCount =
        prototype.info.paramLists.flatten.tail.dropRight(extras.size).size
      val (coordinate, operands) =
        if (args.size == operandCount) (Literal(Constant(null)), args)
        else if (args.size == operandCount + 1) (args.head, args.tail)
        else {
          reporter.error(
            app.pos,
            s"VarHandle.$name has an invalid coordinate/value arity"
          );
          return app
        }
      val variableType = operands.headOption.fold(app.tpe)(_.tpe).dealiasWiden
      val kind = variableKind(variableType)
      val bindings = scala.collection.mutable.ListBuffer.empty[Tree]

      def save(tree: Tree, prefix: String): Tree = {
        val savedName = unit.freshTermName(prefix)
        bindings += ValDef(
          Modifiers(Flag.SYNTHETIC),
          savedName,
          TypeTree(tree.tpe),
          tree
        )
        Ident(savedName)
      }

      val savedReceiver =
        save(receiver, "varHandleReceiver")
      val savedCoordinate =
        TypeApply(
          Select(save(coordinate, "varHandleCoordinate"), nme.asInstanceOf_),
          List(TypeTree(ObjectTpe))
        )
      val savedOperands = operands.map(t => save(t, "varHandleOperand"))
      val access = TypeApply(
        Select(savedReceiver, nme.asInstanceOf_),
        List(TypeTree(NativeVarHandleClass.tpe))
      )

      val valueResult = prototype.info.finalResultType =:= IntTpe
      val discardedResult = app
        .hasAttachment[DiscardedVarHandleResult.type] || (app.tpe =:= UnitTpe)
      val resultWidening =
        if (valueResult && !discardedResult) wideningMethod(variableType, app.tpe)
        else None
      val resultBoxing =
        if (valueResult && !discardedResult && kind != VariableKind.Reference && variableKind(
              app.tpe
            ) == VariableKind.Reference)
          Some(boxingMethod(variableType)).filter(_.info.finalResultType <:< app.tpe.dealiasWiden)
        else None
      val invalid =
        operands.exists(t => !(t.tpe.dealiasWiden =:= variableType)) ||
          (valueResult && !discardedResult && variableKind(
            app.tpe
          ) != kind && resultWidening.isEmpty && resultBoxing.isEmpty)

      def adapted: Tree = {
        def boxed(tree: Tree): Tree =
          TypeApply(Select(tree, nme.asInstanceOf_), List(TypeTree(ObjectTpe)))

        def boxOperand(tree: Tree, source: Tree): Tree =
          if (variableKind(source.tpe) == VariableKind.Reference) boxed(tree)
          else {
            val method = boxingMethod(source.tpe)
            Apply(
              Select(gen.mkAttributedRef(BoxesRunTimeModule), method.name),
              List(tree)
            )
          }
        val expected =
          if (operands.size == 2) savedOperands.headOption else None
        val value = savedOperands.lastOption

        def operandType(index: Int): Tree = Literal(
          Constant(operands.lift(index).fold[Type](UnitTpe)(_.tpe.dealiasWiden))
        )

        val call = Apply(
          Select(access, newTermName("invokeAdapted")),
          List(
            protocolConstant("AccessOperation", operation.toString),
            savedCoordinate,
            expected.fold[Tree](Literal(Constant(null)))(t => boxOperand(t, operands.head)),
            value.fold[Tree](Literal(Constant(null)))(t => boxOperand(t, operands.last)),
            if (operands.size == 2) operandType(0)
            else Literal(Constant(UnitTpe)),
            operandType(operands.size - 1),
            Literal(
              Constant(if (discardedResult) UnitTpe else app.tpe.dealiasWiden)
            ),
            mode.memoryOrder.tree,
            mode.bitwiseOperation.fold[Tree](BitwiseOperation.Or.tree)(_.tree)
          )
        )

        if (app.tpe =:= UnitTpe) Block(List(call), Literal(Constant(())))
        else if (discardedResult && variableKind(
              app.tpe
            ) == VariableKind.Reference)
          Block(List(call), Literal(Constant(null)))
        else if (variableKind(app.tpe) == VariableKind.Reference)
          TypeApply(Select(call, nme.asInstanceOf_), List(TypeTree(app.tpe)))
        else {
          val resultType = app.tpe.dealiasWiden
          val wrapperType = boxingMethod(resultType).info.finalResultType
          val method = getMember(
            wrapperType.typeSymbol,
            conversionNames(resultType.typeSymbol).unboxingMethod
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

      val typedAccess =
        if (invalid) adapted
        else {
          val boxedRead = operation == Operation.Get && variableKind(
            app.tpe
          ) == VariableKind.Reference
          val call = Apply(
            Select(
              access,
              if (boxedRead)
                NativeVarHandleClass.info
                  .member(newTermName("getBoxedReference"))
                  .name
              else operation.method(kind).name
            ),
            savedCoordinate :: savedOperands.map { operand =>
              if (kind == VariableKind.Reference)
                TypeApply(Select(operand, nme.asInstanceOf_), List(TypeTree(ObjectTpe)))
              else operand
            }
              ::: (if (boxedRead) List(Literal(Constant(app.tpe.dealiasWiden))) else Nil)
              ::: extras
          )

          if (app.tpe =:= UnitTpe) Block(List(call), Literal(Constant(())))
          else if (discardedResult && variableKind(app.tpe) == VariableKind.Reference)
            Block(List(call), Literal(Constant(null)))
          else if (kind == VariableKind.Reference)
            TypeApply(Select(call, nme.asInstanceOf_), List(TypeTree(app.tpe)))
          else if (resultBoxing.nonEmpty)
            TypeApply(
              Select(
                Apply(
                  Select(
                    gen.mkAttributedRef(BoxesRunTimeModule),
                    resultBoxing.get.name
                  ),
                  List(call)
                ),
                nme.asInstanceOf_
              ),
              List(TypeTree(app.tpe))
            )
          else
            resultWidening.fold[Tree](call) { conversion =>
              Select(call, conversion.name)
            }
        }
      def declaredType(tree: Option[Tree]): Tree =
        Literal(Constant(tree.fold[Type](UnitTpe)(_.tpe.dealiasWiden)))

      // The handle chooses invocation behavior at runtime. Validate before either access path.
      val validation = Apply(
        Select(access, getMember(NativeVarHandleClass, newTermName("validateInvocation")).name),
        List(
          protocolConstant("AccessOperation", operation.toString),
          if (args.size == operandCount) Literal(Constant(null)) else declaredType(args.headOption),
          declaredType(if (operands.size == 2) operands.headOption else None),
          declaredType(operands.lastOption),
          Literal(Constant(if (valueResult && discardedResult) UnitTpe else app.tpe.dealiasWiden))
        )
      )
      val rewritten =
        Block(
          bindings.toList :+ validation,
          if (operands.isEmpty) typedAccess
          else if (invalid) typedAccess
          else
            If(
              Apply(
                Select(access, newTermName("isExactVariableType")),
                List(Literal(Constant(variableType)))
              ),
              typedAccess,
              adapted
            )
        )
      // The fast and adapted branches reuse references to the saved arguments.
      // Scala 2's typer mutates trees; give each occurrence its own tree.
      typer.atOwner(currentOwner).typed(rewritten.duplicate)
    }
  }
}
