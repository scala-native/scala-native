package scala.scalanative.linker

import org.scalatest._
import scala.scalanative.LinkerSpec
import scala.scalanative.nir._
import scala.scalanative.util.Scope
import scala.scalanative.io._
import scala.scalanative.NIRCompiler
import org.scalatest.flatspec.AnyFlatSpec
import java.nio.file.{Files, Path, Paths}

class StaticForwardersSuite extends LinkerSpec {
  "Static forwarder methods" should "be generated for @main annotated method" in {
    val MainClass = Global.Top("myMainFunction")
    val Package = Global.Top("Main$package")
    val PackageModule = Global.Top("Main$package$")

    compileAndLoad(
      "Main.scala" -> "@main def myMainFunction(): Unit = ()"
    ) { defns =>
      val expected = Seq(
        MainClass,
        MainClass.member(Sig.Ctor(Nil)),
        MainClass.member(
          Sig.Method("main", Rt.ScalaMainSig.types, Sig.Scope.PublicStatic)
        ),
        Package.member(
          Sig.Method("myMainFunction", Nil, Sig.Scope.PublicStatic)
        ),
        PackageModule.member(Sig.Ctor(Nil)),
        PackageModule.member(Sig.Method("myMainFunction", Nil))
      )
      val names = defns.map(_.name)
      assert(expected.diff(names).isEmpty)
    }
  }
  it should "generate static members for methods defined in companion object" in {
    compileAndLoad(
      "Test.scala" ->
        """ 
          |class Foo() {
          |  def foo(): String = {
          |    Foo.bar() + Foo.fooBar
          |  }
          |}
          |object Foo {
          |  def main(args: Array[String]): Unit  = {
          |    val x = new Foo().foo()
          |  }
          |  def bar(): String = "bar"
          |  def fooBar: String = "foo" + bar()
          |}
          """.stripMargin
    ) { defns =>
      val Class = Global.Top("Foo")
      val Module = Global.Top("Foo$")
      val expected = Seq(
        Class.member(Sig.Ctor(Nil)),
        Class.member(Sig.Method("foo", Seq(Rt.String))),
        Class.member(Sig.Method("bar", Seq(Rt.String), Sig.Scope.PublicStatic)),
        Class.member(
          Sig.Method("fooBar", Seq(Rt.String), Sig.Scope.PublicStatic)
        ),
        Class.member(
          Sig.Method(
            Rt.ScalaMainSig.id,
            Rt.ScalaMainSig.types,
            Sig.Scope.PublicStatic
          )
        ),
        Module.member(Sig.Ctor(Nil)),
        Module.member(Rt.ScalaMainSig),
        Module.member(Sig.Method("bar", Seq(Rt.String))),
        Module.member(Sig.Method("fooBar", Seq(Rt.String)))
      )

      assert(expected.diff(defns.map(_.name)).isEmpty)
    }
  }
  it should "generate static accessors to fields defined in compation object" in {
    compileAndLoad(
      "Test.scala" ->
        """ 
          |class Foo() {
          |  val foo = "foo"
          |}
          |object Foo {
          |  val bar = "bar"
          |}
          """.stripMargin
    ) { defns =>
      val Class = Global.Top("Foo")
      val Module = Global.Top("Foo$")
      val expected = Seq(
        Class.member(Sig.Field("foo", Sig.Scope.Private(Class))),
        Class.member(Sig.Method("foo", Seq(Rt.String))),
        Class.member(Sig.Method("bar", Seq(Rt.String), Sig.Scope.PublicStatic)),
        Module.member(Sig.Field("bar", Sig.Scope.Private(Module))),
        Module.member(Sig.Method("bar", Seq(Rt.String)))
      )
      assert(expected.diff(defns.map(_.name)).isEmpty)
    }
  }

  def compileAndLoad(
      sources: (String, String)*
  )(fn: Seq[Defn] => Unit): Unit = {
    Scope { implicit in =>
      val outDir = Files.createTempDirectory("native-test-out")
      val compiler = NIRCompiler.getCompiler(outDir)
      val sourcesDir = NIRCompiler.writeSources(sources.toMap)
      val dir = VirtualDirectory.real(outDir)

      val defns = compiler
        .compile(sourcesDir)
        .toSeq
        .filter(_.toString.endsWith(".nir"))
        .map(outDir.relativize(_))
        .flatMap { path =>
          val buffer = dir.read(path)
          serialization.deserializeBinary(buffer, path.toString)
        }
      fn(defns)
    }
  }
}
