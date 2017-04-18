package scala.scalanative
package bindgen

import native._, stdlib._, stdio._, string._
import scala.collection.mutable.ListBuffer

object Bindgen {
  import ClangAPI._

  /*
   * A simplified AST mapped from libclang and targeted towards generating
   * Scala code.
   */
  sealed trait Node
  case class Function(name: String,
                      returnType: String,
                      args: List[Function.Param])
      extends Node
  object Function {
    case class Param(name: String, tpe: String)
  }

  case class Enum(name: String, values: List[Enum.Value]) extends Node
  object Enum {
    case class Value(name: String, value: CLongLong)
  }

  case class Typedef(name: String, underlying: String) extends Node

  def functionParam(i: Int, parent: CXCursor) = {
    val cursor       = Cursor_getArgument(parent, i)
    val tpe          = getCursorType(cursor)
    val name         = getCursorSpelling(cursor)
    val typeSpelling = getTypeSpelling(tpe)
    val nonEmptyName =
      Option(fromCString(name)).filter(_.nonEmpty).getOrElse(s"arg$i")

    Function.Param(nonEmptyName, fromCString(typeSpelling))
  }

  def functionParams(cursor: CXCursor) = {
    val argc = Cursor_getNumArguments(cursor)
    var i    = 0
    var args = List.empty[Function.Param]

    while (i < argc) {
      args = args :+ functionParam(i, cursor)
      i += 1
    }
    args
  }

  val enumVisitor: Visitor =
    (cursor: CXCursor, parent: CXCursor, data: Data) => {
      val enumValues         = data.cast[ListBuffer[Enum.Value]]
      val kind: CXCursorKind = getCursorKind(cursor)
      assert(kind == CXCursor_EnumConstantDecl)
      val name  = getCursorSpelling(cursor)
      val value = getEnumConstantDeclValue(cursor)
      enumValues += Enum.Value(fromCString(name), value)
      CXChildVisit_Continue
    }

  val visitor: Visitor = (cursor: CXCursor, parent: CXCursor, data: Data) => {
    val nodes              = data.cast[ListBuffer[Node]]
    val kind: CXCursorKind = getCursorKind(cursor)
    if (kind == CXCursor_FunctionDecl) {
      val name               = getCursorSpelling(cursor)
      val cursorType         = getCursorType(cursor)
      val returnType         = getResultType(cursorType)
      val returnTypeSpelling = getTypeSpelling(returnType)
      val argc               = Cursor_getNumArguments(cursor)

      nodes += Function(fromCString(name),
                        fromCString(returnTypeSpelling),
                        functionParams(cursor))

    } else if (kind == CXCursor_EnumDecl) {
      val name       = getCursorSpelling(cursor)
      val enumType   = getEnumDeclIntegerType(cursor)
      val enumValues = ListBuffer[Enum.Value]()

      visitChildren(cursor, enumVisitor, enumValues.cast[Data])

      nodes += Enum(fromCString(name), List(enumValues: _*))

    } else if (kind == CXCursor_TypedefDecl) {
      val name                = getCursorSpelling(cursor)
      val typedefType         = getTypedefDeclUnderlyingType(cursor)
      val typedefTypeSpelling = getTypeSpelling(typedefType)

      nodes += Typedef(fromCString(name), fromCString(typedefTypeSpelling))
    } else {
      val name         = getCursorSpelling(cursor)
      val kindSpelling = getCursorKindSpelling(kind)
      printf(c"Unhandled cursor kind for %s: %s\n", name, kindSpelling)
    }

    CXChildVisit_Continue
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

  def generate(path: String, module: String, pkg: String): String = {
    val index = createIndex(0, 0)
    val unit = parseTranslationUnit(index,
                                    toCString(path),
                                    null,
                                    0,
                                    null,
                                    0,
                                    CXTranslationUnit_SkipFunctionBodies)
    val cursor = getTranslationUnitCursor(unit)

    val nodes = ListBuffer[Node]()
    visitChildren(cursor, visitor, nodes.cast[Data])

    disposeTranslationUnit(unit)
    disposeIndex(index)

    val builder = new StringBuilder()
    builder.append(s"""package $pkg
                      |
                      |import scala.scalanative.native._
                      |
                      |@extern
                      |object $module {
                      |""".stripMargin)

    nodes.foreach {
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

  def main(args: Array[String]): Unit = {
    if (args.length < 3) {
      stdio.fprintf(stdio.stderr,
                    c"Usage: bindgen header-file module package\n")
      stdlib.exit(stdlib.EXIT_FAILURE)
    }
    println(Bindgen.generate(args(0), args(1), args(2)))
  }
}
