package scala.scalanative.junit.plugin

import scala.annotation.tailrec

import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core
import core.Constants.*
import core.Contexts.*
import core.Decorators.*
import core.Flags.*
import core.Names.*
import core.NameOps.*
import core.Phases.*
import core.Scopes.*
import core.Symbols.*
import core.StdNames.*
import core.Types.*
import dotty.tools.dotc.plugins.PluginPhase
import dotty.tools.dotc.transform
import dotty.tools.dotc.report
import dotty.tools.dotc.sbt

/** Generates JUnit bootstrapper objects for Scala Native
 *
 *  Scala Native similarly as Scala.js cannot use reflection to invoke JUnit
 *  tests, instead it injects bootstrapper classes responsible for running them.
 *
 *  This phase is a port of dotty.tools.dotc.transform.sjs.JUnitBootstrappers
 *  based on release 3.1.0. Actual transformation logic is the same.
 *  Unfortunately we cannot use sjs implementation directly, because
 *  JsDefinitions assumes usage of JsPlatform which is absent in SN compiler
 *  plugin.
 *
 *  Additionally this port differs from Scala.js implementation by supporting
 *  test suite wide ignore (ported from Scala 2 Native compiler plugin)
 */
class ScalaNativeJUnitBootstrappers extends PluginPhase {
  def phaseName: String = "scalanative-junitBootstrappers"
  override val runsAfter = Set(transform.Mixin.name)
  override val runsBefore = Set("scalanative-genNIR")

  // Make sure to sync it the one used in JUnitTask
  final val bootstrapperSuffix = "$scalanative$junit$bootstrapper"

  // The actual transform -------------------------------
  override def transformPackageDef(tree: PackageDef)(using Context): Tree = {
    val junitdefn = JUnitDefinitions.defnJUnit

    @tailrec
    def hasTests(sym: ClassSymbol): Boolean = {
      sym.info.decls.exists(m =>
        m.is(Method) && m.hasAnnotation(junitdefn.TestAnnotClass)
      ) ||
      sym.superClass.exists && hasTests(sym.superClass.asClass)
    }

    def isTestClass(sym: Symbol): Boolean = {
      sym.isClass &&
      !sym.isOneOf(ModuleClass | Abstract | Trait) &&
      hasTests(sym.asClass)
    }

    val bootstrappers = tree.stats.collect {
      case clDef: TypeDef if isTestClass(clDef.symbol) =>
        genBootstrapper(clDef.symbol.asClass)
    }

    if bootstrappers.isEmpty then tree
    else cpy.PackageDef(tree)(tree.pid, tree.stats ::: bootstrappers)
  }

  private def genBootstrapper(
      testClass: ClassSymbol
  )(using Context): TypeDef = {
    val junitdefn = JUnitDefinitions.defnJUnit

    /* The name of the bootstrapper module. It is derived from the test class name by
     * appending a specific suffix string mandated "by spec". It will indeed also be
     * computed as such at run-time by the Scala.js JUnit Runtime support. Therefore,
     * it must *not* be a dotc semantic name.
     */
    val bootstrapperName =
      (testClass.name ++ bootstrapperSuffix).toTermName

    val owner = testClass.owner
    val moduleSym = newCompleteModuleSymbol(
      owner,
      bootstrapperName,
      Synthetic,
      Synthetic,
      List(defn.ObjectType, junitdefn.BootstrapperType),
      newScope,
      coord = testClass.span
    ).entered
    val classSym = moduleSym.moduleClass.asClass

    val constr = genConstructor(classSym)

    val testMethods = annotatedMethods(testClass, junitdefn.TestAnnotClass)

    val defs = List(
      genCallOnModule(
        classSym,
        junitNme.beforeClass,
        testClass,
        junitdefn.BeforeClassAnnotClass,
        callParentsFirst = true
      ),
      genCallOnModule(
        classSym,
        junitNme.afterClass,
        testClass,
        junitdefn.AfterClassAnnotClass,
        callParentsFirst = false
      ),
      genCallOnParam(
        classSym,
        junitNme.before,
        testClass,
        junitdefn.BeforeAnnotClass
      ),
      genCallOnParam(
        classSym,
        junitNme.after,
        testClass,
        junitdefn.AfterAnnotClass
      ),
      genTestMetadata(classSym, testClass),
      genTests(classSym, testMethods),
      genInvokeTest(classSym, testClass, testMethods),
      genNewInstance(classSym, testClass)
    )

    sbt.APIUtils.registerDummyClass(classSym)

    ClassDef(classSym, constr, defs)
  }

