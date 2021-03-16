package scala.scalanative.linker

import org.scalatest.Assertions.assertThrows
import org.scalatest.matchers.should.Matchers
import scala.scalanative.LinkerSpec
import scala.util.Properties.{clearProp, envOrNone, setProp}
import scala.scalanative.nir.{Global, Sig, Type, Val}

class LinktimeConditionsSpec extends LinkerSpec with Matchers {
  val entry = "Main$"
  private val props =
    s"""import scala.scalanative.unsafe.linktimeResolved
       |
       |object linktime {
       |  @linktimeResolved()
       |  final def int = 1
       |  
       |  @linktimeResolved()
       |  final def bool = true
       |
       |  @linktimeResolved(fromProperty = null, fromEnv = "LINKTIME_MSG")
       |  final def welcomeMessage = "Hello world"
       |  
       |  @linktimeResolved()
       |  def decimalSeparator = ','
       |  @linktimeResolved()
       |  def float = -1.0f
       |  
       |  object inner{
       |   @linktimeResolved()
       |   def countFrom = 2L
       |   
       |   @linktimeResolved("secret.performance.multiplier")
       |   def performanceMultiplier = 1.0
       |  }
       |}
       |""".stripMargin

  def propName(name: String) = Sig.Generated(name + "_property")

  val linktimeModule     = Global.Top("linktime$")
  val innerModule        = Global.Top("linktime$inner$")
  val intProp            = linktimeModule.member(propName("int"))
  val boolProp           = linktimeModule.member(propName("bool"))
  val welcomeMessageProp = linktimeModule.member(propName("welcomeMessage"))
  val floatProp          = linktimeModule.member(propName("float"))
  val decimalSepProp     = linktimeModule.member(propName("decimalSeparator"))
  val countFromProp      = innerModule.member(propName("countFrom"))
  val perfMultProp       = innerModule.member(propName("performanceMultiplier"))

  val defaultSep = ','
  val defaults = Map(
    intProp            -> Val.Int(1),
    boolProp           -> Val.True,
    welcomeMessageProp -> Val.String("Hello world"),
    floatProp          -> Val.Float(-1.0f),
    decimalSepProp     -> Val.Char(defaultSep),
    countFromProp      -> Val.Long(2),
    perfMultProp       -> Val.Double(1.0)
  )

