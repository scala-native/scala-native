package scala.scalanative.nscplugin

import scala.language.implicitConversions

import dotty.tools.dotc.ast.tpd._
import dotty.tools.dotc.core
import core.Contexts._
import core.Symbols._
import core.StdNames._
import core.Flags._

import scala.collection.mutable
import scala.scalanative.nir
import nir._
import scala.scalanative.util.ScopedVar
import scala.scalanative.util.ScopedVar.{scoped, toValue}

object GenReflectiveInstantisation {
  object nirSymbols {
    val AbstractFunction0Name = Global.Top("scala.runtime.AbstractFunction0")
    val AbstractFunction1Name = Global.Top("scala.runtime.AbstractFunction1")
    val SerializableName = Global.Top("java.io.Serializable")
    val Tuple2Name = Global.Top("scala.Tuple2")
    val Tuple2Ref = Type.Ref(Tuple2Name)
  }
}

trait GenReflectiveInstantisation(using Context) {
  self: NirCodeGen =>
  import GenReflectiveInstantisation._
  import positionsConversions.given

  protected val reflectiveInstantiationBuffers =
    mutable.UnrolledBuffer.empty[ReflectiveInstantiationBuffer]

  protected class ReflectiveInstantiationBuffer(fqcn: String) {
    val name = nir.Global.Top(fqcn + "$scalanative$ReflectivelyInstantiate$")
    reflectiveInstantiationBuffers += this
    private val buf = mutable.UnrolledBuffer.empty[nir.Defn]

    def +=(defn: nir.Defn): Unit = buf += defn
    def nonEmpty = buf.nonEmpty
    def toSeq = buf.toSeq
  }

  def genReflectiveInstantiation(td: TypeDef): Unit = {
    val sym = td.symbol.asClass
    val enableReflectiveInstantiation =
      sym.baseClasses
        .exists(
          _.hasAnnotation(defnNir.EnableReflectiveInstantiationAnnotationClass)
        )

    if (enableReflectiveInstantiation) {
      scoped(
        curClassSym := sym,
        curFresh := Fresh(),
        curUnwindHandler := None,
        curMethodThis := None
      ) {
        registerReflectiveInstantiation(td)
      }
    }
  }

  private def registerReflectiveInstantiation(td: TypeDef): Unit = {
    given nir.Position = td.span
    val sym: Symbol = curClassSym
    val owner = genTypeName(sym)
    val name = owner.member(nir.Sig.Clinit())

    val staticInitBody =
      if (curClassSym.get.is(flag = Module, butNot = Lifted))
        Some(registerModuleClass(td))
      else if (sym.is(Module))
        None // see: https://github.com/scala-js/scala-js/issues/3228
      else if (sym.is(Lifted) && !sym.originalOwner.isClass)
        None // see: https://github.com/scala-js/scala-js/issues/3227
      else Some(registerNormalClass(td))

    staticInitBody
      .filter(_.nonEmpty)
      .foreach { body =>
        generatedDefns +=
          Defn.Define(
            Attrs(),
            name,
            nir.Type.Function(Seq.empty[nir.Type], Type.Unit),
            body
          )
      }
  }

  private def registerModuleClass(
      td: TypeDef
  ): Seq[Inst] = {
    val fqSymId = curClassSym.get.fullName.mangledString
    val fqSymName = Global.Top(fqSymId)
    val fqcnArg = Val.String(fqSymId)
    val runtimeClassArg = Val.ClassOf(fqSymName)

    given nir.Position = td.span
    given reflInstBuffer: ReflectiveInstantiationBuffer =
      ReflectiveInstantiationBuffer(fqSymId)

    withFreshExprBuffer { buf ?=>

      buf.label(curFresh(), Seq.empty)
      val loadModuleFunArg = genModuleLoader(fqSymName)
      buf.genApplyModuleMethod(
        defnNir.ReflectModule,
        defnNir.Reflect_registerLoadableModuleClass,
        Seq(fqcnArg, runtimeClassArg, loadModuleFunArg).map(ValTree(_))
      )
      buf.ret(Val.Unit)
      buf.toSeq
    }
  }

