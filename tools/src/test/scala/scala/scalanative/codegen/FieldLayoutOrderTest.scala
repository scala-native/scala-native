package scala.scalanative
package codegen

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.build.{Config, ScalaNative}
import scala.scalanative.linker.{Class, Link, ReachabilityAnalysis}
import scala.scalanative.util.Scope

class FieldLayoutOrderTest extends CodeGenSpec {

  private val HolderName = nir.Global.Top("Holder")

  private val sources = Map(
    "Test.scala" ->
      """|class Holder {
         |  var a: Long = 0L
         |  var b: AnyRef = null
         |  var c: Int = 0
         |  var d: Long = 0L
         |  var e: AnyRef = null
         |  var f: Byte = 0
         |  var g: AnyRef = null
         |}
         |object Test {
         |  def main(args: Array[String]): Unit = {
         |    val h = new Holder
         |    h.b = args
         |    h.c = args.length
         |    h.e = h.b
         |    h.g = h.e
         |    h.f = h.c.toByte
         |    h.a = h.c.toLong
         |    h.d = h.a + 1
         |    println(h.a + h.c + h.d + h.f + h.b.## + h.e.## + h.g.##)
         |  }
         |}
         |""".stripMargin
  )

  private case class Observed(
      linkedFields: Seq[nir.Global],
      optimizedFields: Seq[nir.Global],
      layoutEntries: Seq[nir.Global],
      size: Long,
      referenceOffsets: Seq[Int]
  )

  private def holder(analysis: ReachabilityAnalysis.Result): Class =
    analysis.infos(HolderName).asInstanceOf[Class]

  private def reach(config: Config, extraEntries: Seq[nir.Global])(implicit
      in: Scope
  ): ReachabilityAnalysis.Result =
    Link(config, ScalaNative.entries(config) ++ extraEntries) match {
      case result: ReachabilityAnalysis.Result => result
      case _ => fail("Expected code to link"); util.unreachable
    }

  private def observe(config: Config, fieldOrder: Seq[nir.Global])(implicit
      in: Scope
  ): Observed = {
    val linked = reach(config, fieldOrder)
    val optimized =
      Await.result(ScalaNative.optimize(config, linked), Duration.Inf)
    implicit val platform: PlatformInfo = PlatformInfo(config)
    val proxies =
      GenerateReflectiveProxies(optimized.dynimpls, optimized.defns)
    implicit val meta: Metadata = new Metadata(optimized, config, proxies)
    val layout = meta.layout(holder(optimized))
    Observed(
      linkedFields = holder(linked).fields.map(_.name),
      optimizedFields = holder(optimized).fields.map(_.name),
      layoutEntries = layout.entries.map(_.name),
      size = layout.size,
      referenceOffsets = layout.layout.referenceFieldsOffsets.map(_.value)
    )
  }

  @Test def layoutDoesNotDependOnReachOrder(): Unit =
    link("Test", sources) {
      case (config, linked) =>
        Scope { implicit in =>
          val fields = holder(linked).fields.map(_.name)
          val declared = Seq("a", "b", "c", "d", "e", "f", "g")
          def ids(names: Seq[nir.Global]) = names.map {
            case nir.Global.Member(_, sig) =>
              sig.unmangled match {
                case nir.Sig.Field(id, _) => id
                case other                => other.toString
              }
            case other => other.toString
          }
          assertEquals(
            "Holder's fields in declaration order",
            declared,
            ids(fields)
          )

          val forward = observe(config, fields)
          val backward = observe(config, fields.reverse)
          assertEquals(
            "layout follows the declaration order",
            declared,
            ids(forward.layoutEntries)
          )
          assertEquals(
            "fields after linking",
            forward.linkedFields,
            backward.linkedFields
          )
          assertEquals(
            "fields after optimizing",
            forward.optimizedFields,
            backward.optimizedFields
          )
          assertEquals(
            "codegen layout entries",
            forward.layoutEntries,
            backward.layoutEntries
          )
          assertEquals("instance size", forward.size, backward.size)
          assertEquals(
            "reference field offsets",
            forward.referenceOffsets,
            backward.referenceOffsets
          )
        }
    }
}
