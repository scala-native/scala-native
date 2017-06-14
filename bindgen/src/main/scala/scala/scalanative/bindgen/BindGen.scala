package scala.scalanative
package bindgen

import native._, stdlib._, stdio._

object BindGen {
  import Trees._

  def main(args: Array[String]): Unit = {
    if (args.length < 3) {
      stdio.fprintf(stdio.stderr,
                    c"Usage: bindgen header-file module package\n")
      stdlib.exit(stdlib.EXIT_FAILURE)
    }

    val trees = ClangImporter.importHeaderFile(args(0))
    println(BindGen.generate(trees, args(1), args(2)))
  }

  def generate(trees: List[Tree], module: String, pkg: String): String = {
    val builder = new StringBuilder()
    builder.append(s"""package $pkg
                      |
                      |import scala.scalanative.native._
                      |
                      |@extern
                      |object $module {
                      |""".stripMargin)

    trees.foreach {
      case Enum(name, enumValues) =>
        builder.append(s"  object $name {\n")
        enumValues.foreach { enumValue =>
          builder.append(s"    val ${enumValue.name} = ${enumValue.value}\n")
        }
        builder.append(s"  }\n")

      case Typedef(name, tpe) =>
        builder.append(s"  type $name = ${cToScala(tpe)}\n")

      case Function(name, returnType, args) =>
        val argList =
          args.map(arg => s"${arg.name}: ${cToScala(arg.tpe)}").mkString(", ")
        builder.append(
          s"  def $name($argList): ${cToScala(returnType)} = extern\n")
    }
    builder.append("}")
    builder.toString
  }

  // Poor-man's mapping from C to Scala
  // FIXME: Use CXType introspection and make pointer handling generic
  def cToScala(tpe: String) = {
    tpe match {
      case "const char *" | "char *" => "CString"
      case "char *const *"           => "Ptr[CString]"
      case "int"                     => "CInt"
      case "int *"                   => "Ptr[CInt]"
      case "unsigned int"            => "CUInt"
      case "unsigned int *"          => "Ptr[CUInt]"
      case "void *"                  => "Ptr[Byte]"
      case "size_t"                  => "CSize"
      case unknown                   => unknown
    }
  }
}
