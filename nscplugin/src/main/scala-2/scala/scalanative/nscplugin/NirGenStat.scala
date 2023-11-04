package scala.scalanative
package nscplugin

import scala.collection.mutable
import scala.reflect.internal.Flags._
import scala.scalanative.nir.Defn.Define.DebugInfo
import scala.tools.nsc.Properties
import scala.scalanative.util.unsupported
import scala.scalanative.util.ScopedVar.scoped
import scala.tools.nsc

trait NirGenStat[G <: nsc.Global with Singleton] { self: NirGenPhase[G] =>

  import global._
  import definitions._
  import nirAddons._
  import nirDefinitions._
  import SimpleType.{fromType, fromSymbol}

  val reflectiveInstantiationInfo =
    mutable.UnrolledBuffer.empty[ReflectiveInstantiationBuffer]

  protected val generatedMirrorClasses = mutable.Map.empty[Symbol, MirrorClass]

  protected case class MirrorClass(
      defn: nir.Defn.Class,
      forwarders: Seq[nir.Defn.Define]
  )

  def isStaticModule(sym: Symbol): Boolean =
    sym.isModuleClass && !sym.isLifted

  class MethodEnv(val fresh: nir.Fresh) {
    private val env = mutable.Map.empty[Symbol, nir.Val]

    def enter(sym: Symbol, value: nir.Val): Unit = {
      env += ((sym, value))
    }

    def enterLabel(ld: LabelDef): nir.Local = {
      val local = fresh()
      enter(ld.symbol, nir.Val.Local(local, nir.Type.Ptr))
      local
    }

    def resolve(sym: Symbol): nir.Val = {
      env(sym)
    }

    def resolveLabel(ld: LabelDef): nir.Local = {
      val nir.Val.Local(n, nir.Type.Ptr) = resolve(ld.symbol)
      n
    }
  }

  class CollectMethodInfo extends Traverser {
    var mutableVars = Set.empty[Symbol]
    var labels = Set.empty[LabelDef]

    override def traverse(tree: Tree) = {
      tree match {
        case label: LabelDef =>
          labels += label
        case Assign(id @ Ident(_), _) =>
          mutableVars += id.symbol
        case _ =>
          ()
      }
      super.traverse(tree)
    }

    def collect(tree: Tree) = {
      traverse(tree)
      this
    }
  }

  class StatBuffer {
    private val buf = mutable.UnrolledBuffer.empty[nir.Defn]
    def toSeq: Seq[nir.Defn] = buf.toSeq

    def +=(defn: nir.Defn): Unit = {
      buf += defn
    }

    def isEmpty = buf.isEmpty
    def nonEmpty = buf.nonEmpty

    def genClass(cd: ClassDef): Unit = {
      val sym = cd.symbol

      scoped(
        curClassSym := sym,
        curClassFresh := nir.Fresh()
      ) {
        if (sym.isStruct) genStruct(cd)
        else genNormalClass(cd)
      }
    }

    def genStruct(cd: ClassDef): Unit = {
      val sym = cd.symbol
      val attrs = genStructAttrs(sym)
      val name = genTypeName(sym)
      val fields = genStructFields(sym)
      val body = cd.impl.body

      buf += nir.Defn.Class(attrs, name, None, Seq.empty)(cd.pos)
      genMethods(cd)
    }

    def genStructAttrs(sym: Symbol): nir.Attrs = nir.Attrs.None

    def genNormalClass(cd: ClassDef): Unit = {
      val sym = cd.symbol
      def attrs = genClassAttrs(cd)
      def name = genTypeName(sym)
      def parent = genClassParent(sym)
      def traits = genClassInterfaces(sym)

      implicit val pos: nir.Position = cd.pos

      buf += {
        if (sym.isScalaModule) nir.Defn.Module(attrs, name, parent, traits)
        else if (sym.isTraitOrInterface) nir.Defn.Trait(attrs, name, traits)
        else nir.Defn.Class(attrs, name, parent, traits)
      }

      genReflectiveInstantiation(cd)
      genClassFields(cd)
      genMethods(cd)
      genMirrorClass(cd)
    }

    def genClassParent(sym: Symbol): Option[nir.Global.Top] = {
      if (sym.isExternType &&
          sym.superClass != ObjectClass) {
        reporter.error(
          sym.pos,
          s"Extern object can only extend extern traits"
        )
      }

      if (sym == NObjectClass) None
      else if (RuntimePrimitiveTypes.contains(sym)) None
      else if (sym.superClass == NoSymbol || sym.superClass == ObjectClass)
        Some(genTypeName(NObjectClass))
      else
        Some(genTypeName(sym.superClass))
    }

    def genClassAttrs(cd: ClassDef): nir.Attrs = {
      val sym = cd.symbol
      val annotationAttrs = sym.annotations.collect {
        case ann if ann.symbol == ExternClass =>
          nir.Attr.Extern(sym.isBlocking)
        case ann if ann.symbol == LinkClass =>
          val Apply(_, Seq(Literal(Constant(name: String)))) = ann.tree
          nir.Attr.Link(name)
        case ann if ann.symbol == DefineClass =>
          val Apply(_, Seq(Literal(Constant(name: String)))) = ann.tree
          nir.Attr.Define(name)
        case ann if ann.symbol == StubClass =>
          nir.Attr.Stub
      }
      val abstractAttr =
        if (sym.isAbstract) Seq(nir.Attr.Abstract) else Seq.empty

      nir.Attrs.fromSeq(annotationAttrs ++ abstractAttr)
    }

