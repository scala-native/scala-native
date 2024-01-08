package scala.scalanative.linker

import org.junit.Test
import org.junit.Assert._

import scalanative.nir.{Sig, Type, Global, Rt}
import scala.scalanative.LinkerSpec
import scala.scalanative.linker.LinktimeIntrinsicCallsResolver.ServiceProviderStatus

class ServiceLoaderReachabilityTest extends LinkerSpec {
  val simpleServicesSources = Map(
    "Test.scala" -> """
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
        """.stripMargin,
    "META-INF/services/Service" -> s"""
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
      "Test.scala" -> """
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
        """.stripMargin,
      "META-INF/services/Service" -> s"""
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
}
