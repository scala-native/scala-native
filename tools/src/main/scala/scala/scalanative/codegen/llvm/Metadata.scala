package scala.scalanative
package codegen
package llvm

import scala.language.implicitConversions

sealed trait Metadata
object Metadata {
  case class Str(value: String) extends Metadata
  case class Const(value: String) extends Metadata
  case class Value(value: nir.Val) extends Metadata
  sealed trait Node extends Metadata {
    def distinct: Boolean = false
  }
  case class Tuple(values: Seq[Metadata]) extends Node
  object Tuple {
    val empty = Tuple(Nil)
  }
  sealed abstract class SpecializedNode extends Node with Product {
    def nodeName: String = getClass().getSimpleName()
  }

  case class DISubrange(
      count: Metadata,
      lowerBound: Option[Metadata] = Some(Value(nir.Val.Int(0))),
  ) extends SpecializedNode
  object DISubrange {
    final val empty = DISubrange(count = Value(nir.Val.Int(-1)))
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

  case class DIExpressions(expressions: Const*) extends Node
  sealed class DIExpression(symbol: String) extends Const(symbol)
  object DIExpression {
    object DW_OP_deref extends DIExpression("DW_OP_deref")
    object DW_OP_plus extends DIExpression("DW_OP_plus")
    object DW_OP_minus extends DIExpression("DW_OP_minus")
    object DW_OP_plus_uconst extends DIExpression("DW_OP_plus_uconst")
    object DW_OP_LLVM_fragment extends DIExpression("DW_OP_LLVM_fragment")
    object DW_OP_LLVM_convert extends DIExpression("DW_OP_LLVM_convert")
    object DW_OP_LLVM_tag_offset extends DIExpression("DW_OP_LLVM_tag_offset")
    object DW_OP_swap extends DIExpression("DW_OP_swap")
    object DW_OP_xderef extends DIExpression("DW_OP_xderef")
    object DW_OP_stack_value extends DIExpression("DW_OP_stack_value")
    object DW_OP_LLVM_entry_value extends DIExpression("DW_OP_LLVM_entry_value")
    object DW_OP_LLVM_arg extends DIExpression("DW_OP_LLVM_arg")
    object DW_OP_breg extends DIExpression("DW_OP_breg")
    object DW_OP_push_object_address
        extends DIExpression("DW_OP_push_object_address")
    object DW_OP_over extends DIExpression("DW_OP_over")
    object DW_OP_LLVM_implicit_pointer
        extends DIExpression("DW_OP_LLVM_implicit_pointer")
  }

  sealed trait Type extends LLVMDebugInformation with Scope
  case class DIBasicType(
      name: String,
      size: BitSize,
      align: BitSize,
      encoding: DW_ATE
  ) extends Type

  case class DIDerivedType(
      tag: DWTag,
      baseType: Type,
      size: BitSize,
      offset: Option[BitSize] = None,
      name: Option[String] = None,
      scope: Option[Scope] = None,
      file: Option[DIFile] = None,
      line: Option[Int] = None,
      flags: DIFlags = DIFlags()
  ) extends Type

  case class DISubroutineType(types: DITypes) extends Type
  case class DICompositeType(
      tag: DWTag,
      size: Option[BitSize] = None,
      name: Option[String] = None,
      identifier: Option[String] = None,
      scope: Option[Scope] = None,
      file: Option[DIFile] = None,
      line: Option[Int] = None,
      flags: DIFlags = DIFlags(),
      // for arrays
      baseType: Option[Type] = None,
      dataLocation: Option[Metadata] = None
  )(private var elements: Tuple)
      extends Type
      with CanBeRecursive {
    override def distinct: Boolean = true

    def recursiveNodes: Seq[Node] = Seq(elements)

    def getElements: Tuple = this.elements
    def withDependentElements(
        producer: DICompositeType => Seq[Node]
    ): this.type = {
      elements = Tuple(producer(this))
      this
    }

  }

  class DITypes(retTpe: Option[Type], arguments: Seq[Type])
      extends Tuple(retTpe.getOrElse(Metadata.Const("null")) +: arguments)
  object DITypes {
    def apply(retTpe: Option[Type], arguments: Seq[Type]): DITypes =
      new DITypes(retTpe, arguments)
  }

