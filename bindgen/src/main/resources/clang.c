/**
 * Macro-based libclang wrapper from which both the C implementation and Scala
 * API are built.
 *
 * Run the following command to see the Scala code:
 *
 *    $ clang -DSCALA -E bindgen/src/main/resources/clang.c | grep -v '^#'
 */

// An incomplete list of enums. Not all of these are currently used.
// Each enum value is exposed to Scala using a C method.
#define ENUM_INFO(_) \
  _(CXCursorKind, CXCursor_StructDecl) \
  _(CXCursorKind, CXCursor_UnionDecl) \
  _(CXCursorKind, CXCursor_EnumDecl) \
  _(CXCursorKind, CXCursor_EnumConstantDecl) \
  _(CXCursorKind, CXCursor_FunctionDecl) \
  _(CXCursorKind, CXCursor_VarDecl) \
  _(CXCursorKind, CXCursor_TypedefDecl) \
  _(CXTranslationUnit_Flags, CXTranslationUnit_None) \
  _(CXTranslationUnit_Flags, CXTranslationUnit_SkipFunctionBodies) \
  _(CXChildVisitResult, CXChildVisit_Break) \
  _(CXChildVisitResult, CXChildVisit_Continue) \
  _(CXChildVisitResult, CXChildVisit_Recurse) \
  _(CXTypeKind, CXType_Invalid) \
  _(CXTypeKind, CXType_Unexposed) \
  _(CXTypeKind, CXType_Void) \
  _(CXTypeKind, CXType_Bool) \
  _(CXTypeKind, CXType_Char_U) \
  _(CXTypeKind, CXType_UChar) \
  _(CXTypeKind, CXType_Char16) \
  _(CXTypeKind, CXType_Char32) \
  _(CXTypeKind, CXType_UShort) \
  _(CXTypeKind, CXType_UInt) \
  _(CXTypeKind, CXType_ULong) \
  _(CXTypeKind, CXType_ULongLong) \
  _(CXTypeKind, CXType_UInt128) \
  _(CXTypeKind, CXType_Char_S) \
  _(CXTypeKind, CXType_SChar) \
  _(CXTypeKind, CXType_WChar) \
  _(CXTypeKind, CXType_Short) \
  _(CXTypeKind, CXType_Int) \
  _(CXTypeKind, CXType_Long) \
  _(CXTypeKind, CXType_LongLong) \
  _(CXTypeKind, CXType_Int128) \
  _(CXTypeKind, CXType_Float) \
  _(CXTypeKind, CXType_Double) \
  _(CXTypeKind, CXType_LongDouble) \
  _(CXTypeKind, CXType_NullPtr) \
  _(CXTypeKind, CXType_Overload) \
  _(CXTypeKind, CXType_Dependent) \
  _(CXTypeKind, CXType_ObjCId) \
  _(CXTypeKind, CXType_ObjCClass) \
  _(CXTypeKind, CXType_ObjCSel) \
  _(CXTypeKind, CXType_Complex) \
  _(CXTypeKind, CXType_Pointer) \
  _(CXTypeKind, CXType_BlockPointer) \
  _(CXTypeKind, CXType_LValueReference) \
  _(CXTypeKind, CXType_RValueReference) \
  _(CXTypeKind, CXType_Record) \
  _(CXTypeKind, CXType_Enum) \
  _(CXTypeKind, CXType_Typedef) \
  _(CXTypeKind, CXType_ObjCInterface) \
  _(CXTypeKind, CXType_ObjCObjectPointer) \
  _(CXTypeKind, CXType_FunctionNoProto) \
  _(CXTypeKind, CXType_FunctionProto) \
  _(CXTypeKind, CXType_ConstantArray) \
  _(CXTypeKind, CXType_Vector) \
  _(CXTypeKind, CXType_IncompleteArray) \
  _(CXTypeKind, CXType_VariableArray) \
  _(CXTypeKind, CXType_DependentSizedArray) \
  _(CXTypeKind, CXType_MemberPointer)

// Wrappers for various getters.
// PRIMITIVE_ wraps methods returning primitive values
// COPY_ wraps methods returning structs by value.
// STRING_ wraps methods returning CXStrings.
#define GETTER_INFO(PRIMITIVE_, COPY_, STRING_) \
  PRIMITIVE_(CXCursorKind, getCursorKind, CXCursor, *cursor) \
  PRIMITIVE_(CInt, Cursor_getNumArguments, CXCursor, *cursor) \
  PRIMITIVE_(CLongLong, getEnumConstantDeclValue, CXCursor, *cursor) \
  COPY_(CXType, getCursorType, CXCursor, *cursor) \
  COPY_(CXType, getResultType, CXType, *tpe) \
  COPY_(CXType, getEnumDeclIntegerType, CXCursor, *cursor) \
  COPY_(CXType, getTypedefDeclUnderlyingType, CXCursor, *cursor) \
  COPY_(CXCursor, getTranslationUnitCursor, CXTranslationUnit, unit) \
  STRING_(getCursorKindSpelling, CXCursorKind, kind) \
  STRING_(getCursorSpelling, CXCursor, *cursor) \
  STRING_(getTypeSpelling, CXType, *tpe)

