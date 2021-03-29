package scala.scalanative.linker

import org.scalatest.matchers.should.Matchers
import scala.scalanative.LinkerSpec
import scala.scalanative.build.{Config, NativeConfig}
import scala.scalanative.nir.{Global, Sig, Type, Val}

class LinktimeConditionsSpec extends LinkerSpec with Matchers {
  val entry = "Main$"
  private val props =
    s"""package scala.scalanative
       |import scala.scalanative.unsafe.resolvedAtLinktime
       |
       |object linktime {
       |  @resolvedAtLinktime()
       |  final def int = 1
       |  
       |  @resolvedAtLinktime
       |  final def bool = true
       |
       |  @resolvedAtLinktime
       |  final def welcomeMessage = "Hello world"
       |  
       |  @resolvedAtLinktime()
       |  def decimalSeparator = ','
       |  @resolvedAtLinktime()
       |  def float = -1.0f
       |  
       |  object inner{
       |   @resolvedAtLinktime()
       |   def countFrom = 2L
       |   
       |   @resolvedAtLinktime("secret.performance.multiplier")
       |   def performanceMultiplier = 1.0
       |  }
       |}
       |""".stripMargin

  def propName(name: String) = Sig.Generated(name + "_property")

  val linktimeModule     = Global.Top("scala.scalanative.linktime$")
  val innerModule        = Global.Top("scala.scalanative.linktime$inner$")
  val intProp            = linktimeModule.member(propName("int"))
  val boolProp           = linktimeModule.member(propName("bool"))
  val welcomeMessageProp = linktimeModule.member(propName("welcomeMessage"))
  val floatProp          = linktimeModule.member(propName("float"))
  val decimalSepProp     = linktimeModule.member(propName("decimalSeparator"))
  val countFromProp      = innerModule.member(propName("countFrom"))
  val perfMultProp       = innerModule.member(propName("performanceMultiplier"))

  val defaults = Map(
    intProp            -> Val.Int(1),
    boolProp           -> Val.True,
    welcomeMessageProp -> Val.String("Hello world"),
    floatProp          -> Val.Float(-1.0f),
    decimalSepProp     -> Val.Char(','),
    countFromProp      -> Val.Long(2),
    perfMultProp       -> Val.Double(1.0)
  )

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

  private val allPropsUsageUnit = Map(
    "props.scala" -> props,
    "main.scala"  -> allPropsUsage
  )

  "Linktime properties" should "exist in linking results" in {
    link(entry, allPropsUsageUnit) { (_, result) =>
      shouldContainAll(defaults.keySet, result.resolvedVals.keys)
    }
  }

  it should "resolve default values" in {
    linkWithProps(
      "props.scala" -> props,
      "main.scala"  -> allPropsUsage
    )() { (_, result) => shouldContainAll(defaults, result.resolvedVals) }
  }

  it should "resolve values from native config" in {
    case class Entry[T](propertyName: String,
                        value: T,
                        linktimeProp: Global.Member,
                        lintimeValue: Val)

    val entries = Seq(
      Entry("scala.scalanative.linktime.int", 42, intProp, Val.Int(42)),
      Entry("scala.scalanative.linktime.bool", false, boolProp, Val.False),
      Entry("scala.scalanative.linktime.welcomeMessage",
            "Hello native",
            welcomeMessageProp,
            Val.String("Hello native")),
      Entry("scala.scalanative.linktime.float",
            3.14f,
            floatProp,
            Val.Float(3.14f)),
      Entry("scala.scalanative.linktime.decimalSeparator",
            '-',
            decimalSepProp,
            Val.Char('-')),
      Entry("scala.scalanative.linktime.inner.countFrom",
            123456L,
            countFromProp,
            Val.Long(123456L)),
      Entry("secret.performance.multiplier",
            9.99,
            perfMultProp,
            Val.Double(9.99))
    )

    linkWithProps(
      "props.scala" -> props,
      "main.scala"  -> allPropsUsage
    )(entries.map(e => e.propertyName -> e.value): _*) { (_, result) =>
      val expected = for (e <- entries) yield e.linktimeProp -> e.lintimeValue
      shouldContainAll(expected, result.resolvedVals)
    }
  }

  it should "not allow to define property default to null" in {
    assertThrows[scala.scalanative.api.CompilationFailedException] {
      linkWithProps(
        "props.scala" -> """
             |package scala.scalanative
             |object props{
             |   @scalanative.unsafe.resolvedAtLinktime()
             |   def linktimeProperty: Int = null
             |}
             |""".stripMargin,
        "main.scala"  -> """
            |import scala.scalanative.props._
            |object Main {
            |  def main(args: Array[String]): Unit = {
            |    if(linktimeProperty) ???
            |  }
            |}""".stripMargin
      )() { (_, _) => () }
    }
  }

  it should "not allow to define property resolved from null" in {
    assertThrows[scala.scalanative.api.CompilationFailedException] {
      linkWithProps(
        "props.scala" ->
          """pakcage scala.scalanative
            |object props{
            |   @scalanative.unsafe.resolvedAtLinktime(withName = null)
            |   def linktimeProperty: Int = 1
            |}""".stripMargin,
        "main.scala" ->
          """import scala.scalanative.props._
            |object Main {
            |  def main(args: Array[String]): Unit = {
            |    if(linktimeProperty) ???
            |  }
            |}""".stripMargin
      )() { (_, _) => () }
    }
  }