    def genClassInterfaces(sym: Symbol) = {
      val isExtern = sym.isExternType
      def validate(psym: Symbol) = {
        val parentIsExtern = psym.isExternType
        if (isExtern && !parentIsExtern)
          reporter.error(
            sym.pos,
            "Extern object can only extend extern traits"
          )
        if (!isExtern && parentIsExtern)
          reporter.error(
            psym.pos,
            "Extern traits can be only mixed with extern traits or objects"
          )
      }

      for {
        parent <- sym.parentSymbols
        psym = parent.info.typeSymbol if psym.isTraitOrInterface
        _ = validate(psym)
      } yield genTypeName(psym)
    }

    private def getAlignmentAttr(sym: Symbol): Option[nir.Attr.Alignment] =
      sym.getAnnotation(AlignClass).map { annot =>
        val Apply(_, args) = annot.tree
        val groupName: Option[String] =
          args.collectFirst { case Literal(Constant(v: String)) => v }

        def getFixedAlignment() = args
          .take(1)
          .collectFirst { case Literal(Constant(v: Int)) => v }
          .map { value =>
            if (value % 8 != 0 || value <= 0 || value > 8192) {
              reporter.error(
                annot.tree.pos,
                "Alignment must be positive integer literal, multiple of 8, and less then 8192 (inclusive)"
              )
            }
            value
          }
        def linktimeResolvedAlignment = args
          .take(1)
          .collectFirst {
            // explicitly @align(contendedPaddingWidth)
            case LinktimeProperty(
                  "scala.scalanative.meta.linktimeinfo.contendedPaddingWidth",
                  _,
                  _
                ) =>
              nir.Attr.Alignment.linktimeResolved
          }
          .getOrElse(
            // implicitly, @align() or @align(group)
            nir.Attr.Alignment.linktimeResolved
          )

        nir.Attr.Alignment(
          size = getFixedAlignment().getOrElse(linktimeResolvedAlignment),
          group = groupName.filterNot(_.isEmpty)
        )
      }

    def genClassFields(cd: ClassDef): Unit = {
      val sym = cd.symbol
      val attrs = nir.Attrs(isExtern = sym.isExternType)
      val classAlign = getAlignmentAttr(sym)

      for (f <- sym.info.decls
          if !f.isMethod && f.isTerm && !f.isModule) {
        if (f.isExtern && !f.isMutable) {
          reporter.error(f.pos, "`extern` cannot be used in val definition")
        }
        val ty = genType(f.tpe)
        val name = genFieldName(f)
        val pos: nir.Position = f.pos
        // Thats what JVM backend does
        // https://github.com/scala/scala/blob/fe724bcbbfdc4846e5520b9708628d994ae76798/src/compiler/scala/tools/nsc/backend/jvm/BTypesFromSymbols.scala#L760-L764
        val fieldAttrs = attrs.copy(
          isVolatile = f.isVolatile,
          isFinal = !f.isMutable,
          align = getAlignmentAttr(f).orElse(classAlign)
        )

        buf += nir.Defn.Var(fieldAttrs, name, ty, nir.Val.Zero(ty))(pos)
      }
    }

    def withFreshExprBuffer[R](f: ExprBuffer => R): R = {
      scoped(
        curFresh := nir.Fresh(),
        curScopeId := nir.ScopeId.TopLevel
      ) {
        val exprBuffer = new ExprBuffer()(curFresh)
        f(exprBuffer)
      }
    }

    def genReflectiveInstantiation(cd: ClassDef): Unit = {
      val sym = cd.symbol
      val enableReflectiveInstantiation = {
        (sym :: sym.ancestors).exists { ancestor =>
          ancestor.hasAnnotation(EnableReflectiveInstantiationAnnotation)
        }
      }

      if (enableReflectiveInstantiation) {
        scoped(
          curClassSym := cd.symbol,
          curFresh := nir.Fresh(),
          curUnwindHandler := None,
          curScopeId := nir.ScopeId.TopLevel
        ) {
          genRegisterReflectiveInstantiation(cd)
        }
      }
    }

    def genRegisterReflectiveInstantiation(cd: ClassDef): Unit = {
      val owner = genTypeName(curClassSym)
      val name = owner.member(nir.Sig.Clinit)

      val staticInitBody =
        if (isStaticModule(curClassSym))
          Some(genRegisterReflectiveInstantiationForModuleClass(cd))
        else if (curClassSym.isModuleClass)
          None // see: https://github.com/scala-js/scala-js/issues/3228
        else if (curClassSym.isLifted && !curClassSym.originalOwner.isClass)
          None // see: https://github.com/scala-js/scala-js/issues/3227
        else
          Some(genRegisterReflectiveInstantiationForNormalClass(cd))

      staticInitBody.foreach {
        case body if body.nonEmpty =>
          buf += new nir.Defn.Define(
            nir.Attrs(),
            name,
            nir.Type.Function(Seq.empty[nir.Type], nir.Type.Unit),
            body
          )(cd.pos)
        case _ => ()
      }
    }