  val allProps      = defaults.keySet
  val allPropsUsage = s"""
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

  val defaultPropsOnly =
    props.replaceAll("@linktimeResolved(.*)", "@linktimeResolved(null, null)")

  "Linktime properties" should "exist in linking results" in {
    link(entry, allPropsUsageUnit) { (_, result) =>
      shouldContainAll(allProps, result.resolvedVals.keys)
    }
  }

  // Is based on assumption that @linktimeResolved annotation
  // with from{Property, Env} set to null does block this channel
  // from resolving value. We test its true by checking that known
  // system properies and env variables were not used to resolve value
  it should "resolve default values" in {
    withProps(
      "linktime.int"                  -> "10",
      "secret.performance.multiplier" -> "2.0"
    ) {
      val msgOpt = envOrNone("LINKTIME_MSG")
      msgOpt should not be empty
      msgOpt shouldNot contain(defaultSep)

      link(entry,
           Map("props.scala" -> defaultPropsOnly,
               "main.scala"  -> allPropsUsage)) { (_, result) =>
        shouldContainAll(defaults, result.resolvedVals)
      }
    }
  }

  it should "resolve values from env variables" in {
    link(entry, allPropsUsageUnit) { (_, result) =>
      val vals        = result.resolvedVals
      val expectedMsg = "scala-native"
      envOrNone("LINKTIME_MSG") should contain(expectedMsg)
      vals(welcomeMessageProp) shouldEqual Val.String(expectedMsg)

      val expectedSeperator = '_'
      envOrNone("LINKTIME_DECIMALSEPARATOR") should contain(
        expectedSeperator.toString)
      vals(decimalSepProp) shouldEqual Val.Char(expectedSeperator)
    }
  }

  it should "resolve values from system properties" in {
    case class Entry(propertyName: String,
                     value: String,
                     linktimeProp: Global.Member,
                     lintimeValue: Val)

    val entries = Seq(
      Entry("linktime.int", "42", intProp, Val.Int(42)),
      Entry("linktime.bool", "false", boolProp, Val.False),
      Entry("linktime.welcomeMessage",
            "Hello native",
            welcomeMessageProp,
            Val.String("Hello native")),
      Entry("linktime.float", "3.14", floatProp, Val.Float(3.14f)),
      Entry("linktime.decimalSeparator", "-", decimalSepProp, Val.Char('-')),
      Entry("linktime.inner.countFrom",
            "123456",
            countFromProp,
            Val.Long(123456L)),
      Entry("secret.performance.multiplier",
            "9.99",
            perfMultProp,
            Val.Double(9.99))
    )

    val noDisabledProps = Map(
      "props.scala" -> props
        .replaceAll("null", "scala.scalanative.runtime.intrinsic"),
      "main.scala" -> allPropsUsage
    )

    withProps(entries.map(e => e.propertyName -> e.value): _*) {
      link(entry, noDisabledProps) { (_, result) =>
        val expected = for (e <- entries) yield e.linktimeProp -> e.lintimeValue
        shouldContainAll(expected, result.resolvedVals)
      }
    }
  }

  it should "prefer system property over env variable" in {
    withProps("linktime.decimalSeparator" -> "p") {
      envOrNone("LINKTIME_DECIMALSEPARATOR") should contain("_")

      link(entry, allPropsUsageUnit) { (_, result) =>
        result.resolvedVals(decimalSepProp) shouldEqual Val.Char('p')
      }
    }
  }

  it should "not allow to define property default to null" in {
    assertThrows[scala.scalanative.api.CompilationFailedException] {
      link(s"""
              |object Main {
              |   @scalanative.unsafe.linktimeResolved()
              |   def linktimeProperty: Int = null
              |   
              |  def main(args: Array[String]): Unit = {
              |    if(linktimeProperty) ??? 
              |  }
              |}""".stripMargin) { (_, _) => () }
    }
  }

  "Linktime conditions" should "resolve simple conditions" in {
    val pathsRange = 1.to(3)
    val compilationUnit = Map(
      "props.scala" -> props,
      "main.scala"  -> s"""
                          |object Main {
                          |  ${pathStrings(pathsRange)}
                          |  
                          |  def main(args: Array[String]): Unit = {
                          |    if(linktime.int == 1) path1()
                          |    else if (linktime.int == 2) path2()
                          |    else path3()
                          |  }
                          |}""".stripMargin
    )
    /* When using normal (runtime) conditions static reachability analysis
     * would report missing stubs in each branch (in this case 3).
     * When using linktime conditions only branch that fulfilled condition
     * would be actually used, others would be discarded and never used/checked.
     * Based on that only 1 unavailable symbol would be reported (from branch that was taken).
     */
    for (n <- pathsRange) withProps("linktime.int" -> n.toString) {
      link(compilationUnit) { (main, result) =>
        result.unavailable should contain only pathForNumber(n)
      }
    }
  }

  it should "allow to use inequality comparsion" in {
    val property   = "linktime.float"
    val pathsRange = 0.until(6)

    val compilationUnit = Map(
      "props.scala" -> props,
      "main.scala"  -> s"""
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
    )
    for (n <- pathsRange.init)
      withProps(property -> s"$n.0f") {
        link(compilationUnit) { (main, result) =>
          result.unavailable should contain only pathForNumber(n)
        }
      }
  }

  it should "allow to use complex conditions" in {
    val double     = "linktime.inner.performanceMultiplier"
    val long       = "linktime.inner.countFrom"
    val string     = "stringProp"
    val pathsRange = 1.to(6)

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

    val compilationUnit = Map(
      "props.scala" -> props,
      "main.scala"  -> s"""
                          |object Main {
                          |   @scalanative.unsafe.linktimeResolved(fromProperty = "prop.string")
                          |   def stringProp: String = "null"
                          |
                          |  ${pathStrings(pathsRange)}
                          |  def main(args: Array[String]): Unit = {
                          |    if($double == -1.0 || $string == "one" || $long == 1) path1()
                          |     else if($double >= 1 && $long <= 2 && $string == "2") path2()
                          |     else if(($double == 3.0 && $long == 3) || $string == "tri") path3()
                          |     else if(($string != "three" || $long > 3) && $double <= 4.0) path4()
                          |     else if(($string != null && $long < 1234567890) && ($double >= -12345.789 && $double <= 12345.789)) path5()
                          |     else path6()
                          |  }
                          |}""".stripMargin
    )
    for (((double, string, long), pathNumber) <- cases)
      withProps(
        "secret.performance.multiplier" -> double.toString,
        "prop.string"                   -> string,
        "linktime.inner.countFrom"      -> long.toString
      ) {
        link(compilationUnit) { (_, result) =>
          result.unavailable should contain only pathForNumber(pathNumber)
        }
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
      "props.scala" -> props,
      "main.scala"  -> s"""
                          |object Main {
                          |   @scalanative.unsafe.linktimeResolved(fromProperty = "prop.bool.1")
                          |   def $bool1 = false
                          |
                          |   @scalanative.unsafe.linktimeResolved(fromProperty = "prop.bool.2")
                          |   def $bool2 = false
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
      withProps(
        "prop.bool.1" -> bool1.toString,
        "prop.bool.2" -> bool2.toString
      ) {
        link(compilationUnit) { (_, result) =>
          result.unavailable should contain only pathForNumber(pathNumber)
        }
      }
  }

  it should "not allow to mix link-time and runtime conditions" in {
    assertThrows[scala.scalanative.api.CompilationFailedException] {
      link(s"""
           |object Main {
           |   @scalanative.unsafe.linktimeResolved()
           |   def linktimeProperty = false
           |   
           |   def runtimeProperty = true
           |   
           |  def main(args: Array[String]): Unit = {
           |    if(linktimeProperty || runtimeProperty) ??? 
           |  }
           |}""".stripMargin) { (_, _) => () }
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

  private def withProps(props: (String, String)*)(body: => Any): Unit = {
    props.foreach {
      case (key, value) =>
        if (value.isEmpty) ()
        else setProp(key, value)
    }
    try { body }
    finally props.foreach {
      case (propName, _) => clearProp(propName)
    }
  }
}
