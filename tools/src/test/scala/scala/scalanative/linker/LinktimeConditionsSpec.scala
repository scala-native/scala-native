package scala.scalanative.linker

import org.junit.Test
import org.junit.Assert._

import scala.scalanative.OptimizerSpec
import scala.scalanative.build.{Config, NativeConfig}
import scala.scalanative.nir.{Global, Sig, Type, Val, Rt, Unmangle}
import scala.util._

class LinktimeConditionsSpec extends OptimizerSpec {
  val entry = "Main"
  val module = "Main$"
  private val props =
    s"""package scala.scalanative
       |import scala.scalanative.unsafe.{resolvedAtLinktime, resolved}
       |
       |object linktime {
       |  @resolvedAtLinktime("int")
       |  final def int: Int = resolved
       |  
       |  @resolvedAtLinktime("bool")
       |  final def bool: Boolean = resolved
       |
       |  @resolvedAtLinktime("welcomeMessage")
       |  final def welcomeMessage: String = resolved
       |  
       |  @resolvedAtLinktime("decimalSeparator")
       |  def decimalSeparator: Char = resolved
       |  @resolvedAtLinktime("float")
       |  def float: Float = resolved
       |
       |  object inner{
       |    @resolvedAtLinktime("inner.countFrom")
       |    def countFrom: Long = resolved
       |   
       |    @resolvedAtLinktime("secret.performance.multiplier")
       |    def performanceMultiplier: Double = resolved
       |  }
       |}
       |""".stripMargin

  val allPropsUsage = s"""
                       |import scala.scalanative.linktime
                       |object Main {
                       |  def main(args: Array[String]): Unit = {
                       |    linktime.int
                       |    linktime.bool
                       |    linktime.welcomeMessage
                       |    linktime.decimalSeparator
                       |    linktime.float
                       |    linktime.inner.countFrom
                       |    linktime.inner.performanceMultiplier
                       |  }
                       |}""".stripMargin

  case class Entry[T](propertyName: String, value: T, linktimeValue: Val)

  val ignoredPropertiesNames = {
    val linktimeInfo = "scala.scalanative.meta.linktimeinfo"
    Set(
      s"$linktimeInfo.asanEnabled",
      s"$linktimeInfo.is32BitPlatform",
      s"$linktimeInfo.isMultithreadingEnabled",
      s"$linktimeInfo.isWeakReferenceSupported",
      s"$linktimeInfo.target.arch",
      s"$linktimeInfo.target.vendor",
      s"$linktimeInfo.target.os",
      s"$linktimeInfo.target.env",
      s"$linktimeInfo.contendedPaddingWidth"
    )
  }

  private def isMangledMethod(name: String) = Try(
    Unmangle.unmangleGlobal(name)
  ) match {
    case Success(Global.Member(_, sig)) => sig.isMethod
    case _                              => false
  }

  // Ignore blacklisted linktime properties which are enfored by list of default properties and
  // ignore all evaluated functions using linktime condtions
  def isIgnoredLinktimeProperty(name: String) =
    ignoredPropertiesNames.contains(name) || isMangledMethod(name)

  val defaultEntries = {
    Seq(
      Entry("int", 42, Val.Int(42)),
      Entry("bool", false, Val.False),
      Entry("welcomeMessage", "Hello native", Val.String("Hello native")),
      Entry("float", 3.14f, Val.Float(3.14f)),
      Entry("decimalSeparator", '-', Val.Char('-')),
      Entry("inner.countFrom", 123456L, Val.Long(123456L)),
      Entry("secret.performance.multiplier", 9.99, Val.Double(9.99))
    )
  }
  val defaultProperties = defaultEntries.map(e => e.propertyName -> e.value)

  @Test def resolvesLinktimeValues(): Unit = {
    linkWithProps(
      "props.scala" -> props,
      "main.scala" -> allPropsUsage
    )(defaultProperties: _*) { (_, result) =>
      def normalized(seq: Iterable[String]): Set[String] =
        seq.toSet.filterNot(isIgnoredLinktimeProperty)
      shouldContainAll(
        normalized(defaultEntries.map(_.propertyName)),
        normalized(result.resolvedVals.keys)
      )
    }
  }

