package scala.scalanative.codegen
package llvm

import scala.scalanative.nir.Val
import scala.language.implicitConversions

sealed trait Metadata
object Metadata {
  case class Str(value: String) extends Metadata
  case class Const(value: String) extends Metadata
  case class Value(value: Val) extends Metadata
  sealed trait Node extends Metadata {
    def distinct: Boolean = false
  }
  case class Tuple(values: Seq[Metadata]) extends Node
  sealed abstract class SpecializedNode extends Node with Product {
    def nodeName: String = getClass().getSimpleName()
  }

  sealed trait LLVMDebugInformation extends SpecializedNode
  sealed trait Scope extends LLVMDebugInformation
  case class DICompileUnit(
      file: DIFile,
      producer: String,
      isOptimized: Boolean
  ) extends Scope {
    override def distinct: Boolean = true
  }
  case class DIFile(filename: String, directory: String) extends Scope
  case class DISubprogram(
      name: String,
      linkageName: String,
      scope: Scope,
      file: DIFile,
      line: Int,
      tpe: DISubroutineType,
      unit: DICompileUnit
  ) extends Scope {
    override def distinct: Boolean = true
  }

  case class DILocation(line: Int, column: Int, scope: Scope)
      extends LLVMDebugInformation
  case class DILocalVariable(
      name: String,
      scope: Scope,
      file: LLVMDebugInformation,
      line: Int,
      tpe: Type
  ) extends LLVMDebugInformation

  sealed trait Type extends LLVMDebugInformation
  case class DIBasicType(name: String, size: Int, align: Int) extends Type
  case class DIDerivedType(tag: DWTag, baseType: Type, size: Int) extends Type
  case class DISubroutineType(types: DITypes) extends Type

  class DITypes(retTpe: Option[Type], arguments: Seq[Type])
      extends Tuple(retTpe.getOrElse(Metadata.Const("null")) +: arguments)
  object DITypes {
    def apply(retTpe: Option[Type], arguments: Seq[Type]): DITypes =
      new DITypes(retTpe, arguments)
  }

  sealed class DWTag(tag: Predef.String) extends Const(tag)
  object DWTag {
    object Pointer extends DWTag("DW_TAG_pointer_type")
  }

  object conversions {
    def tuple(values: Metadata*) = Metadata.Tuple(values)
    implicit def intToValue(v: Int): Metadata.Value = Metadata.Value(Val.Int(v))
    implicit def stringToStr(v: String): Metadata.Str = Metadata.Str(v)
    implicit class StringOps(val v: String) extends AnyVal {
      def const: Metadata.Const = Metadata.Const(v)
    }
  }
}