  private def registerNormalClass(
      td: TypeDef
  ): Seq[Inst] = {
    given nir.Position = td.span

    val fqSymId = curClassSym.get.fullName.mangledString
    val fqSymName = Global.Top(fqSymId)
    val fqcnArg = Val.String(fqSymId)
    val runtimeClassArg = Val.ClassOf(fqSymName)

    // Collect public constructors.
    val ctors =
      if (curClassSym.get.isOneOf(AbstractOrTrait)) Nil
      else
        curClassSym.get.info
          .member(nme.CONSTRUCTOR)
          .alternatives
          .collect {
            case denot if denot.asSymDenotation.isPublic =>
              denot.asSymDenotation.underlyingSymbol
          }

    if (ctors.isEmpty) Nil
    else
      withFreshExprBuffer { buf ?=>
        buf.label(curFresh(), Seq.empty)
        val instantiateClassFunArg = genClassConstructorsInfo(fqSymName, ctors)
        buf.genApplyModuleMethod(
          defnNir.ReflectModule,
          defnNir.Reflect_registerInstantiatableClass,
          Seq(fqcnArg, runtimeClassArg, instantiateClassFunArg)
            .map(ValTree(_))
        )
        buf.ret(Val.Unit)
        buf.toSeq
      }
  }

  // Generate the constructor for the class instantiator class,
  // which is expected to extend one of scala.runtime.AbstractFunctionX.
  private def genConstructor(
      superClass: Global
  )(using
      nir.Position
  )(using reflInstBuffer: ReflectiveInstantiationBuffer): Unit = {
    withFreshExprBuffer { buf ?=>
      val body = {
        // first argument is this
        val thisArg = Val.Local(curFresh(), Type.Ref(reflInstBuffer.name))
        buf.label(curFresh(), Seq(thisArg))
        // call to super constructor
        buf.call(
          Type.Function(Seq(Type.Ref(superClass)), Type.Unit),
          Val.Global(superClass.member(Sig.Ctor(Seq.empty)), Type.Ptr),
          Seq(thisArg),
          unwind(curFresh)
        )
        buf.ret(Val.Unit)
        buf.toSeq
      }

      reflInstBuffer += Defn.Define(
        Attrs(),
        reflInstBuffer.name.member(Sig.Ctor(Seq.empty)),
        nir.Type.Function(Seq(Type.Ref(reflInstBuffer.name)), Type.Unit),
        body
      )
    }
  }

// Allocate and construct an object, using the provided ExprBuffer.
  private def allocAndConstruct(
      name: Global,
      argTypes: Seq[nir.Type],
      args: Seq[Val]
  )(using pos: nir.Position, buf: ExprBuffer): Val = {
    val alloc = buf.classalloc(name, unwind(curFresh))
    buf.call(
      Type.Function(Type.Ref(name) +: argTypes, Type.Unit),
      Val.Global(name.member(Sig.Ctor(argTypes)), Type.Ptr),
      alloc +: args,
      unwind(curFresh)
    )
    alloc
  }

  private def genModuleLoader(
      fqSymName: Global
  )(using
      pos: nir.Position,
      buf: ExprBuffer,
      reflInstBuffer: ReflectiveInstantiationBuffer
  ): Val = {
    val applyMethodSig = Sig.Method("apply", Seq(Rt.Object))
    val enclosingClass = curClassSym.get.originalOwner

    // Generate the module loader class. The generated class extends
    // AbstractFunction0[Any], i.e. has an apply method, which loads the module.
    // We need a fresh ExprBuffer for this, since it is different scope.
    withFreshExprBuffer { buf ?=>
      val body = {
        // first argument is this
        val thisArg = Val.Local(curFresh(), Type.Ref(reflInstBuffer.name))
        buf.label(curFresh(), Seq(thisArg))

        val module =
          if (enclosingClass.exists && !enclosingClass.is(ModuleClass)) Val.Null
          else buf.module(fqSymName, unwind(curFresh))
        buf.ret(module)
        buf.toSeq
      }

      reflInstBuffer += Defn.Define(
        Attrs(),
        reflInstBuffer.name.member(applyMethodSig),
        nir.Type.Function(Seq(Type.Ref(reflInstBuffer.name)), Rt.Object),
        body
      )
    }

    // Generate the module loader class constructor.
    genConstructor(nirSymbols.AbstractFunction0Name)

    reflInstBuffer += Defn.Class(
      Attrs(),
      reflInstBuffer.name,
      Some(nirSymbols.AbstractFunction0Name),
      Seq(nirSymbols.SerializableName)
    )

    // Allocate and return an instance of the generated class.
    allocAndConstruct(reflInstBuffer.name, Seq.empty, Seq.empty)(using pos, buf)
  }

  // Create a new Tuple2 and initialise it with the provided values.
  private def createTuple(arg1: Val, arg2: Val)(using
      nir.Position,
      ExprBuffer
  ): Val = {
    allocAndConstruct(
      nirSymbols.Tuple2Name,
      Seq(Rt.Object, Rt.Object),
      Seq(arg1, arg2)
    )
  }