  @Test def resolvesFromConfig(): Unit = {
    linkWithProps(
      "props.scala" -> props,
      "main.scala" -> allPropsUsage
    )(defaultProperties: _*) { (_, result) =>
      def normalized(elems: Map[String, Val]): Map[String, Val] =
        elems.filter { case (key, _) => !isIgnoredLinktimeProperty(key) }
      val expected =
        defaultEntries.map { e => e.propertyName -> e.linktimeValue }
      shouldContainAll(
        normalized(expected.toMap),
        normalized(result.resolvedVals.toMap)
      )
    }
  }

  @Test def resolveSimpleConditions(): Unit = {
    val pathsRange = 1.to(3)
    /* When using normal (runtime) conditions static reachability analysis
     * would report missing stubs in each branch (in this case 3).
     * When using linktime conditions only branch that fulfilled condition
     * would be actually used, others would be discarded and never used/checked.
     * Based on that only 1 unavailable symbol would be reported (from branch that was taken).
     */
    for (n <- pathsRange)
      doesNotLinkWithProps(
        "props.scala" -> props,
        "main.scala" -> s"""
                          |import scala.scalanative.linktime
                          |object Main {
                          |  ${pathStrings(pathsRange)}
                          |  
                          |  def main(args: Array[String]): Unit = {
                          |    if(linktime.int == 1) path1()
                          |    else if (linktime.int == 2) path2()
                          |    else path3()
                          |  }
                          |}""".stripMargin
      )("int" -> n) {
        case (_, result: ReachabilityAnalysis.UnreachableSymbolsFound) =>
          assertTrue(
            n.toString,
            (result.unavailable.map(_.symbol).toSet - pathForNumber(n)).isEmpty
          )
      }
  }

  @Test def inequalityComparsion(): Unit = {
    val property = "scala.scalanative.linktime.float"
    val pathsRange = 0.until(6)

    for (n <- pathsRange.init)
      doesNotLinkWithProps(
        "props.scala" -> props,
        "main.scala" ->
          s"""
          |import scala.scalanative.linktime
          |object Main {
          |  ${pathStrings(pathsRange)}
          |  def main(args: Array[String]): Unit = {
          |    if($property != 0.0f) {
          |       if($property <= 1.0f) path1()
          |       else if($property < 2.9f) path2()
          |       else if($property > 3.9f) path4()
          |       else if($property >= 3.0f) path3()
          |       else () // should be unreachable
          |    } else path0()
          |  }
          |}""".stripMargin
      )("float" -> n.toFloat) {
        case (_, result: ReachabilityAnalysis.UnreachableSymbolsFound) =>
          assertTrue(
            n.toString,
            (result.unavailable.map(_.symbol).toSet - pathForNumber(n)).isEmpty
          )
      }
  }

  @Test def complexConditions(): Unit = {
    val doubleField = "linktime.inner.performanceMultiplier"
    val longField = "linktime.inner.countFrom"
    val stringField = "stringProp"
    val pathsRange = 1.to(6)
    val compilationUnit = Map(
      "props.scala" -> props,
      "props2.scala" -> """
          |package scala.scalanative
          |object props2{
          |   @scalanative.unsafe.resolvedAtLinktime(withName = "prop.string")
          |   def stringProp: String = scala.scalanative.unsafe.resolved
          |}
          |""".stripMargin,
      "main.scala" ->
        s"""import scala.scalanative.props2._
           |import scala.scalanative.linktime
           |
           |object Main {
           |  ${pathStrings(pathsRange)}
           |  def main(args: Array[String]): Unit = {
           |    if($doubleField == -1.0 || $stringField == "one" || $longField == 1) path1()
           |     else if($doubleField >= 1 && $longField <= 2 && $stringField == "2") path2()
           |     else if(($doubleField == 3.0 && $longField == 3) || $stringField == "tri") path3()
           |     else if(($stringField != "three" || $longField > 3) && $doubleField <= 4.0) path4()
           |     else if(($stringField != null && $longField < 1234567890) && ($doubleField >= -12345.789 && $doubleField <= 12345.789)) path5()
           |     else path6()
           |  }
           |}""".stripMargin
    )

    val cases: List[((Double, String, Long), Int)] = List(
      (-0.0, "none", 1L) -> 1,
      (1.5, "2", 2L) -> 2,
      (3.0, "tri", -1L) -> 3,
      (3.0, "None", 3L) -> 3,
      (4.0, "four", 4L) -> 4,
      (4.0, "three", 4L) -> 4,
      (5.0, "None", 1234567889L) -> 5,
      (654321.0, "None", 1234567891L) -> 6
    )

    for (((doubleValue, stringValue, longValue), pathNumber) <- cases)
      doesNotLinkWithProps(compilationUnit.toSeq: _*)(
        "secret.performance.multiplier" -> doubleValue,
        "prop.string" -> stringValue,
        "inner.countFrom" -> longValue
      ) {
        case (_, result: ReachabilityAnalysis.UnreachableSymbolsFound) =>
          assertTrue(
            (result.unavailable.map(_.symbol).toSet -
              pathForNumber(pathNumber)).isEmpty
          )
      }
  }

