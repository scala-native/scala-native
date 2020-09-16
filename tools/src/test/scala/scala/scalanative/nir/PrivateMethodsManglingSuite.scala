package scala.scalanative.nir
import org.scalatest.matchers.should.Matchers
import scala.scalanative.LinkerSpec

class PrivateMethodsManglingSuite extends LinkerSpec with Matchers {
  "Nested mangling" should "distinguish private methods from different classes" in {
    val sources = Map(
      "A.scala" ->
        """package xyz
		  |abstract class A{
		  | protected def encodeLoop(arg1: Int, arg2: String): String
		  | final def encode(arg1: Int, arg2: String, arg3: Boolean): String = {
		  | 	def loop(): String = {
		  |  		if(arg3) "hello"
		  |    		else encodeLoop(arg1, arg2)
		  |  	}
		  | 	loop()	
		  | }
		  | private def foo(x: Int): Int = x + 1
		  | def bar(x: Int): Int = foo(x)
		  |}
		  |
		  |""".stripMargin,
      "B.scala" ->
        """
		|package xyz
		|object B extends A {
	    |	def encodeLoop(arg1: Int, arg2: String): String = {
	    | 		println("init_logic")
	    |   	val bool: Boolean = false
	    |  		def loop(): String = {
	    |   		if(bool) "asd" else arg2 * arg1
	    |   	}
	    |   	loop()
	    |  }
		|  private def foo(x: Int): Int = x * 2
		|  def baz(x: Int): Int = foo(x)
	    |}
		|""".stripMargin,
      "C.scala" ->
        """package foo
		|object B extends xyz.A {
		|	def encodeLoop(arg1: Int, arg2: String): String = {
		| 		println("init_logic")
		|   	val bool: Boolean = false
		|  		def loop(): String = {
		|   		if(bool) "asd" else arg2 * arg1
		|   	}
		|   	loop()
		|  }
		|  private def foo(x: Int): Int = x * 2
		|  def fooBar(x: Int): Int = foo(x)
		|}
		|""".stripMargin,
      "Main.scala" -> """object Main {
					 |  def main(args: Array[String]): Unit = {
					 |  	xyz.B.encode(1,"asd", true)
					 |   	xyz.B.baz(1)
					 |  	foo.B.encode(1,"asd", true)
					 |   	foo.B.fooBar(1)
					 |    	xyz.B.bar(1)
					 |  }
					 |}""".stripMargin
    )

    val tops = Seq("xyz.B$", "xyz.A", "foo.B$").map(Global.Top)

    link("Main$", sources) {
      case (_, result) =>
        val testedDefns = result.defns.collect {
          case Defn.Define(_, Global.Member(owner, sig), _, _)
              if tops.contains(owner) &&
                !sig.isCtor =>
            sig.mangle
        }.toSet

        val loopType = Seq(Type.Int, Rt.String, Type.Bool, Rt.String)
        val fooType  = Seq(Type.Int, Type.Int)

        def privateDef(method: String, in: String) = {
          s"P_${in}_$method"
        }

        val expected = Set(
          Sig.Method("encode", Seq(Type.Int, Rt.String, Type.Bool, Rt.String)),
          Sig.Method("encodeLoop", Seq(Type.Int, Rt.String, Rt.String)),
          Sig.Method(privateDef("loop$1", "xyz.A"), loopType),
          Sig.Method(privateDef("loop$1", "xyz.B$"), loopType),
          Sig.Method(privateDef("loop$1", "foo.B$"), loopType),
          Sig.Method(privateDef("foo", "xyz.A"), fooType),
          Sig.Method(privateDef("foo", "xyz.B$"), fooType),
          Sig.Method(privateDef("foo", "foo.B$"), fooType)
        ).map(_.mangle)

        expected.foreach { sig => assert(testedDefns.contains(sig)) }
    }

  }
}