  private def genConstructor(owner: ClassSymbol)(using Context): DefDef = {
    val sym = newDefaultConstructor(owner).entered
    DefDef(
      sym, {
        Block(
          Super(This(owner), tpnme.EMPTY)
            .select(defn.ObjectClass.primaryConstructor)
            .appliedToNone :: Nil,
          unitLiteral
        )
      }
    )
  }

  private def genCallOnModule(
      owner: ClassSymbol,
      name: TermName,
      testClass: Symbol,
      annot: Symbol,
      callParentsFirst: Boolean
  )(using Context): DefDef = {
    val sym = newSymbol(
      owner,
      name,
      Synthetic | Method,
      MethodType(Nil, Nil, defn.UnitType)
    ).entered

    extension (sym: Symbol)
      def isTraitOrInterface: Boolean =
        sym.is(Trait) || sym.isAllOf(JavaInterface)

    DefDef(
      sym, {
        val allParents = List
          .unfold(testClass.info.parents) { parents =>
            parents.flatMap(_.parents) match {
              case Nil  => None
              case next => Some((parents ::: next), next)
            }
          }
          .flatten
          .distinct

        val symbols = {
          val all = testClass.info :: allParents
          if callParentsFirst then all.reverse else all
        }

        // Filter out annotations found in the companion of trait for compliance with the JVM
        val (publicCalls, nonPublicCalls) =
          symbols
            .filterNot(_.classSymbol.isTraitOrInterface)
            .map(_.classSymbol.companionModule)
            .filter(_.exists)
            .flatMap(s => annotatedMethods(s.moduleClass.asClass, annot))
            .partition(_.isPublic)

        if nonPublicCalls.nonEmpty then {
          val module = testClass.companionModule.orElse(testClass)
          report.error(
            s"Methods marked with ${annot.showName} annotation in $module must be public",
            module.orElse(owner).srcPos
          )
        }

        Block(
          publicCalls.map(m => Apply(ref(m), Nil)),
          unitLiteral
        )
      }
    )
  }

  private def genCallOnParam(
      owner: ClassSymbol,
      name: TermName,
      testClass: ClassSymbol,
      annot: Symbol
  )(using Context): DefDef = {
    val sym = newSymbol(
      owner,
      name,
      Synthetic | Method,
      MethodType(
        junitNme.instance :: Nil,
        defn.ObjectType :: Nil,
        defn.UnitType
      )
    ).entered

    DefDef(
      sym,
      { (paramRefss: List[List[Tree]]) =>
        val List(List(instanceParamRef)) = paramRefss
        val calls = annotatedMethods(testClass, annot)
          .map(m =>
            Apply(instanceParamRef.cast(testClass.typeRef).select(m), Nil)
          )
        Block(calls, unitLiteral)
      }
    )
  }

  // This method is not part of Scala 3/Scala.js bootstrappers implementation
  private def genTestMetadata(
      owner: ClassSymbol,
      testClass: ClassSymbol
  )(using Context): DefDef = {
    val junitdefn = JUnitDefinitions.defnJUnit

    val sym = newSymbol(
      owner,
      junitNme.testClassMetadata,
      Synthetic | Method,
      MethodType(Nil, junitdefn.TestClassMetadataType)
    ).entered

    DefDef(
      sym, {
        val hasIgnoreAnnot = testClass.hasAnnotation(junitdefn.IgnoreAnnotClass)
        val isIgnored = Literal(Constant(hasIgnoreAnnot))

        New(junitdefn.TestClassMetadataType, List(isIgnored))
      }
    )
  }

  private def genTests(owner: ClassSymbol, tests: List[Symbol])(using
      Context
  ): DefDef = {
    val junitdefn = JUnitDefinitions.defnJUnit

    val sym = newSymbol(
      owner,
      junitNme.tests,
      Synthetic | Method,
      MethodType(Nil, defn.ArrayOf(junitdefn.TestMetadataType))
    ).entered

    DefDef(
      sym, {
        val metadata = for (test <- tests) yield {
          val name = Literal(Constant(test.name.mangledString))
          val ignored =
            Literal(Constant(test.hasAnnotation(junitdefn.IgnoreAnnotClass)))
          val testAnnot = test.getAnnotation(junitdefn.TestAnnotClass).get

          val mappedArguments = testAnnot.arguments.flatMap {
            // Since classOf[...] in annotations would not be transformed, grab the resulting class constant here
            case NamedArg(
                  expectedName: SimpleName,
                  TypeApply(Ident(nme.classOf), fstArg :: _)
                ) if expectedName.toString == "expected" =>
              Some(clsOf(fstArg.tpe))
            // The only other valid argument to @Test annotations is timeout
            case NamedArg(timeoutName: TermName, timeoutLiteral: Literal)
                if timeoutName.toString == "timeout" =>
              Some(timeoutLiteral)
            case other => {
              val shownName = other match {
                case NamedArg(name, _) => name.show(using ctx)
                case other             => other.show(using ctx)
              }
              report.error(
                s"$shownName is an unsupported argument for the JUnit @Test annotation in this position",
                other.sourcePos
              )
              None
            }
          }

          val reifiedAnnot =
            resolveConstructor(junitdefn.TestAnnotType, mappedArguments)
          New(junitdefn.TestMetadataType, List(name, ignored, reifiedAnnot))
        }
        JavaSeqLiteral(metadata, TypeTree(junitdefn.TestMetadataType))
      }
    )
  }