    // Generate the constructor for the class instantiator class,
    // which is expected to extend one of scala.runtime.AbstractFunctionX.
    private def genReflectiveInstantiationConstructor(
        reflInstBuffer: ReflectiveInstantiationBuffer,
        superClass: nir.Global.Top
    )(implicit pos: nir.Position): Unit = {
      withFreshExprBuffer { exprBuf =>
        val body = {
          // first argument is this
          val thisArg =
            nir.Val.Local(curFresh(), nir.Type.Ref(reflInstBuffer.name))
          exprBuf.label(curFresh(), Seq(thisArg))

          // call to super constructor
          exprBuf.call(
            nir.Type.Function(Seq(nir.Type.Ref(superClass)), nir.Type.Unit),
            nir.Val
              .Global(superClass.member(nir.Sig.Ctor(Seq.empty)), nir.Type.Ptr),
            Seq(thisArg),
            unwind(curFresh)
          )

          exprBuf.ret(nir.Val.Unit)
          exprBuf.toSeq
        }

        reflInstBuffer += new nir.Defn.Define(
          nir.Attrs(),
          reflInstBuffer.name.member(nir.Sig.Ctor(Seq.empty)),
          nir.Type
            .Function(Seq(nir.Type.Ref(reflInstBuffer.name)), nir.Type.Unit),
          body
        )
      }
    }

    // Allocate and construct an object, using the provided ExprBuffer.
    private def allocAndConstruct(
        exprBuf: ExprBuffer,
        name: nir.Global.Top,
        argTypes: Seq[nir.Type],
        args: Seq[nir.Val]
    )(implicit pos: nir.Position): nir.Val = {

      val alloc = exprBuf.classalloc(name, unwind(curFresh))
      exprBuf.call(
        nir.Type.Function(nir.Type.Ref(name) +: argTypes, nir.Type.Unit),
        nir.Val.Global(name.member(nir.Sig.Ctor(argTypes)), nir.Type.Ptr),
        alloc +: args,
        unwind(curFresh)
      )
      alloc
    }

    def genRegisterReflectiveInstantiationForModuleClass(
        cd: ClassDef
    ): Seq[nir.Inst] = {
      import NirGenSymbols._

      val fqSymId = curClassSym.fullName + "$"
      val fqSymName = nir.Global.Top(fqSymId)

      implicit val pos: nir.Position = cd.pos

      reflectiveInstantiationInfo += ReflectiveInstantiationBuffer(fqSymId)
      val reflInstBuffer = reflectiveInstantiationInfo.last

      def genModuleLoaderAnonFun(exprBuf: ExprBuffer): nir.Val = {
        val applyMethodSig =
          nir.Sig.Method("apply", Seq(jlObjectRef))

        // Generate the module loader class. The generated class extends
        // AbstractFunction0[Any], i.e. has an apply method, which loads the module.
        // We need a fresh ExprBuffer for this, since it is different scope.
        withFreshExprBuffer { exprBuf =>
          val body = {
            // first argument is this
            val thisArg =
              nir.Val.Local(curFresh(), nir.Type.Ref(reflInstBuffer.name))
            exprBuf.label(curFresh(), Seq(thisArg))

            val m = exprBuf.module(fqSymName, unwind(curFresh))
            exprBuf.ret(m)
            exprBuf.toSeq
          }

          reflInstBuffer += new nir.Defn.Define(
            nir.Attrs(),
            reflInstBuffer.name.member(applyMethodSig),
            nir.Type
              .Function(Seq(nir.Type.Ref(reflInstBuffer.name)), jlObjectRef),
            body
          )
        }

        // Generate the module loader class constructor.
        genReflectiveInstantiationConstructor(
          reflInstBuffer,
          srAbstractFunction0
        )

        reflInstBuffer += nir.Defn.Class(
          nir.Attrs(),
          reflInstBuffer.name,
          Some(srAbstractFunction0),
          Seq(serializable)
        )

        // Allocate and return an instance of the generated class.
        allocAndConstruct(exprBuf, reflInstBuffer.name, Seq.empty, Seq.empty)
      }

      withFreshExprBuffer { exprBuf =>
        exprBuf.label(curFresh(), Seq.empty)

        val fqcnArg = nir.Val.String(fqSymId)
        val runtimeClassArg = nir.Val.ClassOf(fqSymName)
        val loadModuleFunArg = genModuleLoaderAnonFun(exprBuf)

        exprBuf.genApplyModuleMethod(
          ReflectModule,
          Reflect_registerLoadableModuleClass,
          Seq(fqcnArg, runtimeClassArg, loadModuleFunArg).map(ValTree(_))
        )

        exprBuf.ret(nir.Val.Unit)
        exprBuf.toSeq
      }
    }