  @Test def booleanPropertiesInConditions(): Unit = {
    val bool1 = "boolOne"
    val bool2 = "bool2"
    val pathsRange = 1.to(5)

    val cases: List[((Boolean, Boolean), Int)] = List(
      (true, true) -> 1,
      (true, false) -> 2,
      (false, true) -> 3
    )

    val compilationUnit = Map(
      "props.scala" -> s"""
           |package scala.scalanative
           |object props{
           |   @scalanative.unsafe.resolvedAtLinktime(withName = "prop.bool.1")
           |   def $bool1: Boolean = scala.scalanative.unsafe.resolved
           |
           |   @scalanative.unsafe.resolvedAtLinktime(withName = "prop.bool.2")
           |   def $bool2: Boolean = scala.scalanative.unsafe.resolved
           |}""".stripMargin,
      "main.scala" -> s"""
        |import scala.scalanative.props._
        |object Main {
        |
        |  ${pathStrings(pathsRange)}
        |  def main(args: Array[String]): Unit = {
        |    if($bool1 && $bool2 == true) path1()
        |     else if($bool1 && !$bool2) path2()
        |     else if($bool1 == false || $bool2) path3()
        |     else path4()
        |  }
        |}""".stripMargin
    )

    for (((bool1, bool2), pathNumber) <- cases)
      doesNotLinkWithProps(compilationUnit.toSeq: _*)(
        "prop.bool.1" -> bool1,
        "prop.bool.2" -> bool2
      ) {
        case (_, result: ReachabilityAnalysis.UnreachableSymbolsFound) =>
          assertTrue(
            (result.unavailable.map(_.symbol).toSet -
              pathForNumber(pathNumber)).isEmpty
          )
      }
  }

  @Test def referenceLinktimeConditionAtRuntime(): Unit = {
    linkWithProps(
      "props.scala" ->
        """package scala.scalanative
            |
            |object props{
            |   @scalanative.unsafe.resolvedAtLinktime("prop")
            |   def linktimeProperty: Boolean = scala.scalanative.unsafe.resolved
            |}
            |""".stripMargin,
      "main.scala" -> """
                          |import scala.scalanative.props._
                          |object Main {
                          |  def main(args: Array[String]): Unit = {
                          |    println(linktimeProperty)
                          |  }
                          |}""".stripMargin
    )("prop" -> true) { (_, result) =>
      assertEquals(Val.True, result.resolvedVals("prop"))
    }
  }

  @Test def inlineLinktimeValue(): Unit = {
    optimizeWithProps(
      "props.scala" ->
        """package scala.scalanative
          |
          |object props{
          |   @scalanative.unsafe.resolvedAtLinktime("prop")
          |   def linktimeProperty: Boolean = scala.scalanative.unsafe.resolved
          |}
          |""".stripMargin,
      "main.scala" -> """
                        |import scala.scalanative.props._
                        |object Main {
                        |  @scalanative.annotation.alwaysinline
                        |  def prop() = linktimeProperty
                        |  def main(args: Array[String]): Unit = {
                        |    println(prop())
                        |  }
                        |}""".stripMargin
    )("prop" -> true) { (_, result) =>
      // Check if compiles and does not fail to optimize
      assertTrue(result.isSuccessful)
    }
  }