  private def genInvokeTest(
      owner: ClassSymbol,
      testClass: ClassSymbol,
      tests: List[Symbol]
  )(using Context): DefDef = {
    val junitdefn = JUnitDefinitions.defnJUnit

    val sym = newSymbol(
      owner,
      junitNme.invokeTest,
      Synthetic | Method,
      MethodType(
        List(junitNme.instance, junitNme.name),
        List(defn.ObjectType, defn.StringType),
        junitdefn.FutureType
      )
    ).entered

    DefDef(
      sym,
      { (paramRefss: List[List[Tree]]) =>
        val List(List(instanceParamRef, nameParamRef)) = paramRefss
        val castInstanceSym = newSymbol(
          sym,
          junitNme.castInstance,
          Synthetic,
          testClass.typeRef,
          coord = owner.span
        )
        Block(
          ValDef(
            castInstanceSym,
            instanceParamRef.cast(testClass.typeRef)
          ) :: Nil,
          tests.foldRight[Tree] {
            val tp = junitdefn.NoSuchMethodExceptionType
            Throw(resolveConstructor(tp, nameParamRef :: Nil))
          } { (test, next) =>
            If(
              Literal(Constant(test.name.mangledString))
                .select(defn.Any_equals)
                .appliedTo(nameParamRef),
              genTestInvocation(testClass, test, ref(castInstanceSym)),
              next
            )
          }
        )
      }
    )
  }

  private def genTestInvocation(
      testClass: ClassSymbol,
      testMethod: Symbol,
      instance: Tree
  )(using Context): Tree = {
    val junitdefn = JUnitDefinitions.defnJUnit

    val resultType = testMethod.info.resultType
    def returnsUnit =
      resultType.isRef(defn.UnitClass) || resultType.isRef(defn.BoxedUnitClass)
    def returnsFuture = resultType.isRef(junitdefn.FutureClass)

    if returnsUnit then {
      val newSuccess =
        ref(junitdefn.SuccessModule_apply).appliedTo(ref(defn.BoxedUnit_UNIT))
      Block(
        instance.select(testMethod).appliedToNone :: Nil,
        ref(junitdefn.FutureModule_successful).appliedTo(newSuccess)
      )
    } else if returnsFuture then {
      instance.select(testMethod).appliedToNone
    } else {
      // We lie in the error message to not expose that we support async testing.
      report.error(
        s"JUnit test must have Unit return type, but got $resultType",
        testMethod.sourcePos
      )
      EmptyTree
    }
  }

  private def genNewInstance(owner: ClassSymbol, testClass: ClassSymbol)(using
      Context
  ): DefDef = {
    val sym = newSymbol(
      owner,
      junitNme.newInstance,
      Synthetic | Method,
      MethodType(Nil, defn.ObjectType)
    ).entered

    DefDef(sym, New(testClass.typeRef, Nil))
  }

  private def castParam(param: Symbol, clazz: Symbol)(using Context): Tree =
    ref(param).cast(clazz.typeRef)

  private def annotatedMethods(owner: ClassSymbol, annot: Symbol)(using
      Context
  ): List[Symbol] =
    owner.info
      .membersBasedOnFlags(Method, EmptyFlags)
      .filter(_.symbol.hasAnnotation(annot))
      .map(_.symbol)
      .toList

  private object junitNme {
    val beforeClass: TermName = termName("beforeClass")
    val afterClass: TermName = termName("afterClass")
    val before: TermName = termName("before")
    val after: TermName = termName("after")
    val testClassMetadata: TermName = termName("testClassMetadata")
    val tests: TermName = termName("tests")
    val invokeTest: TermName = termName("invokeTest")
    val newInstance: TermName = termName("newInstance")

    val instance: TermName = termName("instance")
    val name: TermName = termName("name")
    val castInstance: TermName = termName("castInstance")
  }
}
