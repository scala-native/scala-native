package scala.scalanative
package bindgen

import native._, stdlib._, stdio._
import scala.collection.mutable.ListBuffer

object ClangImporter {
  import ClangAPI._
  import Trees._

  def importHeaderFile(path: String): List[Tree] = {
    val index = createIndex(0, 0)
    val unit = parseTranslationUnit(index,
                                    toCString(path),
                                    null,
                                    0,
                                    null,
                                    0,
                                    CXTranslationUnit_SkipFunctionBodies)
    val cursor = getTranslationUnitCursor(unit)

    val trees = ListBuffer[Tree]()
    visitChildren(cursor, visitor, trees.cast[Data])

    disposeTranslationUnit(unit)
    disposeIndex(index)

    trees.toList
  }

  val visitor: Visitor = (cursor: CXCursor, parent: CXCursor, data: Data) => {
    val trees              = data.cast[ListBuffer[Tree]]
    val kind: CXCursorKind = getCursorKind(cursor)
    if (kind == CXCursor_FunctionDecl) {
      val name               = getCursorSpelling(cursor)
      val cursorType         = getCursorType(cursor)
      val returnType         = getResultType(cursorType)
      val returnTypeSpelling = getTypeSpelling(returnType)
      val argc               = Cursor_getNumArguments(cursor)

      trees += Function(fromCString(name),
                        fromCString(returnTypeSpelling),
                        functionParams(cursor))

    } else if (kind == CXCursor_EnumDecl) {
      val name       = getCursorSpelling(cursor)
      val enumType   = getEnumDeclIntegerType(cursor)
      val enumValues = ListBuffer[Enum.Value]()

      visitChildren(cursor, enumVisitor, enumValues.cast[Data])

      trees += Enum(fromCString(name), List(enumValues: _*))

    } else if (kind == CXCursor_TypedefDecl) {
      val name                = getCursorSpelling(cursor)
      val typedefType         = getTypedefDeclUnderlyingType(cursor)
      val typedefTypeSpelling = getTypeSpelling(typedefType)

      trees += Typedef(fromCString(name), fromCString(typedefTypeSpelling))
    } else {
      val name         = getCursorSpelling(cursor)
      val kindSpelling = getCursorKindSpelling(kind)
      printf(c"Unhandled cursor kind for %s: %s\n", name, kindSpelling)
    }

    CXChildVisit_Continue
  }

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
}
