import scalanative.build.Config.InlineSourceHook
import scala.scalanative.nir.Attr.InlineSource
import scala.scalanative.build
import sbt._

enablePlugins(ScalaNativePlugin)

scalaVersion := "2.11.12"

val hook = new InlineSourceHook {
  val name = "inline-source test"

  def process(srcs: Seq[InlineSource], logger: build.Logger): Option[String] = {
    srcs foreach { src =>
      IO.write(new File(src.annottee + "." + src.language), src.source)
    }
    None
  }

}

nativeInlineSourceHooks += hook
