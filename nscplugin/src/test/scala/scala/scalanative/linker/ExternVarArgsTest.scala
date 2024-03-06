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
          |@extern trait FFI {
          |  def printf(format: CString, args: Any*): Unit = extern
          |}
          |@extern object FFI extends FFI
          |
          |object Test{
          |  def main(): Unit = {
          |    def string: Ptr[Byte] = ???
          |    def size: Ptr[Size] = ???
          |    def long: Ptr[Long] = ???
          |    def float: Ptr[Float] = ???
          |    FFI.printf(c"", !(string + 1), string, !size, !long, long, !float)
          |    val ffi: FFI = null
          |    ffi.printf(c"", !(string + 1), string, !size, !long, long, !float)
          |  }
          |}
          |""".stripMargin
    ) { defns =>
      val TestModule = nir.Global.Top("Test$")
      val MainMethod =
        TestModule.member(nir.Sig.Method("main", Seq(nir.Type.Unit)))
      val FFIModule = nir.Global.Top("FFI$")
      val FFITrait = nir.Global.Top("FFI")
      val PrintfSig = nir.Sig.Extern("printf")
      val PrintfMethod = FFIModule.member(PrintfSig)
      val PrintfTraitMethod = FFITrait.member(PrintfSig)

      // Enusre has correct signature
      defns
        .collect {
          case nir.Defn.Declare(_, name @ PrintfMethod, ty)      => ty
          case nir.Defn.Declare(_, name @ PrintfTraitMethod, ty) => ty
        }
        .ensuring(_.size == 2)
        .foreach { ty =>
          assertEquals(ty.args.last, nir.Type.Vararg)
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
      defns
        .collectFirst {
          case defn @ nir.Defn.Define(_, MainMethod, _, insts, _) => insts
        }
        .ensuring(_.isDefined, "Not found main method")
        .head
        .collect {
          case nir.Inst.Let(
                _,
                nir.Op.Call(
                  _,
                  nir.Val.Global(PrintfMethod | PrintfTraitMethod, _),
                  args
                ),
                _
              ) =>
            args
        }
        .ensuring(
          _.size == 2,
          "Not found either tested method or the extern calls"
        )
        .foreach { callArgs =>
          assertEquals(expectedCallArgs.toList, callArgs.map(_.ty).toList)
        }
    }
  }
}