#ifdef SCALA
package scala.scalanative
package bindgen

import scalanative.native._

#define EXTERN_FUNCTION0(symbol, id, tpe) @name(#symbol) def id(): tpe = extern;
#define EXTERN_FUNCTION1(symbol, id, tpe, argType, arg) \
  @name(#symbol) def id(`arg`: argType): tpe = extern;

#define DEFINE_ENUM(tpe, id) EXTERN_FUNCTION0(scalanative_clang_##id, id, tpe)

#define PRIMITIVE_GETTER(tpe, name, argType, arg) \
  EXTERN_FUNCTION1(scalanative_clang_##name, name, tpe, argType, arg)

#define COPY_GETTER(tpe, name, argType, arg) \
  EXTERN_FUNCTION1(scalanative_clang_##name, name, tpe, argType, arg)

#define STRING_GETTER(name, argType, arg) \
  EXTERN_FUNCTION1(scalanative_clang_##name, name, CString, argType, arg)

@extern
@link("clang")
object ClangAPI {
  type Data = Ptr[Byte]
  type CXIndex = Ptr[Byte]
  type CXCursor = Ptr[Byte]
  type CXType = Ptr[Byte]
  type CXTranslationUnit = Ptr[Byte]
  type CXUnsavedFile = Ptr[Byte]
  type Visitor = CFunctionPtr3[CXCursor, CXCursor, Data, UInt]

  type CXCursorKind = UInt
  type CXTypeKind = UInt
  type CXTranslationUnit_Flags = UInt
  type CXChildVisitResult = UInt
  ENUM_INFO(DEFINE_ENUM)

  GETTER_INFO(PRIMITIVE_GETTER, COPY_GETTER, STRING_GETTER)

  @name("scalanative_clang_Cursor_getArgument")
  def Cursor_getArgument(cursor: CXCursor, i: CInt): CXCursor = extern

  @name("scalanative_clang_visitChildren")
  def visitChildren(parent: CXCursor, visitor: Visitor, data: Data): UInt = extern

  @name("clang_createIndex")
  def createIndex(excludeDeclarationsFromPCH: CInt, displayDiagnostics: CInt): CXIndex = extern

  @name("clang_disposeIndex")
  def disposeIndex(index: CXIndex): Unit = extern

  @name("clang_parseTranslationUnit")
  def parseTranslationUnit(index: CXIndex, fileName: CString, argv: Ptr[CString], argc: CInt, unsavedFiles: CXUnsavedFile, numUnsavedFiles: CInt, options: UInt): CXTranslationUnit = extern

  @name("clang_disposeTranslationUnit")
  def disposeTranslationUnit(unit: CXTranslationUnit): Unit = extern
}
#endif

#ifndef SCALA
#include <clang-c/Index.h>
#include <stdlib.h>
#include <string.h>

typedef enum CXCursorKind CXCursorKind;
typedef long long CLongLong;
typedef int CInt;

#define DEFINE_ENUM(tpe, id) enum tpe scalanative_clang_##id() { return id; };
ENUM_INFO(DEFINE_ENUM)

#define PRIMITIVE_GETTER(tpe, name, argType, arg) \
  tpe \
  scalanative_clang_##name(argType arg) \
  { \
    return clang_##name(arg); \
  }

#define COPY_GETTER(tpe, name, argType, arg) \
  tpe * \
  scalanative_clang_##name(argType arg) \
  { \
    tpe *copy = malloc(sizeof(tpe)); \
    *copy = clang_##name(arg); \
    return copy; \
  }

#define STRING_GETTER(name, argType, arg) \
  const char * \
  scalanative_clang_##name(argType arg) \
  { \
    CXString cxstring = clang_##name(arg); \
    const char *string = strdup(clang_getCString(cxstring)); \
    clang_disposeString(cxstring); \
    return string; \
  }

GETTER_INFO(PRIMITIVE_GETTER, COPY_GETTER, STRING_GETTER)

CXCursor *
scalanative_clang_Cursor_getArgument(CXCursor *cursor, int i)
{
  CXCursor *copy = malloc(sizeof(CXCursor));
  *copy = clang_Cursor_getArgument(*cursor, i);
  return copy;
}

/*
 * Wrappers for the visitor API
 */

typedef enum CXChildVisitResult (*bindgen_visitor)(CXCursor *cursor, CXCursor *parent, void *data);

struct bindgen_context {
  bindgen_visitor visitor;
  void *data;
};

static enum CXChildVisitResult
scalanative_clang_visit(CXCursor cursor, CXCursor parent, CXClientData data)
{
  struct bindgen_context *ctx = data;

  CXSourceLocation location = clang_getCursorLocation(cursor);
  if (!clang_Location_isFromMainFile(location))
    return CXChildVisit_Continue;

  return ctx->visitor(&cursor, &parent, ctx->data);
}

unsigned
scalanative_clang_visitChildren(CXCursor *parent, bindgen_visitor visitor,  void *data)
{
  struct bindgen_context ctx = { .visitor = visitor, .data = data };

  return clang_visitChildren(*parent, scalanative_clang_visit, &ctx);
}
#endif