    def genRegisterReflectiveInstantiationForNormalClass(
        cd: ClassDef
    ): Seq[nir.Inst] = {
      import NirGenSymbols._

      val fqSymId = curClassSym.fullName
      val fqSymName = nir.Global.Top(fqSymId)

      // Create a new Tuple2 and initialise it with the provided values.
      def createTuple2(exprBuf: ExprBuffer, _1: nir.Val, _2: nir.Val)(implicit
          pos: nir.Position
      ): nir.Val = {
        allocAndConstruct(
          exprBuf,
          tuple2,
          Seq(jlObjectRef, jlObjectRef),
          Seq(_1, _2)
        )
      }

      def genClassConstructorsInfo(
          exprBuf: ExprBuffer,
          ctors: Seq[global.Symbol]
      )(implicit pos: nir.Position): nir.Val = {
        val applyMethodSig =
          nir.Sig.Method("apply", Seq(jlObjectRef, jlObjectRef))

        // Constructors info is an array of Tuple2 (tpes, inst), where:
        // - tpes is an array with the runtime classes of the constructor arguments.
        // - inst is a function, which accepts an array with tpes and returns a new
        //   instance of the class.
        val ctorsInfo = exprBuf.arrayalloc(
          nir.Type.Array(tuple2Ref),
          nir.Val.Int(ctors.length),
          unwind(curFresh)
        )

        // For each (public) constructor C, generate a lambda responsible for
        // initialising and returning an instance of the class, using C.
        for ((ctor, ctorIdx) <- ctors.zipWithIndex) {
          val ctorSig = genMethodSig(ctor)
          val ctorArgsSig = ctorSig.args.map(_.mangle).mkString
          implicit val pos: nir.Position = ctor.pos

          reflectiveInstantiationInfo += ReflectiveInstantiationBuffer(
            fqSymId + ctorArgsSig
          )
          val reflInstBuffer = reflectiveInstantiationInfo.last

          // Lambda generation consists of generating a class which extends
          // scala.runtime.AbstractFunction1, with an apply method that accepts
          // the list of arguments, instantiates an instance of the class by
          // forwarding the arguments to C, and returns the instance.
          withFreshExprBuffer { exprBuf =>
            val body = {
              // first argument is this
              val thisArg =
                nir.Val.Local(curFresh(), nir.Type.Ref(reflInstBuffer.name))
              // second argument is parameters sequence
              val argsArg =
                nir.Val.Local(curFresh(), nir.Type.Array(jlObjectRef))
              exprBuf.label(curFresh(), Seq(thisArg, argsArg))

              // Extract and cast arguments to proper types.
              val argsVals =
                (for ((arg, argIdx) <- ctorSig.args.tail.zipWithIndex) yield {
                  val elem =
                    exprBuf.arrayload(
                      jlObjectRef,
                      argsArg,
                      nir.Val.Int(argIdx),
                      unwind(curFresh)
                    )
                  // If the expected argument type can be boxed (i.e. is a primitive
                  // type), then we need to unbox it before passing it to C.
                  nir.Type.box.get(arg) match {
                    case Some(bt) =>
                      exprBuf.unbox(bt, elem, unwind(curFresh))
                    case None =>
                      exprBuf.as(arg, elem, unwind(curFresh))
                  }
                })

              // Allocate a new instance and call C.
              val alloc = allocAndConstruct(
                exprBuf,
                fqSymName,
                ctorSig.args.tail,
                argsVals
              )

              exprBuf.ret(alloc)
              exprBuf.toSeq
            }

            reflInstBuffer += new nir.Defn.Define(
              nir.Attrs(),
              reflInstBuffer.name.member(applyMethodSig),
              nir.Type.Function(
                Seq(
                  nir.Type.Ref(reflInstBuffer.name),
                  nir.Type.Array(jlObjectRef)
                ),
                jlObjectRef
              ),
              body
            )
          }

          // Generate the class instantiator constructor.
          genReflectiveInstantiationConstructor(
            reflInstBuffer,
            srAbstractFunction1
          )

          reflInstBuffer += nir.Defn.Class(
            nir.Attrs(),
            reflInstBuffer.name,
            Some(srAbstractFunction1),
            Seq(serializable)
          )

          // Allocate an instance of the generated class.
          val instantiator =
            allocAndConstruct(
              exprBuf,
              reflInstBuffer.name,
              Seq.empty,
              Seq.empty
            )

          // Create the current constructor's info. We need:
          // - an array with the runtime classes of the ctor parameters.
          // - the instantiator function created above (instantiator).
          val rtClasses = exprBuf.arrayalloc(
            jlClassRef,
            nir.Val.Int(ctorSig.args.tail.length),
            unwind(curFresh)
          )
          for ((arg, argIdx) <- ctorSig.args.tail.zipWithIndex) {
            // Store the runtime class in the array.
            exprBuf.arraystore(
              jlClassRef,
              rtClasses,
              nir.Val.Int(argIdx),
              nir.Val.ClassOf(nir.Type.typeToName(arg)),
              unwind(curFresh)
            )
          }

          // Allocate a tuple to store the current constructor's info
          val to = createTuple2(exprBuf, rtClasses, instantiator)

          exprBuf.arraystore(
            tuple2Ref,
            ctorsInfo,
            nir.Val.Int(ctorIdx),
            to,
            unwind(curFresh)
          )
        }

        ctorsInfo
      }

      // Collect public constructors.
      val ctors =
        if (curClassSym.isAbstractClass) Nil
        else
          curClassSym.info
            .member(nme.CONSTRUCTOR)
            .alternatives
            .filter(_.isPublic)

      implicit val pos: nir.Position = cd.pos
      if (ctors.isEmpty)
        Seq.empty
      else
        withFreshExprBuffer { exprBuf =>
          exprBuf.label(curFresh(), Seq.empty)

          val fqcnArg = nir.Val.String(fqSymId)
          val runtimeClassArg = nir.Val.ClassOf(fqSymName)

          val instantiateClassFunArg =
            genClassConstructorsInfo(exprBuf, ctors)

          exprBuf.genApplyModuleMethod(
            ReflectModule,
            Reflect_registerInstantiatableClass,
            Seq(fqcnArg, runtimeClassArg, instantiateClassFunArg).map(
              ValTree(_)
            )
          )

          exprBuf.ret(nir.Val.Unit)
          exprBuf.toSeq
        }
    }

