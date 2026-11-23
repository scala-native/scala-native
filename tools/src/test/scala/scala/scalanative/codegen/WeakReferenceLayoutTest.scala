package scala.scalanative
package codegen

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.linker.Class

/* The GC finds the referent of every reference object right after the object
 * header, and never marks it through the reference map.
 */
class WeakReferenceLayoutTest extends CodeGenSpec {

  private val sources = Map(
    "Test.scala" ->
      """|class Holder {
         |  var a: AnyRef = null
         |}
         |// Only a subclass is allocated, never WeakReference itself
         |class Ref(value: AnyRef, val extra: AnyRef)
         |    extends java.lang.ref.WeakReference[AnyRef](value)
         |object Test {
         |  def main(args: Array[String]): Unit = {
         |    val h = new Holder
         |    h.a = args
         |    val ref = new Ref(h, args)
         |    val soft = new java.lang.ref.SoftReference[AnyRef](h)
         |    val queue = new java.lang.ref.ReferenceQueue[AnyRef]()
         |    val phantom = new java.lang.ref.PhantomReference[AnyRef](h, queue)
         |    println(ref.get().## + ref.extra.## + h.a.## + soft.get().##)
         |    println(phantom.refersTo(h))
         |  }
         |}
         |""".stripMargin
  )

  private def referenceMap(meta: Metadata, name: String): Seq[Int] = {
    val cls = meta.analysis.infos(nir.Global.Top(name)).asInstanceOf[Class]
    meta.layout(cls).referenceOffsetsValue match {
      case nir.Val.Const(nir.Val.ArrayValue(_, values)) =>
        values.collect { case nir.Val.Int(value) => value }
      case other => fail(s"Unexpected reference map $other"); Nil
    }
  }

  private def referentOffset(meta: Metadata, name: String): Int = {
    val cls = meta.analysis.infos(nir.Global.Top(name)).asInstanceOf[Class]
    val layout = meta.layout(cls)
    val referent = layout.entries.find(_.name == FieldLayout.Referent).get
    layout.layout.tys(layout.index(referent)).offset.toInt
  }

  @Test def referentFollowsTheHeader(): Unit =
    optimize("Test", sources) {
      case (config, optimized) =>
        implicit val platform: PlatformInfo = PlatformInfo(config)
        val proxies =
          GenerateReflectiveProxies(optimized.dynimpls, optimized.defns)
        val meta = new Metadata(optimized, config, proxies)

        val headerSize =
          MemoryLayout.sizeOf(meta.layouts.ObjectHeader.layout)(platform)
        val references = Seq(
          "Ref",
          "java.lang.ref.SoftReference",
          "java.lang.ref.PhantomReference"
        )
        for (name <- references)
          assertEquals(
            s"referent of $name follows the header",
            headerSize,
            referentOffset(meta, name).toLong
          )

        def marked(name: String) = {
          val map = referenceMap(meta, name)
          assertEquals(s"$name has two map sections", 2, map.count(_ == -1))
          map.take(map.indexOf(-1))
        }
        val offset = referentOffset(meta, "Ref")
        val ref = marked("Ref")
        assertFalse("weak referent is not marked", ref.contains(offset))
        assertTrue("other reference fields are marked", ref.nonEmpty)
        for (name <- references.tail)
          assertFalse(
            s"referent of $name is not marked",
            marked(name).contains(offset)
          )
        assertEquals(
          "plain classes keep two sections",
          Seq(-1, -1),
          referenceMap(meta, "Holder").drop(1)
        )
    }
}