  "Linktime conditions" should "resolve simple conditions" in {
    val pathsRange = 1.to(3)
    /* When using normal (runtime) conditions static reachability analysis
     * would report missing stubs in each branch (in this case 3).
     * When using linktime conditions only branch that fulfilled condition
     * would be actually used, others would be discarded and never used/checked.
     * Based on that only 1 unavailable symbol would be reported (from branch that was taken).
     */
    for (n <- pathsRange)
      linkWithProps(
        "props.scala" -> props,
        "main.scala"  -> s"""
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
      )("scala.scalanative.linktime.int" -> n) { (_, result) =>
        result.unavailable should contain only pathForNumber(n)
      }
  }

  it should "allow to use inequality comparsion" in {
    val property   = "scala.scalanative.linktime.float"
    val pathsRange = 0.until(6)

    for (n <- pathsRange.init)
      linkWithProps(
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
      )(property -> s"$n.0f") { (_, result) =>
        result.unavailable should contain only pathForNumber(n)
      }
  }

  it should "allow to use complex conditions" in {
    val doubleField = "linktime.inner.performanceMultiplier"
    val longField   = "linktime.inner.countFrom"
    val stringField = "stringProp"
    val pathsRange  = 1.to(6)
    val compilationUnit = Map(
      "props.scala"  -> props,
      "props2.scala" -> """
          |package scala.scalanative
          |object props2{
          |   @scalanative.unsafe.resolvedAtLinktime(withName = "prop.string")
          |   def stringProp: String = "null"
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
      (-0.0, "", 1L)              -> 1,
      (1.5, "2", 2L)              -> 2,
      (3.0, "tri", -1L)           -> 3,
      (3.0, "", 3L)               -> 3,
      (4.0, "four", 4L)           -> 4,
      (4.0, "three", 4L)          -> 4,
      (5.0, "", 1234567889L)      -> 5,
      (654321.0, "", 1234567891L) -> 6
    )

    for (((doubleValue, stringValue, longValue), pathNumber) <- cases)
      linkWithProps(compilationUnit.toSeq: _*)(
        "secret.performance.multiplier"              -> doubleValue,
        "prop.string"                                -> stringValue,
        "scala.scalanative.linktime.inner.countFrom" -> longValue
      ) { (_, result) =>
        result.unavailable should contain only pathForNumber(pathNumber)
      }
  }

  it should "handle boolean properties in conditions" in {
    val bool1      = "boolOne"
    val bool2      = "bool2"
    val pathsRange = 1.to(5)

    val cases: List[((Boolean, Boolean), Int)] = List(
      (true, true)  -> 1,
      (true, false) -> 2,
      (false, true) -> 3
    )

    val compilationUnit = Map(
      "props.scala" -> s"""
           |package scala.scalanative
           |object props{
           |   @scalanative.unsafe.resolvedAtLinktime(withName = "prop.bool.1")
           |   def $bool1 = false
           |
           |   @scalanative.unsafe.resolvedAtLinktime(withName = "prop.bool.2")
           |   def $bool2 = false
           |}""".stripMargin,
      "main.scala"  -> s"""
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
      linkWithProps(compilationUnit.toSeq: _*)(
        "prop.bool.1" -> bool1,
        "prop.bool.2" -> bool2
      ) { (_, result) =>
        result.unavailable should contain only pathForNumber(pathNumber)
      }
  }

  it should "not allow to mix link-time and runtime conditions" in {
    assertThrows[scala.scalanative.api.CompilationFailedException] {
      linkWithProps(
        "props.scala" ->
          """package scala.scalantive
            |
            |object props{
            |   @scalanative.unsafe.resolvedAtLinktime()
            |   def linktimeProperty = false
            |
            |   def runtimeProperty = true
            |}
            |""".stripMargin,
        "main.scala" -> """
           |import scala.scalanative.props._
           |object Main {
           |  def main(args: Array[String]): Unit = {
           |    if(linktimeProperty || runtimeProperty) ??? 
           |  }
           |}""".stripMargin
      )() { (_, _) => () }
    }
  }

  private def shouldContainAll[T](expected: Iterable[T], given: Iterable[T]) = {
    val left  = given.toSet
    val right = expected.toSet
    assert((left -- right).isEmpty, "underapproximation")
    assert((right -- left).isEmpty, "overapproximation")
  }

  private def link[T](sources: Map[String, String])(
      fn: (Method, Result) => T): T = {
    link(entry, sources) { (_, result) =>
      implicit val linkerResult: Result = result
      val mainSig =
        Sig.Method("main",
                   Seq(Type.Array(scalanative.nir.Rt.String), Type.Unit))
      val MethodRef(_, mainMethod) = Global.Member(Global.Top(entry), mainSig)
      fn(mainMethod, result)
    }
  }

  private def pathForNumber(n: Int) = {
    Global.Member(owner = Global.Top(entry),
                  sig = Sig.Method(s"path$n", Seq(Type.Unit)))
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

  private def linkWithProps(sources: (String, String)*)(props: (String, Any)*)(
      body: (Config, Result) => Unit): Unit = {
    def setupConfig(config: NativeConfig): NativeConfig = {
      config
        .withLinktimeProperties(props.toMap)
        .withLinkStubs(false)
    }
    link(entry, sources.toMap, setupConfig = setupConfig)(body)
  }
}