    def genMethods(cd: ClassDef): Unit = {
      val methods = cd.impl.body.flatMap {
        case dd: DefDef => genMethod(dd)
        case _          => Nil
      }
      buf ++= methods
      buf ++= genStaticMethodForwarders(cd, methods)
      buf ++= genTopLevelExports(cd)
    }

    def genMethod(dd: DefDef): Option[nir.Defn] = {
      val fresh = nir.Fresh()
      val env = new MethodEnv(fresh)

      implicit val pos: nir.Position = dd.pos
      val scopes = mutable.Set.empty[DebugInfo.LexicalScope]
      scopes += DebugInfo.LexicalScope.TopLevel(dd.rhs.pos)

      scoped(
        curMethodSym := dd.symbol,
        curMethodEnv := env,
        curMethodInfo := (new CollectMethodInfo).collect(dd.rhs),
        curFresh := fresh,
        curUnwindHandler := None,
        curMethodLocalNames := localNamesBuilder(),
        curFreshScope := initFreshScope(dd.rhs),
        curScopeId := nir.ScopeId.TopLevel,
        curScopes := scopes
      ) {
        val sym = dd.symbol
        val owner = curClassSym.get
        // implicit class is erased to method at this point
        val isExtern = sym.isExtern
        val attrs = genMethodAttrs(sym, isExtern)
        val name = genMethodName(sym)
        val sig = genMethodSig(sym)

        dd.rhs match {
          case EmptyTree =>
            Some(
              nir.Defn.Declare(
                attrs,
                name,
                if (attrs.isExtern) genExternMethodSig(sym) else sig
              )
            )

          case _ if dd.symbol.isConstructor && isExtern =>
            validateExternCtor(dd.rhs)
            None

          case _ if dd.name == nme.CONSTRUCTOR && owner.isStruct =>
            None

          case rhs if isExtern =>
            checkExplicitReturnTypeAnnotation(dd, "extern method")
            genExternMethod(attrs, name, sig, dd)

          case _ if sym.hasAnnotation(ResolvedAtLinktimeClass) =>
            genLinktimeResolved(dd, name)

          case rhs =>
            scoped(
              curMethodSig := sig
            ) {
              curMethodUsesLinktimeResolvedValues = false
              val body = genMethodBody(dd, rhs, isExtern)
              val methodAttrs =
                if (curMethodUsesLinktimeResolvedValues)
                  attrs.copy(isLinktimeResolved = true)
                else attrs
              Some(
                new nir.Defn.Define(
                  methodAttrs,
                  name,
                  sig,
                  insts = body,
                  debugInfo = nir.Defn.Define.DebugInfo(
                    localNames = curMethodLocalNames.get.toMap,
                    lexicalScopes = scopes.toList
                  )
                )
              )
            }
        }
      }
    }

    protected def genLinktimeResolved(dd: DefDef, name: nir.Global.Member)(
        implicit pos: nir.Position
    ): Option[nir.Defn] = {
      if (dd.symbol.isConstant) {
        globalError(
          dd.pos,
          "Link-time property cannot be constant value, it would be inlined by scalac compiler"
        )
      }
      val retty = genType(dd.tpt.tpe)

      import LinktimeProperty.Type._
      dd match {
        case LinktimeProperty(propertyName, Provided, _) =>
          if (dd.rhs.symbol == ResolvedMethod) Some {
            checkExplicitReturnTypeAnnotation(dd, "value resolved at link-time")
            genLinktimeResolvedMethod(dd, retty, name) {
              _.call(
                nir.Linktime.PropertyResolveFunctionTy(retty),
                nir.Linktime.PropertyResolveFunction(retty),
                nir.Val.String(propertyName) :: Nil,
                nir.Next.None
              )
            }
          }
          else {
            globalError(
              dd.pos,
              s"Link-time resolved property must have ${ResolvedMethod.fullName} as body"
            )
            None
          }

        case LinktimeProperty(_, Calculated, _) =>
          Some {
            genLinktimeResolvedMethod(dd, retty, name) { buf =>
              def resolve(tree: Tree): nir.Val = tree match {
                case Literal(Constant(_)) =>
                  buf.genExpr(tree)
                case If(cond, thenp, elsep) =>
                  buf.genIf(retty, cond, thenp, elsep, ensureLinktime = true)
                case tree: Apply if retty == nir.Type.Bool =>
                  val True = ValTree(nir.Val.True)
                  val False = ValTree(nir.Val.False)
                  buf.genIf(retty, tree, True, False, ensureLinktime = true)
                case Block(stats, expr) =>
                  stats.foreach { v =>
                    globalError(
                      v.pos,
                      "Linktime resolved block can only contain other linktime resolved def defintions"
                    )
                    // unused, generated to prevent compiler plugin crash when referencing ident
                    buf.genExpr(v)
                  }
                  resolve(expr)
              }
              resolve(dd.rhs)
            }
          }

        case _ =>
          globalError(
            dd.pos,
            "Cannot transform to linktime resolved expression"
          )
          None
      }
    }

    private def genLinktimeResolvedMethod(
        dd: DefDef,
        retty: nir.Type,
        methodName: nir.Global.Member
    )(genValue: ExprBuffer => nir.Val)(implicit pos: nir.Position): nir.Defn = {
      implicit val fresh = nir.Fresh()
      val buf = new ExprBuffer()

      scoped(
        curFresh := fresh,
        curMethodSym := dd.symbol,
        curMethodThis := None,
        curMethodEnv := new MethodEnv(fresh),
        curMethodInfo := new CollectMethodInfo,
        curUnwindHandler := None,
        curScopeId := nir.ScopeId.TopLevel
      ) {
        buf.label(fresh())
        val value = genValue(buf)
        buf.ret(value)
      }

      new nir.Defn.Define(
        nir
          .Attrs(inlineHint = nir.Attr.AlwaysInline, isLinktimeResolved = true),
        methodName,
        nir.Type.Function(Seq.empty, retty),
        buf.toSeq
      )
    }