  private def genClassConstructorsInfo(
      fqSymName: Global.Top,
      ctors: Seq[Symbol]
  )(using pos: nir.Position, buf: ExprBuffer): Val = {
    val applyMethodSig = Sig.Method("apply", Seq(Rt.Object, Rt.Object))

    // Constructors info is an array of Tuple2 (tpes, inst), where:
    // - tpes is an array with the runtime classes of the constructor arguments.
    // - inst is a function, which accepts an array with tpes and returns a new
    //   instance of the class.
    val ctorsInfo = buf.arrayalloc(
      Type.Array(nirSymbols.Tuple2Ref),
      Val.Int(ctors.length),
      unwind(curFresh)
    )

    // For each (public) constructor C, generate a lambda responsible for
    // initialising and returning an instance of the class, using C.
    for ((ctor, ctorIdx) <- ctors.zipWithIndex) {
      val ctorSig = genMethodSig(ctor)
      val ctorArgsSig = ctorSig.args.map(_.mangle).mkString
      given nir.Position = ctor.span
      given reflInstBuffer: ReflectiveInstantiationBuffer =
        ReflectiveInstantiationBuffer(fqSymName.id + ctorArgsSig)

      // Lambda generation consists of generating a class which extends
      // scala.runtime.AbstractFunction1, with an apply method that accepts
      // the list of arguments, instantiates an instance of the class by
      // forwarding the arguments to C, and returns the instance.
      withFreshExprBuffer { buf ?=>
        val body = {
          // first argument is this
          val thisArg = Val.Local(curFresh(), Type.Ref(reflInstBuffer.name))
          // second argument is parameters sequence
          val argsArg = Val.Local(curFresh(), Type.Array(Rt.Object))
          buf.label(curFresh(), Seq(thisArg, argsArg))

          // Extract and cast arguments to proper types.
          val argsVals =
            for (arg, argIdx) <- ctorSig.args.tail.zipWithIndex
            yield {
              val elem =
                buf.arrayload(
                  Rt.Object,
                  argsArg,
                  Val.Int(argIdx),
                  unwind(curFresh)
                )
              // If the expected argument type can be boxed (i.e. is a primitive
              // type), then we need to unbox it before passing it to C.
              Type.box.get(arg) match {
                case Some(bt) => buf.unbox(bt, elem, unwind(curFresh))
                case None     => buf.as(arg, elem, unwind(curFresh))
              }
            }

          // Allocate a new instance and call constructor
          val alloc = allocAndConstruct(
            fqSymName,
            ctorSig.args.tail,
            argsVals
          )

          buf.ret(alloc)
          buf.toSeq
        }

        reflInstBuffer += Defn.Define(
          Attrs.None,
          reflInstBuffer.name.member(applyMethodSig),
          nir.Type.Function(
            Seq(Type.Ref(reflInstBuffer.name), Type.Array(Rt.Object)),
            Rt.Object
          ),
          body
        )
      }

      // Generate the class instantiator constructor.
      genConstructor(nirSymbols.AbstractFunction1Name)

      reflInstBuffer += Defn.Class(
        Attrs(),
        reflInstBuffer.name,
        Some(nirSymbols.AbstractFunction1Name),
        Seq(nirSymbols.SerializableName)
      )

      // Allocate an instance of the generated class.
      val instantiator =
        allocAndConstruct(reflInstBuffer.name, Seq.empty, Seq.empty)

      // Create the current constructor's info. We need:
      // - an array with the runtime classes of the ctor parameters.
      // - the instantiator function created above (instantiator).
      val rtClasses = buf.arrayalloc(
        Rt.Class,
        Val.Int(ctorSig.args.tail.length),
        unwind(curFresh)
      )
      for ((arg, argIdx) <- ctorSig.args.tail.zipWithIndex) {
        // Store the runtime class in the array.
        buf.arraystore(
          Rt.Class,
          rtClasses,
          Val.Int(argIdx),
          Val.ClassOf(Type.typeToName(arg)),
          unwind(curFresh)
        )
      }

      // Allocate a tuple to store the current constructor's info
      val to = createTuple(rtClasses, instantiator)

      buf.arraystore(
        nirSymbols.Tuple2Ref,
        ctorsInfo,
        Val.Int(ctorIdx),
        to,
        unwind(curFresh)
      )
    }
    ctorsInfo
  }

}