  sealed class DWTag(tag: Predef.String) extends Const(tag)
  object DWTag {
    object Pointer extends DWTag("DW_TAG_pointer_type")
    object Reference extends DWTag("DW_TAG_reference_type")
    object Array extends DWTag("DW_TAG_array_type")
    object Structure extends DWTag("DW_TAG_structure_type")
    object Class extends DWTag("DW_TAG_class_type")
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

  sealed class ModFlagBehavior(tag: Int) extends Value(nir.Val.Int(tag))
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

  case class DIFlags(union: DIFlag*) extends AnyVal {
    def nonEmpty: Boolean = union.nonEmpty
  }
  sealed class DIFlag(value: String)
  object DIFlag {
    case object DIFlagZero extends DIFlag("DIFlagZero")
    case object DIFlagPrivate extends DIFlag("DIFlagPrivate")
    case object DIFlagProtected extends DIFlag("DIFlagProtected")
    case object DIFlagPublic extends DIFlag("DIFlagPublic")
    case object DIFlagFwdDecl extends DIFlag("DIFlagFwdDecl")
    case object DIFlagAppleBlock extends DIFlag("DIFlagAppleBlock")
    case object DIFlagReservedBit4 extends DIFlag("DIFlagReservedBit4")
    case object DIFlagVirtual extends DIFlag("DIFlagVirtual")
    case object DIFlagArtificial extends DIFlag("DIFlagArtificial")
    case object DIFlagExplicit extends DIFlag("DIFlagExplicit")
    case object DIFlagPrototyped extends DIFlag("DIFlagPrototyped")
    case object DIFlagObjcClassComplete
        extends DIFlag("DIFlagObjcClassComplete")
    case object DIFlagObjectPointer extends DIFlag("DIFlagObjectPointer")
    case object DIFlagVector extends DIFlag("DIFlagVector")
    case object DIFlagStaticMember extends DIFlag("DIFlagStaticMember")
    case object DIFlagLValueReference extends DIFlag("DIFlagLValueReference")
    case object DIFlagRValueReference extends DIFlag("DIFlagRValueReference")
    case object DIFlagReserved extends DIFlag("DIFlagReserved")
    case object DIFlagSingleInheritance
        extends DIFlag("DIFlagSingleInheritance")
    case object DIFlagMultipleInheritance
        extends DIFlag("DIFlagMultipleInheritance")
    case object DIFlagVirtualInheritance
        extends DIFlag("DIFlagVirtualInheritance")
    case object DIFlagIntroducedVirtual
        extends DIFlag("DIFlagIntroducedVirtual")
    case object DIFlagBitField extends DIFlag("DIFlagBitField")
    case object DIFlagNoReturn extends DIFlag("DIFlagNoReturn")
    case object DIFlagTypePassByValue extends DIFlag("DIFlagTypePassByValue")
    case object DIFlagTypePassByReference
        extends DIFlag("DIFlagTypePassByReference")
    case object DIFlagEnumClass extends DIFlag("DIFlagEnumClass")
    case object DIFlagThunk extends DIFlag("DIFlagThunk")
    case object DIFlagNonTrivial extends DIFlag("DIFlagNonTrivial")
    case object DIFlagBigEndian extends DIFlag("DIFlagBigEndian")
    case object DIFlagLittleEndian extends DIFlag("DIFlagLittleEndian")
    case object DIFlagIndirectVirtualBase
        extends DIFlag("DIFlagIndirectVirtualBase")
  }

  trait CanBeRecursive {
    def recursiveNodes: Seq[Node]
  }

  implicit class LongBitSizeOps(v: Long) {
    def toBitSize: BitSize = new BitSize(v)
  }
  implicit class IntBitSizeOps(v: Int) {
    def toBitSize: BitSize = new BitSize(v.toLong)
  }
  class BitSize(val sizeOfBytes: Long) extends AnyVal {
    def sizeOfBits: Long = sizeOfBytes * 8
  }

  object Constants {
    val PRODUCER = "Scala Native"
    val DWARF_VERSION = 3
    val DEBUG_INFO_VERSION = 3
  }

  object conversions {
    def tuple(values: Metadata*) = Metadata.Tuple(values)
    implicit def intToValue(v: Int): Metadata.Value =
      Metadata.Value(nir.Val.Int(v))
    implicit def stringToStr(v: String): Metadata.Str = Metadata.Str(v)
    implicit class StringOps(val v: String) extends AnyVal {
      def string: Metadata.Str = Metadata.Str(v)
      def const: Metadata.Const = Metadata.Const(v)
    }
  }
}
