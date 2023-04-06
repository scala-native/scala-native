package scala.scalanative.junit.plugin

// Ported from Scala.js

import scala.annotation.tailrec
import scala.tools.nsc._
import scala.tools.nsc.plugins.{
  Plugin => NscPlugin,
  PluginComponent => NscPluginComponent
}

/** The Scala Native JUnit plugin replaces reflection based test lookup.
 *
 *  For each JUnit test `my.pkg.X`, it generates a bootstrapper module/object
 *  `my.pkg.X\$scalanative\$junit\$bootstrapper` implementing
 *  `scala.scalanative.junit.Bootstrapper`.
 *
 *  The test runner uses these objects to obtain test metadata and dispatch to
 *  relevant methods.
 */
class ScalaNativeJUnitPlugin(val global: Global) extends NscPlugin {

  val name: String = "scalanative-junit"
  val description: String = "Makes JUnit test classes invokable in Scala Native"

  val components: List[NscPluginComponent] = global match {
    case _: doc.ScaladocGlobal => Nil
    case _                     => List(ScalaNativeJUnitPluginComponent)
  }

  object ScalaNativeJUnitPluginComponent
      extends plugins.PluginComponent
      with transform.Transform {

    val global: Global = ScalaNativeJUnitPlugin.this.global
    import global._
    import definitions._
    import rootMirror.getRequiredClass

    val phaseName: String = "scalanative-junitBootstrappers"
    val runsAfter: List[String] = List("mixin")
    override val runsBefore: List[String] = List("scalanative-genNIR")

    protected def newTransformer(unit: CompilationUnit): Transformer =
      new ScalaNativeJUnitPluginTransformer

    private object JUnitAnnots {
      val Test: ClassSymbol = getRequiredClass("org.junit.Test")
      val Before: ClassSymbol = getRequiredClass("org.junit.Before")
      val After: ClassSymbol = getRequiredClass("org.junit.After")
      val BeforeClass: ClassSymbol = getRequiredClass("org.junit.BeforeClass")
      val AfterClass: ClassSymbol = getRequiredClass("org.junit.AfterClass")
      val Ignore: ClassSymbol = getRequiredClass("org.junit.Ignore")
    }

    private object Names {
      val beforeClass: TermName = newTermName("beforeClass")
      val afterClass: TermName = newTermName("afterClass")
      val before: TermName = newTermName("before")
      val after: TermName = newTermName("after")
      val testClassMetadata: TermName = newTermName("testClassMetadata")
      val tests: TermName = newTermName("tests")
      val invokeTest: TermName = newTermName("invokeTest")
      val newInstance: TermName = newTermName("newInstance")

      val instance: TermName = newTermName("instance")
      val name: TermName = newTermName("name")
    }

    private lazy val BootstrapperClass =
      getRequiredClass("scala.scalanative.junit.Bootstrapper")

    private lazy val TestClassMetadataClass =
      getRequiredClass("scala.scalanative.junit.TestClassMetadata")

    private lazy val TestMetadataClass =
      getRequiredClass("scala.scalanative.junit.TestMetadata")

    private lazy val FutureClass =
      getRequiredClass("scala.concurrent.Future")

    private lazy val FutureModule_successful =
      getMemberMethod(FutureClass.companionModule, newTermName("successful"))

    private lazy val SuccessModule_apply =
      getMemberMethod(
        getRequiredClass("scala.util.Success").companionModule,
        nme.apply
      )

    class ScalaNativeJUnitPluginTransformer extends Transformer {
      override def transform(tree: Tree): Tree = tree match {
        case tree: PackageDef =>
          @tailrec
          def hasTests(sym: Symbol): Boolean = {
            sym.info.members.exists(m =>
              m.isMethod && m.hasAnnotation(JUnitAnnots.Test)
            ) ||
            sym.superClass.exists && hasTests(sym.superClass)
          }

          def isTest(sym: Symbol) = {
            sym.isClass &&
            !sym.isModuleClass &&
            !sym.isAbstract &&
            !sym.isTrait &&
            hasTests(sym)
          }

          val bootstrappers = tree.stats.collect {
            case clDef: ClassDef if isTest(clDef.symbol) =>
              genBootstrapper(clDef.symbol.asClass)
          }

          val newStats = tree.stats.map(transform) ++ bootstrappers
          treeCopy.PackageDef(tree, tree.pid, newStats)

        case tree =>
          super.transform(tree)
      }

      def genBootstrapper(testClass: ClassSymbol): ClassDef = {
        // Create the module and its module class, and enter them in their owner's scope
        val (moduleSym, bootSym) = testClass.owner.newModuleAndClassSymbol(
          newTypeName(
            testClass.name.toString + "$scalanative$junit$bootstrapper"
          ),
          testClass.pos,
          0L
        )
        val bootInfo =
          ClassInfoType(
            List(ObjectTpe, BootstrapperClass.toType),
            newScope,
            bootSym
          )
        bootSym.setInfo(bootInfo)
        moduleSym.setInfoAndEnter(bootSym.toTypeConstructor)
        bootSym.owner.info.decls.enter(bootSym)

        val testMethods = annotatedMethods(testClass, JUnitAnnots.Test)

        val defs = List(
          genConstructor(bootSym),
          genCallOnModule(
            bootSym,
            Names.beforeClass,
            testClass,
            JUnitAnnots.BeforeClass,
            callParentsFirst = true
          ),
          genCallOnModule(
            bootSym,
            Names.afterClass,
            testClass,
            JUnitAnnots.AfterClass,
            callParentsFirst = false
          ),
          genCallOnParam(bootSym, Names.before, testClass, JUnitAnnots.Before),
          genCallOnParam(bootSym, Names.after, testClass, JUnitAnnots.After),
          genTestMetadata(bootSym, testClass),
          genTests(bootSym, testMethods),
          genInvokeTest(bootSym, testClass, testMethods),
          genNewInstance(bootSym, testClass)
        )

        ClassDef(bootSym, defs)
      }

      private def genConstructor(owner: ClassSymbol): DefDef = {
        /* The constructor body must be a Block in order not to freak out the
         * JVM back-end.
         */
        val rhs = Block(
          gen.mkMethodCall(
            Super(owner, tpnme.EMPTY),
            ObjectClass.primaryConstructor,
            Nil,
            Nil
          )
        )

        val sym = owner.newClassConstructor(NoPosition)
        sym.setInfoAndEnter(MethodType(Nil, owner.tpe))
        typer.typedDefDef(newDefDef(sym, rhs)())
      }

      private def genCallOnModule(
          owner: ClassSymbol,
          name: TermName,
          testClass: Symbol,
          annot: Symbol,
          callParentsFirst: Boolean
      ): DefDef = {
        val sym = owner.newMethodSymbol(name)
        sym.setInfoAndEnter(MethodType(Nil, definitions.UnitTpe))

        val symbols = {
          val all = (testClass :: testClass.ancestors)
          if (callParentsFirst) all.reverse
          else all
        }

        // Filter out annotations found in the companion of trait for compliance with the JVM
        val (publicCalls, nonPublicCalls) =
          symbols
            .filterNot(_.isTraitOrInterface)
            .flatMap(sym => annotatedMethods(sym.companionModule, annot))
            .partition(_.isPublic)

        if (nonPublicCalls.nonEmpty) {
          val module = testClass.companionModule.orElse(testClass)
          globalError(
            pos = module.pos,
            s"Methods marked with ${annot.nameString} annotation in $module must be public"
          )
        }

        val calls = publicCalls
          .map(gen.mkMethodCall(_, Nil, Nil))
          .toList

        typer.typedDefDef(newDefDef(sym, Block(calls: _*))())
      }

      private def genCallOnParam(
          owner: ClassSymbol,
          name: TermName,
          testClass: Symbol,
          annot: Symbol
      ): DefDef = {
        val sym = owner.newMethodSymbol(name)

        val instanceParam =
          sym.newValueParameter(Names.instance).setInfo(ObjectTpe)

        sym.setInfoAndEnter(
          MethodType(List(instanceParam), definitions.UnitTpe)
        )

        val instance = castParam(instanceParam, testClass)

        val (publicCalls, nonPublicCalls) =
          annotatedMethods(testClass, annot).partition(_.isPublic)

        if (nonPublicCalls.nonEmpty) {
          globalError(
            pos = testClass.pos,
            s"Methods marked with ${annot.nameString} annotation in $testClass must be public"
          )
        }

        val calls = publicCalls
          .map(gen.mkMethodCall(instance, _, Nil, Nil))
          .toList

        typer.typedDefDef(newDefDef(sym, Block(calls: _*))())
      }

      private def genTestMetadata(
          owner: ClassSymbol,
          testClass: ClassSymbol
      ): DefDef = {
        val sym = owner.newMethodSymbol(Names.testClassMetadata)

        sym.setInfoAndEnter(
          MethodType(Nil, typeRef(NoType, TestClassMetadataClass, Nil))
        )

        val ignored = testClass.hasAnnotation(JUnitAnnots.Ignore)
        val isIgnored = Literal(Constant(ignored))

        val rhs = New(TestClassMetadataClass, isIgnored)

        typer.typedDefDef(newDefDef(sym, rhs)())
      }

      private def genTests(owner: ClassSymbol, tests: Scope): DefDef = {
        val sym = owner.newMethodSymbol(Names.tests)

        sym.setInfoAndEnter(
          MethodType(
            Nil,
            typeRef(NoType, ArrayClass, List(TestMetadataClass.tpe))
          )
        )

        val metadata = for (test <- tests) yield {
          val reifiedAnnot = New(
            JUnitAnnots.Test,
            test.getAnnotation(JUnitAnnots.Test).get.args: _*
          )

          val name = Literal(Constant(test.name.toString))

          val testIgnored = test.hasAnnotation(JUnitAnnots.Ignore)
          val isIgnored = Literal(Constant(testIgnored))

          New(TestMetadataClass, name, isIgnored, reifiedAnnot)
        }

        val rhs = ArrayValue(TypeTree(TestMetadataClass.tpe), metadata.toList)

        typer.typedDefDef(newDefDef(sym, rhs)())
      }

      private def genInvokeTest(
          owner: ClassSymbol,
          testClass: Symbol,
          tests: Scope
      ): DefDef = {
        val sym = owner.newMethodSymbol(Names.invokeTest)

        val instanceParam =
          sym.newValueParameter(Names.instance).setInfo(ObjectTpe)
        val nameParam = sym.newValueParameter(Names.name).setInfo(StringTpe)

        sym.setInfo(
          MethodType(
            List(instanceParam, nameParam),
            FutureClass.toTypeConstructor
          )
        )

        val instance = castParam(instanceParam, testClass)
        val rhs = tests.foldRight[Tree] {
          Throw(New(typeOf[NoSuchMethodException], Ident(nameParam)))
        } { (sym, next) =>
          val cond =
            gen.mkMethodCall(
              Ident(nameParam),
              Object_equals,
              Nil,
              List(Literal(Constant(sym.name.toString)))
            )

          val call = genTestInvocation(sym, instance)

          If(cond, call, next)
        }

        typer.typedDefDef(newDefDef(sym, rhs)())
      }

      private def genTestInvocation(sym: Symbol, instance: Tree): Tree = {
        sym.tpe.resultType.typeSymbol match {
          case UnitClass =>
            val boxedUnit = gen.mkAttributedRef(definitions.BoxedUnit_UNIT)
            val newSuccess =
              gen.mkMethodCall(SuccessModule_apply, List(boxedUnit))
            Block(
              gen.mkMethodCall(instance, sym, Nil, Nil),
              gen.mkMethodCall(FutureModule_successful, List(newSuccess))
            )

          case FutureClass =>
            gen.mkMethodCall(instance, sym, Nil, Nil)

          case _ =>
            // We lie in the error message to not expose that we support async testing.
            reporter.error(sym.pos, "JUnit test must have Unit return type")
            EmptyTree
        }
      }

      private def genNewInstance(
          owner: ClassSymbol,
          testClass: ClassSymbol
      ): DefDef = {
        val sym = owner.newMethodSymbol(Names.newInstance)
        sym.setInfoAndEnter(MethodType(Nil, ObjectTpe))
        typer.typedDefDef(newDefDef(sym, New(testClass))())
      }

      private def castParam(param: Symbol, clazz: Symbol): Tree =
        gen.mkAsInstanceOf(Ident(param), clazz.tpe, any = false)

      private def annotatedMethods(owner: Symbol, annot: Symbol): Scope =
        owner.info.members.filter(m => m.isMethod && m.hasAnnotation(annot))
    }
  }
}