    def genExternMethod(
        attrs: nir.Attrs,
        name: nir.Global.Member,
        origSig: nir.Type,
        dd: DefDef
    ): Option[nir.Defn] = {
      val rhs = dd.rhs
      def externMethodDecl() = {
        val externSig = genExternMethodSig(curMethodSym)
        val externDefn = nir.Defn.Declare(attrs, name, externSig)(rhs.pos)

        Some(externDefn)
      }

      def isCallingExternMethod(sym: Symbol) =
        sym.isExtern

      def isExternMethodAlias(target: Symbol) =
        (name, genName(target)) match {
          case (nir.Global.Member(_, lsig), nir.Global.Member(_, rsig)) =>
            lsig == rsig
          case _ => false
        }
      val defaultArgs = dd.symbol.paramss.flatten.filter(_.hasDefault)
      rhs match {
        case _ if defaultArgs.nonEmpty =>
          reporter.error(
            defaultArgs.head.pos,
            "extern method cannot have default argument"
          )
          None
        case Apply(ref: RefTree, Seq()) if ref.symbol == ExternMethod =>
          externMethodDecl()

        case _ if curMethodSym.hasFlag(ACCESSOR) => None

        case Apply(target, _) if isCallingExternMethod(target.symbol) =>
          val sym = target.symbol
          if (isExternMethodAlias(sym)) externMethodDecl()
          else {
            reporter.error(
              target.pos,
              "Referencing other extern symbols in not supported"
            )
            None
          }

        case _ =>
          reporter.error(
            rhs.pos,
            "methods in extern objects must have extern body"
          )
          None
      }
    }

    def validateExternCtor(rhs: Tree): Unit = {
      val classSym = curClassSym.get
      def isExternCall(tree: Tree): Boolean = tree match {
        case Typed(target, _) => isExternCall(target)
        case Apply(extern, _) => extern.symbol == ExternMethod
        case _                => false
      }

      def isCurClassSetter(sym: Symbol) =
        sym.isSetter && sym.owner.tpe <:< classSym.tpe

      rhs match {
        case Block(Nil, _) => () // empty mixin constructor
        case Block(inits, _) =>
          val externs = collection.mutable.Set.empty[Symbol]
          inits.foreach {
            case Assign(ref: RefTree, rhs) if isExternCall(rhs) =>
              externs += ref.symbol

            case Apply(fun, Seq(arg))
                if isCurClassSetter(fun.symbol) && isExternCall(arg) =>
              externs += fun.symbol

            case Apply(target, _) if target.symbol.isConstructor => ()

            case tree =>
              reporter.error(
                rhs.pos,
                "extern objects may only contain extern fields and methods"
              )
          }
          def isInheritedField(f: Symbol) = {
            def hasFieldGetter(cls: Symbol) = f.getterIn(cls) != NoSymbol
            def inheritedTraits(cls: Symbol) =
              cls.parentSymbols.filter(_.isTraitOrInterface)
            def inheritsField(cls: Symbol): Boolean =
              hasFieldGetter(cls) || inheritedTraits(cls).exists(inheritsField)
            inheritsField(classSym)
          }

          // Exclude fields derived from extern trait
          for (f <- curClassSym.info.decls) {
            if (f.isField && !isInheritedField(f)) {
              if (!(externs.contains(f) || externs.contains(f.setter))) {
                reporter.error(
                  f.pos,
                  "extern objects may only contain extern fields"
                )
              }
            }
          }
      }
    }

    def genMethodAttrs(sym: Symbol, isExtern: Boolean): nir.Attrs = {
      val inlineAttrs =
        if (sym.isBridge || sym.hasFlag(ACCESSOR)) Seq(nir.Attr.AlwaysInline)
        else Nil

      val annotatedAttrs =
        sym.annotations.map(_.symbol).collect {
          case NoInlineClass     => nir.Attr.NoInline
          case AlwaysInlineClass => nir.Attr.AlwaysInline
          case InlineClass       => nir.Attr.InlineHint
          case StubClass         => nir.Attr.Stub
          case NoOptimizeClass   => nir.Attr.NoOpt
          case NoSpecializeClass => nir.Attr.NoSpecialize
        }
      val externAttrs =
        if (isExtern)
          Seq(nir.Attr.Extern(sym.isBlocking || sym.owner.isBlocking))
        else Nil

      nir.Attrs.fromSeq(inlineAttrs ++ annotatedAttrs ++ externAttrs)
    }

