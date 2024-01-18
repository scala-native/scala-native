package scala.scalanative
package linker

import org.junit.Test
import org.junit.Assert._

class ExternVarArgsTest {

  @Test def unboxesVarArgs(): Unit = {
    compileAndLoad(
      "Test.scala" ->
        """import scala.scalanative.unsafe._
          | 
          |@extern object FFI {
          |  def printf(format: CString, args: Any*): Unit = extern
          |}
          |
          |object Test{
          |  def main(): Unit = {
          |    def string: Ptr[Byte] = ???
          |    def size: Ptr[Size] = ???
          |    def long: Ptr[Long] = ???
          |    def float: Ptr[Float] = ???
          |    FFI.printf(c"", !(string + 1), string, !size, !long, long, !float)
          |  }
          |}
          |""".stripMargin
    ) { defns =>
      val TestModule = nir.Global.Top("Test$")
      val MainMethod =
        TestModule.member(nir.Sig.Method("main", Seq(nir.Type.Unit)))
      val FFIModule = nir.Global.Top("FFI$")
      val PrintfMethod = FFIModule.member(nir.Sig.Extern("printf"))
      val callArgs: Seq[nir.Val] = defns
        .collectFirst {
          case nir.Defn.Define(_, MainMethod, _, insts, _) => insts
        }
        .flatMap {
          _.collectFirst {
            case nir.Inst.Let(
                  _,
                  nir.Op.Call(_, nir.Val.Global(PrintfMethod, _), args),
                  _
                ) =>
              args
          }
        }
        .headOption
        .getOrElse {
          fail("Not found either tested method or the extern calls"); ???
        }
      val expectedCallArgs = Seq(
        nir.Type.Ptr, // format CString
        nir.Type.Int, // byte extended to Int
        nir.Type.Ptr, // Ptr[Byte]
        nir.Type.Size, // size,
        nir.Type.Long, // long
        nir.Type.Ptr, // Ptr[Long]
        nir.Type.Double // float extended to double
      )

      assertEquals(expectedCallArgs.toList, callArgs.map(_.ty).toList)
    }
  }
}
