package scala.scalanative
package codegen

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.linker.{Class, Trait}

class DispatchTableOrderTest extends CodeGenSpec {

  private val sources = Map(
    "Test.scala" ->
      """|import scala.language.reflectiveCalls
         |trait T {
         |  def tz(): Int; def ta(): Int; def tm(): Int; def tb(): Int
         |}
         |class Base extends T {
         |  def tz() = 1; def ta() = 2; def tm() = 3; def tb() = 4
         |  def vz() = 1; def va() = 2; def vm() = 3; def vb() = 4
         |}
         |class Sub1 extends Base {
         |  override def tz() = 5; override def ta() = 6
         |  override def tm() = 7; override def tb() = 8
         |  override def vz() = 5; override def va() = 6
         |  override def vm() = 7; override def vb() = 8
         |}
         |class Sub2 extends Base {
         |  override def tz() = 9; override def ta() = 10
         |  override def tm() = 11; override def tb() = 12
         |  override def vz() = 9; override def va() = 10
         |  override def vm() = 11; override def vb() = 12
         |}
         |object Test {
         |  def viaT(t: T): Int = t.tz() + t.ta() + t.tm() + t.tb()
         |  def main(args: Array[String]): Unit = {
         |    val b: Base = if (args.isEmpty) new Sub1 else new Sub2
         |    val r: { def zz(): Int; def aa(): Int } =
         |      new { def zz() = 1; def aa() = 2 }
         |    println(b.vz() + b.va() + b.vm() + b.vb())
         |    println(viaT(b) + viaT(new Base))
         |    println(r.zz() + r.aa())
         |  }
         |}
         |""".stripMargin
  )

  private def assertSorted(what: String, sigs: Seq[nir.Sig]): Unit =
    assertEquals(what, sigs.sortBy(_.mangle), sigs)

  @Test def dispatchTablesDoNotFollowHashOrder(): Unit =
    link("Test", sources) {
      case (config, linked) =>
        implicit val platform: PlatformInfo = PlatformInfo(config)
        val proxies = GenerateReflectiveProxies(linked.dynimpls, linked.defns)
        val meta = new Metadata(linked, config, proxies)
        def info(name: String) = linked.infos(nir.Global.Top(name))

        val base = info("Base").asInstanceOf[Class]
        val vtable = meta.vtable(base)
        val inherited = base.parent.fold(0)(meta.vtable(_).value match {
          case nir.Val.ArrayValue(_, values) => values.size
          case _                             => 0
        })
        val own = base.calls.toSeq
          .filter(vtable.index(_) >= inherited)
          .sortBy(vtable.index)
        assertTrue("Base has own vtable slots", own.size >= 4)
        assertSorted("vtable slots", own)

        val trt = info("T").asInstanceOf[Trait]
        assertTrue("T has itable methods", trt.methods.size >= 4)
        assertSorted("itable methods", trt.methods)

        assertTrue("program uses dynamic calls", linked.dynsigs.size >= 2)
        assertSorted("dynamic signatures", linked.dynsigs)
        assertEquals(
          "dynamic implementations",
          linked.dynimpls.sorted(nir.Global.globalOrdering),
          linked.dynimpls
        )

        val traitIds = meta.traits.map(meta.ids)
        val sortedIds = meta.traits
          .sortBy[nir.Global](_.name)
          .map(meta.ids)
        assertEquals("traits visited in name order", sortedIds, traitIds)
    }
}