    def genMethodBody(
        dd: DefDef,
        bodyp: Tree,
        isExtern: Boolean
    ): Seq[nir.Inst] = {
      val fresh = curFresh.get
      val buf = new ExprBuffer()(fresh)
      val isSynchronized = dd.symbol.hasFlag(SYNCHRONIZED)
      val sym = dd.symbol
      val isStatic = sym.isStaticInNIR

      implicit val pos: nir.Position = bodyp.pos

      val paramSyms = genParamSyms(dd, isStatic)
      val params = paramSyms.map {
        case None =>
          val ty = genType(curClassSym.tpe)
          nir.Val.Local(namedId(fresh)("this"), ty)
        case Some(sym) =>
          val ty = genType(sym.tpe)
          val name = genLocalName(sym)
          val param = nir.Val.Local(namedId(fresh)(name), ty)
          curMethodEnv.enter(sym, param)
          param
      }

      def genEntry(): Unit = {
        buf.label(fresh(), params)
      }

      def genVars(): Unit = {
        val vars = curMethodInfo.mutableVars.toSeq
        vars.foreach { sym =>
          val ty = genType(sym.info)
          val name = genLocalName(sym)
          val slot =
            buf.let(namedId(fresh)(name), nir.Op.Var(ty), unwind(fresh))
          curMethodEnv.enter(sym, slot)
        }
      }

      def withOptSynchronized(bodyGen: ExprBuffer => nir.Val): nir.Val = {
        if (!isSynchronized) bodyGen(buf)
        else {
          val syncedIn = curMethodThis.getOrElse {
            unsupported(
              s"cannot generate `synchronized` for method ${curMethodSym.name}, curMethodThis was empty"
            )
          }
          buf.genSynchronized(ValTree(syncedIn))(bodyGen)
        }
      }

      def genBody(): nir.Val = bodyp match {
        // Tailrec emits magical labeldefs that can hijack this reference is
        // current method. This requires special treatment on our side.
        case Block(
              List(ValDef(_, nme.THIS, _, _)),
              label @ LabelDef(name, Ident(nme.THIS) :: _, rhs)
            ) =>
          val local = curMethodEnv.enterLabel(label)
          val values = params.take(label.params.length)

          buf.jump(local, values)(label.pos)
          scoped(
            curMethodThis := {
              if (isStatic) None
              else Some(params.head)
            },
            curMethodIsExtern := isExtern
          ) {
            buf.genReturn {
              withOptSynchronized(_.genTailRecLabel(dd, isStatic, label))
            }
          }

        case _ if curMethodSym.get == NObjectInitMethod =>
          scoped(
            curMethodIsExtern := isExtern
          ) {
            buf.genReturn(nir.Val.Unit)
          }

        case _ =>
          scoped(
            curMethodThis := {
              if (isStatic) None
              else Some(params.head)
            },
            curMethodIsExtern := isExtern
          ) {
            buf.genReturn {
              withOptSynchronized(_.genExpr(bodyp))
            }
          }
      }

      genEntry()
      genVars()
      genBody()
      nir.ControlFlow.removeDeadBlocks(buf.toSeq)
    }
  }

  // Static forwarders -------------------------------------------------------

  // Ported from Scala.js
  /* It is important that we always emit forwarders, because some Java APIs
   * actually have a public static method and a public instance method with
   * the same name. For example the class `Integer` has a
   * `def hashCode(): Int` and a `static def hashCode(Int): Int`. The JVM
   * back-end considers them as colliding because they have the same name,
   * but we must not.
   *
   * By default, we only emit forwarders for top-level objects, like the JVM
   * back-end. However, if requested via a compiler option, we enable them
   * for all static objects. This is important so we can implement static
   * methods of nested static classes of JDK APIs (see scala-js/#3950).
   */

  /** Is the given Scala class, interface or module class a candidate for static
   *  forwarders?
   *
   *    - the flag `-XnoForwarders` is not set to true, and
   *    - the symbol is static, and
   *    - either of both of the following is true:
   *      - the plugin setting `GenStaticForwardersForNonTopLevelObjects` is set
   *        to true, or
   *      - the symbol was originally at the package level
   *
   *  Other than the the fact that we also consider interfaces, this performs
   *  the same tests as the JVM back-end.
   */
  private def isCandidateForForwarders(sym: Symbol): Boolean = {
    !settings.noForwarders.value && sym.isStatic && {
      // Reject non-top-level objects unless opted in via the appropriate option
      scalaNativeOpts.genStaticForwardersForNonTopLevelObjects ||
      !sym.name.containsChar('$') // this is the same test that scalac performs
    }
  }

  /** Gen the static forwarders to the members of a class or interface for
   *  methods of its companion object.
   *
   *  This is only done if there exists a companion object and it is not a JS
   *  type.
   *
   *  Precondition: `isCandidateForForwarders(sym)` is true
   */
  private def genStaticForwardersForClassOrInterface(
      existingMembers: Seq[nir.Defn],
      sym: Symbol
  ): Seq[nir.Defn.Define] = {
    /*  Phase travel is necessary for non-top-level classes, because flatten
     *  breaks their companionModule. This is tracked upstream at
     *  https://github.com/scala/scala-dev/issues/403
     */
    val module = exitingPhase(currentRun.picklerPhase)(sym.companionModule)
    if (module == NoSymbol) Nil
    else {
      val moduleClass = module.moduleClass
      if (moduleClass.isExternType) Nil
      else genStaticForwardersFromModuleClass(existingMembers, moduleClass)
    }
  }

