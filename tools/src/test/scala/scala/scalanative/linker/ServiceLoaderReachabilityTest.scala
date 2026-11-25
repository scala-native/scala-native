package scala.scalanative.linker

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.LinkerSpec
import scala.scalanative.linker.LinktimeIntrinsicCallsResolver.ServiceProviderStatus
import scalanative.nir.{ControlFlow, Defn, Global, Rt, Sig, Type}

class ServiceLoaderReachabilityTest extends LinkerSpec {
  val simpleServicesSources = Map(
    "Test.scala" ->
      """|
         |trait Service
         |class Foo extends Service
         |object Foo
         |class Bar extends Service
         |class Baz extends Bar
         |
         |package impl {
         | // Different service, should not be reachable
         | trait Service
         | class Baz extends Service
         |}
         |
         |object Test{
         |  def main(args: Array[String]): Unit = {
         |    java.util.ServiceLoader.load(classOf[Service])
         |  }
         |}
         |""".stripMargin,
    "META-INF/services/Service" ->
      s"""|
          |Foo
          |Bar
          |Baz
          |""".stripMargin
  )

  @Test def canFindNotLoadedServiceProviders(): Unit = link(
    "Test",
    simpleServicesSources,
    _.withServiceProviders(Map.empty)
  ) {
    case (_, result) =>
      val providers = result.foundServiceProviders.serviceProviders("Service")
      assertEquals(3, providers.size)
      providers
        .map(_.status)
        .foreach(assertEquals(ServiceProviderStatus.Available, _))
  }

  @Test def canFindLoadedProviders(): Unit = link(
    "Test",
    simpleServicesSources,
    _.withServiceProviders(Map("Service" -> Seq("Foo", "Baz")))
  ) {
    case (_, result) =>
      val providers = result.foundServiceProviders.serviceProviders("Service")
      assertEquals(3, providers.size)

      def provider(name: String) = providers.find(_.name == name).get
      assertEquals(ServiceProviderStatus.Loaded, provider("Foo").status)
      assertEquals(ServiceProviderStatus.Available, provider("Bar").status)
      assertEquals(ServiceProviderStatus.Loaded, provider("Baz").status)
  }

  @Test def canFindMissingProviders(): Unit = link(
    "Test",
    simpleServicesSources ++ Map(
      "Test.scala" ->
        """|
           |trait Service
           |class Foo extends Service
           |class Bar extends Service
           |class Baz extends Bar
           |
           |package services {
           | trait OtherService
           |}
           |
           |object Test{
           |  def main(args: Array[String]): Unit = {
           |    java.util.ServiceLoader.load(classOf[Service])
           |    java.util.ServiceLoader.loadInstalled(classOf[services.OtherService])
           |  }
           |}
           |""".stripMargin,
      "META-INF/services/Service" ->
        s"""|
            |Foo
            |Bar
            |NotImplemented
            |""".stripMargin
    ),
    _.withServiceProviders(
      Map("Service" -> Seq("Foo", "NotImplemented", "NotFound"))
    )
  ) {
    case (_, result) =>
      assertEquals(2, result.foundServiceProviders.serviceProviders.size)

      val providers = result.foundServiceProviders.serviceProviders("Service")
      assertEquals(4, providers.size)

      def provider(name: String) = providers.find(_.name == name).get
      assertEquals(ServiceProviderStatus.Loaded, provider("Foo").status)
      assertEquals(ServiceProviderStatus.Available, provider("Bar").status)
      assertEquals(
        ServiceProviderStatus.UnknownConfigEntry,
        provider("NotFound").status
      )
      assertEquals(
        ServiceProviderStatus.NotFoundOnClasspath,
        provider("NotImplemented").status
      )

      val otherServiceProviders =
        result.foundServiceProviders.serviceProviders("services.OtherService")
      assertTrue(otherServiceProviders.isEmpty)
  }

  @Test def loadInsideTryHasValidControlFlow(): Unit = link(
    "Test",
    simpleServicesSources ++ Map(
      "Test.scala" ->
        """|
           |trait Service
           |class Foo extends Service
           |class Bar extends Service
           |class Baz extends Bar
           |
           |object Test{
           |  def main(args: Array[String]): Unit = {
           |    try {
           |      val it = java.util.ServiceLoader.load(classOf[Service]).iterator()
           |      while (it.hasNext()) println(it.next())
           |    } catch { case _: Throwable => () }
           |  }
           |}
           |""".stripMargin
    ),
    _.withServiceProviders(Map("Service" -> Seq("Foo", "Baz")))
  ) {
    case (_, result) =>
      val providers = result.foundServiceProviders.serviceProviders("Service")
      assertEquals(
        ServiceProviderStatus.Loaded,
        providers.find(_.name == "Foo").get.status
      )
      assertTrue(
        "generated provider loader",
        result.defns.exists(_.name.show.contains("loadProvider_Foo"))
      )
      result.defns.foreach {
        case defn: Defn.Define => ControlFlow.Graph(defn.insts)
        case _                 => ()
      }
  }
}