  @Test def methodsBasedOnLinktimeValues(): Unit = {
    linkWithProps(
      "props.scala" ->
        """package scala.scalanative
          |
          |object props{
          |   @scalanative.unsafe.resolvedAtLinktime("os")
          |   def os: String = scala.scalanative.unsafe.resolved
          | 
          |   @scalanative.unsafe.resolvedAtLinktime
          |   def isWindows: Boolean = os == "windows"
          |    
          |   @scalanative.unsafe.resolvedAtLinktime
          |   def isMac: Boolean = {
          |     @scalanative.unsafe.resolvedAtLinktime
          |     def vendor = "apple"
          |     
          |     os == "darwin" && vendor == "apple"
          |   }
          |
          |   @scalanative.unsafe.resolvedAtLinktime
          |   def dynLibExt: String = 
          |     if(isWindows) ".dll"
          |     else if(isMac) ".dylib"
          |     else ".so"
          |}
          |""".stripMargin,
      "main.scala" -> """
          |import scala.scalanative.props._
          |object Main {
          |  def main(args: Array[String]): Unit = {
          |    println(dynLibExt)
          |  }
          |}""".stripMargin
    )("os" -> "darwin") { (_, result) =>
      val Props = Global.Top("scala.scalanative.props$")
      def calculatedVal(
          name: String,
          ty: Type,
          scope: Sig.Scope = Sig.Scope.Public
      ) = {
        val global = Props.member(Sig.Method(name, Seq(ty), scope))
        val mangled = scalanative.nir.Mangle(global)
        result.resolvedVals.get(mangled)
      }
      assertEquals(Val.String("darwin"), result.resolvedVals("os"))
      // nested method is defined as private
      assertTrue(
        calculatedVal("vendor$1", Rt.String, Sig.Scope.Private(Props))
          .contains(Val.String("apple"))
      )
      assertTrue(calculatedVal("isWindows", Type.Bool).contains(Val.False))
      assertTrue(calculatedVal("isMac", Type.Bool).contains(Val.True))
      assertTrue(
        calculatedVal("dynLibExt", Rt.String).contains(Val.String(".dylib"))
      )
    }
  }

  private def shouldContainAll[T](
      expected: Iterable[T],
      actual: Iterable[T]
  ) = {
    val left = actual.toSet
    val right = expected.toSet
    assertTrue("underapproximation", (left -- right).isEmpty)
    assertTrue("overapproximation", (right -- left).isEmpty)
  }

  private def link[T](
      sources: Map[String, String]
  )(fn: (Method, ReachabilityAnalysis.Result) => T): T = {
    link(entry, sources) { (_, result) =>
      implicit val linkerResult: ReachabilityAnalysis.Result = result
      val MethodRef(_, mainMethod) =
        Global.Top(entry).member(Rt.ScalaMainSig): @unchecked
      fn(mainMethod, result)
    }
  }

  private def pathForNumber(n: Int) = {
    Global.Member(
      owner = Global.Top(module),
      sig = Sig.Method(s"path$n", Seq(Type.Unit))
    )
  }

  private def pathStrings(range: Range) = {
    range
      .map { n =>
        s"""
         |@scalanative.annotation.stub
         |def path$n(): Unit = ???
         |""".stripMargin
      }
      .mkString("\n")
  }

  private def mayLinkWithProps(
      sources: (String, String)*
  )(
      props: (String, Any)*
  )(body: (Config, ReachabilityAnalysis) => Unit): Unit = {
    def setupConfig(config: NativeConfig): NativeConfig = {
      config
        .withLinktimeProperties(props.toMap)
        .withTargetTriple("x86_64-unknown-linux-gnu")
        .withLinkStubs(false)
    }
    link(entry, sources.toMap, setupConfig = setupConfig)(body)
  }
  private def linkWithProps(
      sources: (String, String)*
  )(
      props: (String, Any)*
  )(body: (Config, ReachabilityAnalysis.Result) => Unit): Unit = {
    mayLinkWithProps(sources: _*)(props: _*) {
      case (config, analysis: ReachabilityAnalysis.Result) =>
        body(config, analysis)
      case _ => fail("Failed to link"); scala.scalanative.util.unreachable
    }
  }

  private def doesNotLinkWithProps(
      sources: (String, String)*
  )(props: (String, Any)*)(
      body: (Config, ReachabilityAnalysis.UnreachableSymbolsFound) => Unit
  ): Unit = {
    mayLinkWithProps(sources: _*)(props: _*) {
      case (config, analysis: ReachabilityAnalysis.UnreachableSymbolsFound) =>
        body(config, analysis)
      case _ =>
        fail("Expected code to not link"); scala.scalanative.util.unreachable
    }
  }

  private def optimizeWithProps(
      sources: (String, String)*
  )(
      props: (String, Any)*
  )(body: (Config, ReachabilityAnalysis.Result) => Unit): Unit = {
    def setupConfig(config: NativeConfig): NativeConfig = {
      config
        .withLinktimeProperties(props.toMap)
        .withTargetTriple("x86_64-unknown-linux-gnu")
        .withLinkStubs(false)
        .withOptimize(true)
        .withMode(scalanative.build.Mode.releaseFull)
    }
    optimize(entry, sources.toMap, setupConfig = setupConfig)(body)
  }
}