  /** Gen the static forwarders for the methods of a module class.
   *
   *  Precondition: `isCandidateForForwarders(moduleClass)` is true
   */
  private def genStaticForwardersFromModuleClass(
      existingMembers: Seq[nir.Defn],
      moduleClass: Symbol
  ): Seq[nir.Defn.Define] = {
    assert(moduleClass.isModuleClass, moduleClass)

    lazy val existingStaticMethodNames = existingMembers.collect {
      case nir.Defn.Define(_, name @ nir.Global.Member(_, sig), _, _, _)
          if sig.isStatic =>
        name
    }

    def listMembersBasedOnFlags = {
      import scala.tools.nsc.symtab.Flags._
      // Copy-pasted from BCodeHelpers
      val ExcludedForwarderFlags: Long = {
        SPECIALIZED | LIFTED | PROTECTED | STATIC | EXPANDEDNAME | PRIVATE | MACRO
      }

      moduleClass.info.membersBasedOnFlags(
        ExcludedForwarderFlags,
        METHOD
      )
    }

    /* See BCodeHelprs.addForwarders in 2.12+ for why we normally use
     * exitingUncurry.
     */
    val members = exitingUncurry(listMembersBasedOnFlags)

    def isExcluded(m: Symbol): Boolean = {
      def isOfJLObject: Boolean = {
        val o = m.owner
        (o eq ObjectClass) || (o eq AnyRefClass) || (o eq AnyClass)
      }

      m.isDeferred || m.isConstructor || m.hasAccessBoundary ||
        m.isExtern ||
        isOfJLObject
    }

    val forwarders = for {
      sym <- members
      if !isExcluded(sym)
    } yield {
      implicit val pos: nir.Position = sym.pos

      val methodName = genMethodName(sym)
      val forwarderName = genStaticMemberName(sym, moduleClass)
      val nir.Type.Function(_ +: paramTypes, retType) = genMethodSig(sym)
      val forwarderParamTypes = paramTypes
      val forwarderType = nir.Type.Function(forwarderParamTypes, retType)

      if (existingStaticMethodNames.contains(forwarderName)) {
        reporter.error(
          curClassSym.get.pos,
          "Unexpected situation: found existing public static method " +
            s"${sym} in the companion class of " +
            s"${moduleClass.fullName}; cannot generate a static forwarder " +
            "the method of the same name in the object." +
            "Please report this as a bug in the Scala Native support."
        )
      }

      new nir.Defn.Define(
        attrs = nir.Attrs(inlineHint = nir.Attr.InlineHint),
        name = forwarderName,
        ty = forwarderType,
        insts = curStatBuffer
          .withFreshExprBuffer { buf =>
            val fresh = curFresh.get
            scoped(
              curUnwindHandler := None,
              curMethodThis := None,
              curScopeId := nir.ScopeId.TopLevel
            ) {
              val entryParams =
                forwarderParamTypes.map(nir.Val.Local(fresh(), _))
              buf.label(fresh(), entryParams)
              val res =
                buf.genApplyModuleMethod(
                  moduleClass,
                  sym,
                  entryParams.map(ValTree(_))
                )
              buf.ret(res)
            }
            buf.toSeq
          }
      )
    }

    forwarders.toList
  }

  private def genStaticMethodForwarders(
      td: ClassDef,
      existingMethods: Seq[nir.Defn]
  ): Seq[nir.Defn] = {
    val sym = td.symbol
    if (!isCandidateForForwarders(sym)) Nil
    else if (sym.isModuleClass) Nil
    else genStaticForwardersForClassOrInterface(existingMethods, sym)
  }

  /** Create a mirror class for top level module that has no defined companion
   *  class. A mirror class is a class containing only static methods that
   *  forward to the corresponding method on the MODULE instance of the given
   *  Scala object. It will only be generated if there is no companion class: if
   *  there is, an attempt will instead be made to add the forwarder methods to
   *  the companion class.
   */
  private def genMirrorClass(cd: ClassDef) = {
    val sym = cd.symbol
    // phase travel to pickler required for isNestedClass (looks at owner)
    val isTopLevelModuleClass = exitingPickler {
      sym.isModuleClass && !sym.isNestedClass
    }
    if (isTopLevelModuleClass && sym.companionClass == NoSymbol) {
      val classDefn = nir.Defn.Class(
        attrs = nir.Attrs.None,
        name = nir.Global.Top(genTypeName(sym).id.stripSuffix("$")),
        parent = Some(nir.Rt.Object.name),
        traits = Nil
      )(cd.pos)
      generatedMirrorClasses += sym -> MirrorClass(
        classDefn,
        genStaticForwardersFromModuleClass(Nil, sym)
      )
    }
  }

  private def checkExplicitReturnTypeAnnotation(
      externMethodDd: DefDef,
      methodKind: String
  ): Unit = {
    externMethodDd.tpt match {
      case resultTypeTree: global.TypeTree if resultTypeTree.wasEmpty =>
        global.reporter.error(
          externMethodDd.pos,
          s"$methodKind ${externMethodDd.name} needs result type"
        )
      case _ => ()
    }
  }

  protected object LinktimeProperty {
    sealed trait Type
    object Type {
      case object Provided extends Type
      case object Calculated extends Type
    }
    def unapply(tree: Tree): Option[(String, Type, nir.Position)] = {
      if (tree.symbol == null) None
      else
        tree.symbol
          .getAnnotation(ResolvedAtLinktimeClass)
          .flatMap(_.args match {
            case Literal(Constant(name: String)) :: Nil =>
              Some(name, Type.Provided, tree.pos)
            case _ :: Nil =>
              globalError(
                tree.symbol.pos,
                s"Name used to resolve link-time property needs to be non-null literal constant"
              )
              None
            case Nil =>
              val syntheticName = genName(tree.symbol).mangle
              Some(syntheticName, Type.Calculated, tree.pos)
          })
    }
  }
}
