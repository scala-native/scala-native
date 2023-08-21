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

  case class DILexicalBlock(scope: Scope, file: DIFile, line: Int, column: Int)
      extends Scope {
    override def distinct: Boolean = true
  }

  case class DILocation(line: Int, column: Int, scope: Scope)
      extends LLVMDebugInformation
  case class DILocalVariable(
      name: String,
      arg: Option[Int],
      scope: Scope,
      file: DIFile,
      line: Int,
      tpe: Type
  ) extends LLVMDebugInformation

  // TOOD: actual DW_OP expressions as parameters
  case class DIExpression() extends LLVMDebugInformation

  sealed trait Type extends LLVMDebugInformation
  case class DIBasicType(name: String, size: Int, align: Int, encoding: DW_ATE)
      extends Type
  case class DIDerivedType(tag: DWTag, baseType: Type, size: Int) extends Type

  case class DISubroutineType(types: DITypes) extends Type
  case class DICompositeType(
      tag: DWTag,
      // TODO: name: String
      size: Int,
      elements: Tuple
  ) extends Type

  class DITypes(retTpe: Option[Type], arguments: Seq[Type])
      extends Tuple(retTpe.getOrElse(Metadata.Const("null")) +: arguments)
  object DITypes {
    def apply(retTpe: Option[Type], arguments: Seq[Type]): DITypes =
      new DITypes(retTpe, arguments)
  }

  sealed class DWTag(tag: Predef.String) extends Const(tag)
  object DWTag {
    object Pointer extends DWTag("DW_TAG_pointer_type")
    object StructureType extends DWTag("DW_TAG_structure_type")
    object Member extends DWTag("DW_TAG_member")
  }

  sealed class DW_ATE(tag: Predef.String) extends Const(tag)
  object DW_ATE {
    object Address extends DW_ATE("DW_ATE_address")
    object Boolean extends DW_ATE("DW_ATE_boolean")
    object Float extends DW_ATE("DW_ATE_float")
    object Signed extends DW_ATE("DW_ATE_signed")
    object SignedChar extends DW_ATE("DW_ATE_signed_char")
    object Unsigned extends DW_ATE("DW_ATE_unsigned")
    object UnsignedChar extends DW_ATE("DW_ATE_unsigned_char")
  }

  sealed class ModFlagBehavior(tag: Int) extends Value(Val.Int(tag))
  object ModFlagBehavior {
    object Error extends ModFlagBehavior(1)
    object Warning extends ModFlagBehavior(2)
    object Require extends ModFlagBehavior(3)
    object Override extends ModFlagBehavior(4)
    object Append extends ModFlagBehavior(5)
    object AppendUnique extends ModFlagBehavior(6)
    object Max extends ModFlagBehavior(7)
    object Min extends ModFlagBehavior(8)

    final val ModFlagBehaviorFirstVal = Error
    final val ModFlagBehaviorLastVal = Min
  }

  object Constants {
    val PRODUCER = "Scala Native"
    val DWARF_VERSION = 3
    val DEBUG_INFO_VERSION = 3
  }

  object conversions {
    def tuple(values: Metadata*) = Metadata.Tuple(values)
    implicit def intToValue(v: Int): Metadata.Value = Metadata.Value(Val.Int(v))
    implicit def stringToStr(v: String): Metadata.Str = Metadata.Str(v)
    implicit class StringOps(val v: String) extends AnyVal {
      def string: Metadata.Str = Metadata.Str(v)
      def const: Metadata.Const = Metadata.Const(v)
    }
  }
}
