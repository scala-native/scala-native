package scala.scalanative
package codegen

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.linker.Class

/* The GC finds the referent of a weak reference in the reference map of its
 * class, after the reference fields and the raw pointer fields.
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
         |    println(ref.get().## + ref.extra.## + h.a.##)
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
    val referent = layout.entries.find(_.name == FieldLayout.WeakReferent).get
    layout.layout.tys(layout.index(referent)).offset.toInt
  }

  @Test def weakReferencesListTheirReferentApart(): Unit =
    optimize("Test", sources) {
      case (config, optimized) =>
        implicit val platform: PlatformInfo = PlatformInfo(config)
        val proxies =
          GenerateReflectiveProxies(optimized.dynimpls, optimized.defns)
        val meta = new Metadata(optimized, config, proxies)

        val holder = referenceMap(meta, "Holder")
        assertEquals("no weak referent section", 2, holder.count(_ == -1))

        val ref = referenceMap(meta, "Ref")
        val offset = referentOffset(meta, "Ref")
        val firstEnd = ref.indexOf(-1)
        val secondEnd = ref.indexOf(-1, firstEnd + 1)
        assertFalse(
          "referent is not marked",
          ref.take(firstEnd).contains(offset)
        )
        assertTrue(
          "other reference fields are marked",
          ref.take(firstEnd).nonEmpty
        )
        assertEquals(
          "referent offset after the raw pointer section",
          Seq(offset, -1),
          ref.drop(secondEnd + 1)
        )
    }
}
