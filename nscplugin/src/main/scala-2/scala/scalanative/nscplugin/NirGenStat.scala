package scala.scalanative
package nscplugin

import scala.collection.mutable
import scala.reflect.internal.Flags._
import scala.scalanative.nir._
import scala.tools.nsc.Properties
import scala.scalanative.util.unsupported
import scala.scalanative.util.ScopedVar.scoped
import scala.tools.nsc
import scalanative.nir.ControlFlow.removeDeadBlocks

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

  protected val isScala211 = Properties.versionNumberString.startsWith("2.11")

  def isStaticModule(sym: Symbol): Boolean =
    sym.isModuleClass && !isImplClass(sym) && !sym.isLifted

  class MethodEnv(val fresh: Fresh) {
    private val env = mutable.Map.empty[Symbol, Val]

    def enter(sym: Symbol, value: Val): Unit = {
      env += ((sym, value))
    }

    def enterLabel(ld: LabelDef): Local = {
      val local = fresh()
      enter(ld.symbol, Val.Local(local, Type.Ptr))
      local
    }

    def resolve(sym: Symbol): Val = {
      env(sym)
    }

    def resolveLabel(ld: LabelDef): Local = {
      val Val.Local(n, Type.Ptr) = resolve(ld.symbol)
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
      scoped(
        curClassSym := cd.symbol,
        curClassFresh := nir.Fresh()
      ) {
        if (cd.symbol.isStruct) genStruct(cd)
        else genNormalClass(cd)
      }
    }

    def genStruct(cd: ClassDef): Unit = {
      val sym = cd.symbol
      val attrs = genStructAttrs(sym)
      val name = genTypeName(sym)
      val fields = genStructFields(sym)
      val body = cd.impl.body

      buf += Defn.Class(attrs, name, None, Seq.empty)(cd.pos)
      genMethods(cd)
    }

    def genStructAttrs(sym: Symbol): Attrs = Attrs.None

    def genNormalClass(cd: ClassDef): Unit = {
      val sym = cd.symbol
      def attrs = genClassAttrs(cd)
      def name = genTypeName(sym)
      def parent = genClassParent(sym)
      def traits = genClassInterfaces(sym)

      implicit val pos: nir.Position = cd.pos
      genReflectiveInstantiation(cd)
      genClassFields(cd)
      genMethods(cd)
      genMirrorClass(cd)

      buf += {
        if (sym.isScalaModule) {
          Defn.Module(attrs, name, parent, traits)
        } else if (sym.isTraitOrInterface) {
          Defn.Trait(attrs, name, traits)
        } else {
          Defn.Class(attrs, name, parent, traits)
        }
      }
    }

    def genClassParent(sym: Symbol): Option[nir.Global] =
      if (sym == NObjectClass) {
        None
      } else if (sym.superClass == NoSymbol || sym.superClass == ObjectClass) {
        Some(genTypeName(NObjectClass))
      } else {
        Some(genTypeName(sym.superClass))
      }

    def genClassAttrs(cd: ClassDef): Attrs = {
      val sym = cd.symbol
      val annotationAttrs = sym.annotations.collect {
        case ann if ann.symbol == ExternClass =>
          Attr.Extern
        case ann if ann.symbol == LinkClass =>
          val Apply(_, Seq(Literal(Constant(name: String)))) = ann.tree
          Attr.Link(name)
        case ann if ann.symbol == StubClass =>
          Attr.Stub
      }
      val abstractAttr =
        if (sym.isAbstract) Seq(Attr.Abstract) else Seq()

      Attrs.fromSeq(annotationAttrs ++ abstractAttr)
    }

    def genClassInterfaces(sym: Symbol) =
      for {
        parent <- sym.info.parents
        psym = parent.typeSymbol if psym.isTraitOrInterface
      } yield {
        genTypeName(psym)
      }

    def genClassFields(cd: ClassDef): Unit = {
      val sym = cd.symbol
      val attrs = nir.Attrs(isExtern = sym.isExternModule)

      for (f <- sym.info.decls
          if !f.isMethod && f.isTerm && !f.isModule) {
        val ty = genType(f.tpe)
        val name = genFieldName(f)
        val pos: nir.Position = f.pos

        buf += Defn.Var(attrs, name, ty, Val.Zero(ty))(pos)
      }
    }

    def withFreshExprBuffer[R](f: ExprBuffer => R): R = {
      scoped(
        curFresh := Fresh()
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
          curFresh := Fresh(),
          curUnwindHandler := None
        ) {
          genRegisterReflectiveInstantiation(cd)
        }
      }
    }

    def genRegisterReflectiveInstantiation(cd: ClassDef): Unit = {
      val owner = genTypeName(curClassSym)
      val name = owner.member(nir.Sig.Clinit())

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
          buf += Defn.Define(
            Attrs(),
            name,
            nir.Type.Function(Seq.empty[nir.Type], Type.Unit),
            body
          )(cd.pos)
        case _ => ()
      }
    }

    // Generate the constructor for the class instantiator class,
    // which is expected to extend one of scala.runtime.AbstractFunctionX.
    private def genReflectiveInstantiationConstructor(
        reflInstBuffer: ReflectiveInstantiationBuffer,
        superClass: Global
    )(implicit pos: nir.Position): Unit = {
      withFreshExprBuffer { exprBuf =>
        val body = {
          // first argument is this
          val thisArg = Val.Local(curFresh(), Type.Ref(reflInstBuffer.name))
          exprBuf.label(curFresh(), Seq(thisArg))

          // call to super constructor
          exprBuf.call(
            Type.Function(Seq(Type.Ref(superClass)), Type.Unit),
            Val.Global(superClass.member(Sig.Ctor(Seq())), Type.Ptr),
            Seq(thisArg),
            unwind(curFresh)
          )

          exprBuf.ret(Val.Unit)
          exprBuf.toSeq
        }

        reflInstBuffer += Defn.Define(
          Attrs(),
          reflInstBuffer.name.member(Sig.Ctor(Seq())),
          nir.Type.Function(Seq(Type.Ref(reflInstBuffer.name)), Type.Unit),
          body
        )
      }
    }

    // Allocate and construct an object, using the provided ExprBuffer.
    private def allocAndConstruct(
        exprBuf: ExprBuffer,
        name: Global,
        argTypes: Seq[nir.Type],
        args: Seq[Val]
    )(implicit pos: nir.Position): Val = {

      val alloc = exprBuf.classalloc(name, unwind(curFresh))
      exprBuf.call(
        Type.Function(Type.Ref(name) +: argTypes, Type.Unit),
        Val.Global(name.member(Sig.Ctor(argTypes)), Type.Ptr),
        alloc +: args,
        unwind(curFresh)
      )
      alloc
    }

    def genRegisterReflectiveInstantiationForModuleClass(
        cd: ClassDef
    ): Seq[Inst] = {
      import NirGenSymbols._

      val fqSymId = curClassSym.fullName + "$"
      val fqSymName = Global.Top(fqSymId)

      implicit val pos: nir.Position = cd.pos

      reflectiveInstantiationInfo += ReflectiveInstantiationBuffer(fqSymId)
      val reflInstBuffer = reflectiveInstantiationInfo.last

      def genModuleLoaderAnonFun(exprBuf: ExprBuffer): Val = {
        val applyMethodSig =
          Sig.Method("apply", Seq(jlObjectRef))

        // Generate the module loader class. The generated class extends
        // AbstractFunction0[Any], i.e. has an apply method, which loads the module.
        // We need a fresh ExprBuffer for this, since it is different scope.
        withFreshExprBuffer { exprBuf =>
          val body = {
            // first argument is this
            val thisArg = Val.Local(curFresh(), Type.Ref(reflInstBuffer.name))
            exprBuf.label(curFresh(), Seq(thisArg))

            val m = exprBuf.module(fqSymName, unwind(curFresh))
            exprBuf.ret(m)
            exprBuf.toSeq
          }

          reflInstBuffer += Defn.Define(
            Attrs(),
            reflInstBuffer.name.member(applyMethodSig),
            nir.Type.Function(Seq(Type.Ref(reflInstBuffer.name)), jlObjectRef),
            body
          )
        }

        // Generate the module loader class constructor.
        genReflectiveInstantiationConstructor(
          reflInstBuffer,
          srAbstractFunction0
        )

        reflInstBuffer += Defn.Class(
          Attrs(),
          reflInstBuffer.name,
          Some(srAbstractFunction0),
          Seq(serializable)
        )

        // Allocate and return an instance of the generated class.
        allocAndConstruct(exprBuf, reflInstBuffer.name, Seq(), Seq())
      }

      withFreshExprBuffer { exprBuf =>
        exprBuf.label(curFresh(), Seq())

        val fqcnArg = Val.String(fqSymId)
        val runtimeClassArg = Val.ClassOf(fqSymName)
        val loadModuleFunArg = genModuleLoaderAnonFun(exprBuf)

        exprBuf.genApplyModuleMethod(
          ReflectModule,
          Reflect_registerLoadableModuleClass,
          Seq(fqcnArg, runtimeClassArg, loadModuleFunArg).map(ValTree(_))
        )

        exprBuf.ret(Val.Unit)
        exprBuf.toSeq
      }
    }

    def genRegisterReflectiveInstantiationForNormalClass(
        cd: ClassDef
    ): Seq[Inst] = {
      import NirGenSymbols._

      val fqSymId = curClassSym.fullName
      val fqSymName = Global.Top(fqSymId)

      // Create a new Tuple2 and initialise it with the provided values.
      def createTuple2(exprBuf: ExprBuffer, _1: Val, _2: Val)(implicit
          pos: nir.Position
      ): Val = {
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
      )(implicit pos: nir.Position): Val = {
        val applyMethodSig =
          Sig.Method("apply", Seq(jlObjectRef, jlObjectRef))

        // Constructors info is an array of Tuple2 (tpes, inst), where:
        // - tpes is an array with the runtime classes of the constructor arguments.
        // - inst is a function, which accepts an array with tpes and returns a new
        //   instance of the class.
        val ctorsInfo = exprBuf.arrayalloc(
          Type.Array(tuple2Ref),
          Val.Int(ctors.length),
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
              val thisArg = Val.Local(curFresh(), Type.Ref(reflInstBuffer.name))
              // second argument is parameters sequence
              val argsArg = Val.Local(curFresh(), Type.Array(jlObjectRef))
              exprBuf.label(curFresh(), Seq(thisArg, argsArg))

              // Extract and cast arguments to proper types.
              val argsVals =
                (for ((arg, argIdx) <- ctorSig.args.tail.zipWithIndex) yield {
                  val elem =
                    exprBuf.arrayload(
                      jlObjectRef,
                      argsArg,
                      Val.Int(argIdx),
                      unwind(curFresh)
                    )
                  // If the expected argument type can be boxed (i.e. is a primitive
                  // type), then we need to unbox it before passing it to C.
                  Type.box.get(arg) match {
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

            reflInstBuffer += Defn.Define(
              Attrs(),
              reflInstBuffer.name.member(applyMethodSig),
              nir.Type.Function(
                Seq(Type.Ref(reflInstBuffer.name), Type.Array(jlObjectRef)),
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

          reflInstBuffer += Defn.Class(
            Attrs(),
            reflInstBuffer.name,
            Some(srAbstractFunction1),
            Seq(serializable)
          )

          // Allocate an instance of the generated class.
          val instantiator =
            allocAndConstruct(exprBuf, reflInstBuffer.name, Seq(), Seq())

          // Create the current constructor's info. We need:
          // - an array with the runtime classes of the ctor parameters.
          // - the instantiator function created above (instantiator).
          val rtClasses = exprBuf.arrayalloc(
            jlClassRef,
            Val.Int(ctorSig.args.tail.length),
            unwind(curFresh)
          )
          for ((arg, argIdx) <- ctorSig.args.tail.zipWithIndex) {
            // Store the runtime class in the array.
            exprBuf.arraystore(
              jlClassRef,
              rtClasses,
              Val.Int(argIdx),
              Val.ClassOf(Type.typeToName(arg)),
              unwind(curFresh)
            )
          }

          // Allocate a tuple to store the current constructor's info
          val to = createTuple2(exprBuf, rtClasses, instantiator)

          exprBuf.arraystore(
            tuple2Ref,
            ctorsInfo,
            Val.Int(ctorIdx),
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
          exprBuf.label(curFresh(), Seq())

          val fqcnArg = Val.String(fqSymId)
          val runtimeClassArg = Val.ClassOf(fqSymName)

          val instantiateClassFunArg =
            genClassConstructorsInfo(exprBuf, ctors)

          exprBuf.genApplyModuleMethod(
            ReflectModule,
            Reflect_registerInstantiatableClass,
            Seq(fqcnArg, runtimeClassArg, instantiateClassFunArg).map(
              ValTree(_)
            )
          )

          exprBuf.ret(Val.Unit)
          exprBuf.toSeq
        }
    }

    def genMethods(cd: ClassDef): Unit = {
      val methods = cd.impl.body.flatMap {
        case dd: DefDef => genMethod(dd)
        case _          => Nil
      }
      val forwarders = genStaticMethodForwarders(cd, methods)
      buf ++= methods
      buf ++= forwarders
    }

    private def genJavaDefaultMethodBody(dd: DefDef): Seq[nir.Inst] = {
      val fresh = Fresh()
      val buf = new ExprBuffer()(fresh)

      implicit val pos: nir.Position = dd.pos

      val sym = dd.symbol
      val implClassFullName = sym.owner.fullName + "$class"

      val implClassSym = findMemberFromRoot(TermName(implClassFullName))

      val implMethodSym = implClassSym.info
        .member(sym.name)
        .suchThat { s =>
          s.isMethod &&
          s.tpe.params.size == sym.tpe.params.size + 1 &&
          s.tpe.params.head.tpe =:= sym.owner.toTypeConstructor &&
          s.tpe.params.tail.zip(sym.tpe.params).forall {
            case (sParam, symParam) =>
              sParam.tpe =:= symParam.tpe
          }
        }

      val implName = Val.Global(genMethodName(implMethodSym), Type.Ptr)
      val implSig = genMethodSig(implMethodSym)

      val Type.Function(paramtys, retty) = implSig

      val params = paramtys.map(ty => Val.Local(fresh(), ty))
      buf.label(fresh(), params)

      val res = buf.call(implSig, implName, params, Next.None)
      buf.ret(res)

      buf.toSeq
    }

    def genMethod(dd: DefDef): Option[nir.Defn] = {
      val fresh = Fresh()
      val env = new MethodEnv(fresh)

      implicit val pos: nir.Position = dd.pos

      scoped(
        curMethodSym := dd.symbol,
        curMethodEnv := env,
        curMethodInfo := (new CollectMethodInfo).collect(dd.rhs),
        curFresh := fresh,
        curUnwindHandler := None
      ) {
        val sym = dd.symbol
        val owner = curClassSym.get
        val attrs = genMethodAttrs(sym)
        val name = genMethodName(sym)
        val sig = genMethodSig(sym)

        dd.rhs match {
          case EmptyTree
              if (isScala211 &&
                sym.hasAnnotation(JavaDefaultMethodAnnotation)) =>
            scoped(
              curMethodSig := sig
            ) {
              val body = genJavaDefaultMethodBody(dd)
              Some(Defn.Define(attrs, name, sig, body))
            }

          case EmptyTree =>
            Some(Defn.Declare(attrs, name, sig))

          case Apply(TypeApply(Select(retBlock, _), _), _)
              if retBlock.tpe == NoType && isScala211 =>
            // Fix issue #2305 Compile error on macro using Scala 2.11.12
            Some(Defn.Declare(attrs, name, sig))

          case _ if dd.name == nme.CONSTRUCTOR && owner.isExternModule =>
            validateExternCtor(dd.rhs)
            None

          case _ if dd.name == nme.CONSTRUCTOR && owner.isStruct =>
            None

          case rhs if owner.isExternModule =>
            checkExplicitReturnTypeAnnotation(dd, "extern method")
            genExternMethod(attrs, name, sig, rhs)

          case rhs
              if (isScala211 &&
                sym.hasAnnotation(JavaDefaultMethodAnnotation) &&
                !isImplClass(sym.owner)) =>
            // Have a concrete method with JavaDefaultMethodAnnotation; a blivet.
            // Do not emit, not even as abstract.
            None

          case _ if sym.hasAnnotation(ResolvedAtLinktimeClass) =>
            genLinktimeResolved(dd, name)

          case rhs =>
            scoped(
              curMethodSig := sig
            ) {
              val body = genMethodBody(dd, rhs)
              Some(Defn.Define(attrs, name, sig, body))
            }
        }
      }
    }

    protected def genLinktimeResolved(dd: DefDef, name: Global)(implicit
        pos: nir.Position
    ): Option[nir.Defn] = {
      if (dd.symbol.isConstant) {
        globalError(
          dd.pos,
          "Link-time property cannot be constant value, it would be inlined by scalac compiler"
        )
      }

      dd.rhs.symbol match {
        case ResolvedMethod =>
          checkExplicitReturnTypeAnnotation(dd, "value resolved at link-time")
          dd match {
            case LinktimeProperty(propertyName, _) =>
              val retty = genType(dd.tpt.tpe)
              val defn = genLinktimeResolvedMethod(retty, propertyName, name)
              Some(defn)
            case _ => None
          }
        case _ =>
          globalError(
            dd.pos,
            s"Link-time resolved property must have ${ResolvedMethod.fullName} as body"
          )
          None
      }
    }

    /* Generate stub method that can be used to get value of link-time property at runtime */
    private def genLinktimeResolvedMethod(
        retty: nir.Type,
        propertyName: String,
        methodName: nir.Global
    )(implicit pos: nir.Position): nir.Defn = {
      implicit val fresh: Fresh = Fresh()
      val buf = new ExprBuffer()

      buf.label(fresh())
      val value = buf.call(
        Linktime.PropertyResolveFunctionTy(retty),
        Linktime.PropertyResolveFunction(retty),
        Val.String(propertyName) :: Nil,
        Next.None
      )
      buf.ret(value)

      Defn.Define(
        Attrs(inlineHint = Attr.AlwaysInline),
        methodName,
        Type.Function(Seq(), retty),
        buf.toSeq
      )
    }

    def genExternMethod(
        attrs: nir.Attrs,
        name: nir.Global,
        origSig: nir.Type,
        rhs: Tree
    ): Option[nir.Defn] = {
      rhs match {
        case Apply(ref: RefTree, Seq()) if ref.symbol == ExternMethod =>
          val moduleName = genTypeName(curClassSym)
          val externAttrs = Attrs(isExtern = true)
          val externSig = genExternMethodSig(curMethodSym)
          val externDefn = Defn.Declare(externAttrs, name, externSig)(rhs.pos)
          Some(externDefn)

        case _ if curMethodSym.hasFlag(ACCESSOR) => None

        case rhs =>
          global.reporter.error(
            rhs.pos,
            "methods in extern objects must have extern body"
          )
          None
      }
    }

    def validateExternCtor(rhs: Tree): Unit = {
      val Block(_ +: init, _) = rhs
      val externs = init.map {
        case Assign(ref: RefTree, Apply(extern, Seq()))
            if extern.symbol == ExternMethod =>
          ref.symbol
        case _ =>
          unsupported(
            "extern objects may only contain extern fields and methods"
          )
      }.toSet
      for {
        f <- curClassSym.info.decls if f.isField
        if !externs.contains(f)
      } {
        unsupported("extern objects may only contain extern fields")
      }
    }

    def genMethodAttrs(sym: Symbol): Attrs = {
      val inlineAttrs =
        if (sym.isBridge || sym.hasFlag(ACCESSOR)) {
          Seq(Attr.AlwaysInline)
        } else {
          sym.annotations.collect {
            case ann if ann.symbol == NoInlineClass     => Attr.NoInline
            case ann if ann.symbol == AlwaysInlineClass => Attr.AlwaysInline
            case ann if ann.symbol == InlineClass       => Attr.InlineHint
          }
        }
      val stubAttrs =
        sym.annotations.collect {
          case ann if ann.symbol == StubClass => Attr.Stub
        }
      val optAttrs =
        sym.annotations.collect {
          case ann if ann.symbol == NoOptimizeClass   => Attr.NoOpt
          case ann if ann.symbol == NoSpecializeClass => Attr.NoSpecialize
        }

      Attrs.fromSeq(inlineAttrs ++ stubAttrs ++ optAttrs)
    }

    def genMethodBody(
        dd: DefDef,
        bodyp: Tree
    ): Seq[nir.Inst] = {
      val fresh = curFresh.get
      val buf = new ExprBuffer()(fresh)
      val isSynchronized = dd.symbol.hasFlag(SYNCHRONIZED)
      val isStatic = dd.symbol.isStaticInNIR || isImplClass(dd.symbol.owner)
      val isExtern = dd.symbol.owner.isExternModule

      implicit val pos: nir.Position = bodyp.pos

      val paramSyms = genParamSyms(dd, isStatic)
      val params = paramSyms.map {
        case None =>
          val ty = genType(curClassSym.tpe)
          Val.Local(fresh(), ty)
        case Some(sym) =>
          val ty = genType(sym.tpe)
          val param = Val.Local(fresh(), ty)
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
          val slot = buf.var_(ty, unwind(fresh))
          curMethodEnv.enter(sym, slot)
        }
      }

      def withOptSynchronized(bodyGen: ExprBuffer => Val): Val = {
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

      def genBody(): Val = bodyp match {
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
              else Some(Val.Local(params.head.name, params.head.ty))
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
              else Some(Val.Local(params.head.name, params.head.ty))
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
      removeDeadBlocks(buf.toSeq)
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
    !settings.noForwarders.value && sym.isStatic && !isImplClass(sym) && {
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
      existingMembers: Seq[Defn],
      sym: Symbol
  ): Seq[Defn.Define] = {
    /*  Phase travel is necessary for non-top-level classes, because flatten
     *  breaks their companionModule. This is tracked upstream at
     *  https://github.com/scala/scala-dev/issues/403
     */
    val module = exitingPhase(currentRun.picklerPhase)(sym.companionModule)
    if (module == NoSymbol) Nil
    else {
      val moduleClass = module.moduleClass
      if (moduleClass.isExternModule) Nil
      else genStaticForwardersFromModuleClass(existingMembers, moduleClass)
    }
  }

  private lazy val dontUseExitingUncurryForForwarders =
    scala.util.Properties.versionNumberString.startsWith("2.11.")

  /** Gen the static forwarders for the methods of a module class.
   *
   *  Precondition: `isCandidateForForwarders(moduleClass)` is true
   */
  private def genStaticForwardersFromModuleClass(
      existingMembers: Seq[Defn],
      moduleClass: Symbol
  ): Seq[Defn.Define] = {
    assert(moduleClass.isModuleClass, moduleClass)

    lazy val existingStaticMethodNames = existingMembers.collect {
      case nir.Defn.Define(_, name @ Global.Member(_, sig), _, _)
          if sig.isStatic =>
        name
    }

    def listMembersBasedOnFlags = {
      import scala.tools.nsc.symtab.Flags._
      // Copy-pasted from BCodeHelpers (it's somewhere else in 2.11.x)
      val ExcludedForwarderFlags: Long = {
        SPECIALIZED | LIFTED | PROTECTED | STATIC | EXPANDEDNAME | PRIVATE | MACRO
      }

      moduleClass.info.membersBasedOnFlags(
        ExcludedForwarderFlags,
        METHOD
      )
    }

    /* See BCodeHelprs.addForwarders in 2.12+ for why we normally use
     * exitingUncurry. In 2.11.x we do not use it, because Scala/JVM did not
     * use it back then, and using it on that version causes mixed in methods
     * not to be found (this notably breaks `extends App` as the `main`
     * method that it defines is not found).
     *
     * This means that in 2.11.x we suffer from
     * https://github.com/scala/bug/issues/10812, like upstream Scala/JVM,
     * but it does not really affect Scala Native because the NIR methods are not
     * used for compilation, only for linking, and for linking it is fine to
     * have additional, unexpected bridges.
     */
    val members =
      if (dontUseExitingUncurryForForwarders) listMembersBasedOnFlags
      else exitingUncurry(listMembersBasedOnFlags)

    def isExcluded(m: Symbol): Boolean = {
      def isOfJLObject: Boolean = {
        val o = m.owner
        (o eq ObjectClass) || (o eq AnyRefClass) || (o eq AnyClass)
      }

      m.isDeferred || m.isConstructor || m.hasAccessBoundary ||
        m.owner.isExternModule ||
        isOfJLObject
    }

    val forwarders = for {
      sym <- members
      if !isExcluded(sym)
    } yield {
      implicit val pos: nir.Position = sym.pos

      val methodName = genMethodName(sym)
      val forwarderName = genStaticMemberName(sym, moduleClass)
      val Type.Function(_ +: paramTypes, retType) = genMethodSig(sym)
      val forwarderParamTypes = paramTypes
      val forwarderType = Type.Function(forwarderParamTypes, retType)

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

      Defn.Define(
        attrs = Attrs(inlineHint = nir.Attr.InlineHint),
        name = forwarderName,
        ty = forwarderType,
        insts = curStatBuffer
          .withFreshExprBuffer { buf =>
            val fresh = curFresh.get
            scoped(
              curUnwindHandler := None,
              curMethodThis := None
            ) {
              val entryParams = forwarderParamTypes.map(Val.Local(fresh(), _))
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
      existingMethods: Seq[Defn]
  ): Seq[Defn] = {
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
      val classDefn = Defn.Class(
        attrs = Attrs.None,
        name = Global.Top(genTypeName(sym).id.stripSuffix("$")),
        parent = Some(Rt.Object.name),
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
    def unapply(tree: Tree): Option[(String, nir.Position)] = {
      if (tree.symbol == null) None
      else {
        tree.symbol
          .getAnnotation(ResolvedAtLinktimeClass)
          .flatMap(_.args.headOption)
          .flatMap {
            case Literal(Constant(name: String)) => Some((name, tree.pos))
            case _ =>
              globalError(
                tree.symbol.pos,
                s"Name used to resolve link-time property needs to be non-null literal constant"
              )
              None
          }
      }
    }
  }
}
