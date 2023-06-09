package scala.scalanative
package nir
package serialization

import java.net.URI
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

import scala.collection.mutable
import scala.util.control.NonFatal

import scala.scalanative.nir.serialization.{Tags => T}
import scala.scalanative.nir.Global.{Top, Member}

import scala.annotation.{tailrec, switch}

class DeserializationException(
    global: nir.Global,
    file: String,
    compatVersion: Int,
    revision: Int,
    cause: Throwable
) extends RuntimeException(
      s"Failed to deserialize definition of ${global} defined in $file. NIR version:${compatVersion}.${revision}.",
      cause
    )

// scalafmt: { maxColumn = 120}
final class BinaryDeserializer(buffer: ByteBuffer, fileName: String) {
  import buffer._

  lazy val prelude = Prelude.readFrom(buffer, fileName)

  final def deserialize(): Seq[Defn] = {
    val allDefns = mutable.UnrolledBuffer.empty[Defn]
    offsets.foreach {
      case (global, offset) =>
        buffer.position(prelude.sections.defns + offset)
        try allDefns += getDefn()
        catch {
          case NonFatal(ex) =>
            throw new DeserializationException(
              global,
              fileName,
              compatVersion = prelude.compat,
              revision = prelude.revision,
              cause = ex
            )
        }
    }
    allDefns.toSeq
  }

  private lazy val offsets: mutable.Map[Global, Int] = {
    buffer.position(prelude.sections.offsets)
    val entries = mutable.Map.empty[Global, Int]

    while ({
      val global = getGlobal()
      val offset = getLebSignedInt()
      global match {
        case Global.None => false
        case _ =>
          entries(global) = offset
          true
      }
    }) ()

    entries
  }
  private lazy val globals = offsets.keySet

  private val cache = new mutable.LongMap[Any]
  private def in[T](start: Int)(getT: => T): T = {
    val target = start + getLebUnsignedInt()
    cache
      .getOrElseUpdate(
        target, {
          val pos = buffer.position()
          buffer.position(target)
          try getT
          finally buffer.position(pos)
        }
      )
      .asInstanceOf[T]
  }

  private def getTag(): Byte = get()

  // Leb128 decoders
  private def getLebChar(): Char = getLebUnsignedInt().toChar
  private def getLebShort(): Short = getLebSignedInt().toShort
  private def getLebSignedInt(): Int = {
    var result, shift, count = 0
    var byte: Byte = -1
    while ({
      byte = buffer.get()
      result |= (byte & 0x7f).toInt << shift
      shift += 7
      count += 1
      (byte & 0x80) != 0 && count < 5
    }) ()
    if ((byte & 0x80) == 0x80) throw new Exception("Invalid LEB128 sequence")

    // Sign extend
    if (shift < 32 && (byte & 0x40) != 0) {
      result |= (-1 << shift)
    }
    result
  }

  private def getLebSignedLong(): Long = {
    var result = 0L
    var shift, count = 0
    var byte: Byte = -1
    while ({
      byte = buffer.get()
      result |= (byte & 0x7f).toLong << shift
      shift += 7
      count += 1
      (byte & 0x80) != 0 && count < 10
    }) ()

    if ((byte & 0x80) == 0x80) throw new Exception("Invalid LEB128 sequence")
    // Sign extend
    if (shift < 64 && (byte & 0x40) != 0) {
      result |= (-1L << shift)
    }
    result
  }

  def getLebUnsignedInt(): Int = {
    var result, shift, count = 0
    var byte: Byte = -1
    while ({
      byte = buffer.get()
      result |= (byte & 0x7f) << shift
      shift += 7
      count += 1
      (byte & 0x80) != 0 && count < 5
    }) ()
    if ((byte & 0x80) == 0x80) throw new Exception("Invalid LEB128 sequence")
    result
  }

  def getLebUnsignedLong(): Long = {
    var result = 0L
    var shift, count = 0
    var byte: Byte = -1
    while ({
      byte = buffer.get()
      result |= (byte & 0x7f).toLong << shift
      shift += 7
      count += 1
      (byte & 0x80) != 0 && count < 10
    }) ()
    if ((byte & 0x80) == 0x80) throw new Exception("Invalid LEB128 sequence")
    result
  }

  private def getSeq[T](getT: => T): Seq[T] =
    Seq.fill(getLebUnsignedInt())(getT)
  private def getOpt[T](getT: => T): Option[T] =
    if (get == 0) None
    else Some(getT)

  private def getString(): String = in(prelude.sections.strings) {
    val chars = Array.fill(getLebUnsignedInt())(getLebChar())
    new String(chars)
  }

  private def getBytes(): Array[Byte] = {
    val arr = new Array[Byte](getLebUnsignedInt())
    get(arr)
    arr
  }

  private def getBool(): Boolean = get != 0

  private def getAttrs(): Attrs = Attrs.fromSeq(getSeq(getAttr()))
  private def getAttr(): Attr = (getTag(): @switch) match {
    case T.MayInlineAttr    => Attr.MayInline
    case T.InlineHintAttr   => Attr.InlineHint
    case T.NoInlineAttr     => Attr.NoInline
    case T.AlwaysInlineAttr => Attr.AlwaysInline

    case T.MaySpecialize => Attr.MaySpecialize
    case T.NoSpecialize  => Attr.NoSpecialize

    case T.UnOptAttr   => Attr.UnOpt
    case T.NoOptAttr   => Attr.NoOpt
    case T.DidOptAttr  => Attr.DidOpt
    case T.BailOptAttr => Attr.BailOpt(getString())

    case T.DynAttr      => Attr.Dyn
    case T.StubAttr     => Attr.Stub
    case T.ExternAttr   => Attr.Extern(getBool())
    case T.LinkAttr     => Attr.Link(getString())
    case T.AbstractAttr => Attr.Abstract
    case T.VolatileAttr => Attr.Volatile
    case T.FinalAttr    => Attr.Final

    case T.LinktimeResolvedAttr => Attr.LinktimeResolved
  }

  private def getBin(): Bin = (getTag(): @switch) match {
    case T.IaddBin => Bin.Iadd
    case T.FaddBin => Bin.Fadd
    case T.IsubBin => Bin.Isub
    case T.FsubBin => Bin.Fsub
    case T.ImulBin => Bin.Imul
    case T.FmulBin => Bin.Fmul
    case T.SdivBin => Bin.Sdiv
    case T.UdivBin => Bin.Udiv
    case T.FdivBin => Bin.Fdiv
    case T.SremBin => Bin.Srem
    case T.UremBin => Bin.Urem
    case T.FremBin => Bin.Frem
    case T.ShlBin  => Bin.Shl
    case T.LshrBin => Bin.Lshr
    case T.AshrBin => Bin.Ashr
    case T.AndBin  => Bin.And
    case T.OrBin   => Bin.Or
    case T.XorBin  => Bin.Xor
  }

  private def getInsts(): Seq[Inst] = in(prelude.sections.insts) {
    getSeq(getInst())
  }
  private def getInst(): Inst = {
    val tag = getTag()
    implicit val pos: nir.Position = getPosition()
    (tag: @switch) match {
      case T.LabelInst       => Inst.Label(getLocal(), getParams())
      case T.LetInst         => Inst.Let(getLocal(), getOp(), Next.None)
      case T.LetUnwindInst   => Inst.Let(getLocal(), getOp(), getNext())
      case T.RetInst         => Inst.Ret(getVal())
      case T.JumpInst        => Inst.Jump(getNext())
      case T.IfInst          => Inst.If(getVal(), getNext(), getNext())
      case T.SwitchInst      => Inst.Switch(getVal(), getNext(), getNexts())
      case T.ThrowInst       => Inst.Throw(getVal(), getNext())
      case T.UnreachableInst => Inst.Unreachable(getNext())
      case T.LinktimeIfInst =>
        Inst.LinktimeIf(getLinktimeCondition(), getNext(), getNext())
    }
  }

  private def getComp(): Comp = (getTag(): @switch) match {
    case T.IeqComp => Comp.Ieq
    case T.IneComp => Comp.Ine
    case T.UgtComp => Comp.Ugt
    case T.UgeComp => Comp.Uge
    case T.UltComp => Comp.Ult
    case T.UleComp => Comp.Ule
    case T.SgtComp => Comp.Sgt
    case T.SgeComp => Comp.Sge
    case T.SltComp => Comp.Slt
    case T.SleComp => Comp.Sle

    case T.FeqComp => Comp.Feq
    case T.FneComp => Comp.Fne
    case T.FgtComp => Comp.Fgt
    case T.FgeComp => Comp.Fge
    case T.FltComp => Comp.Flt
    case T.FleComp => Comp.Fle
  }

  private def getConv(): Conv = (getTag(): @switch) match {
    case T.TruncConv     => Conv.Trunc
    case T.ZextConv      => Conv.Zext
    case T.SextConv      => Conv.Sext
    case T.FptruncConv   => Conv.Fptrunc
    case T.FpextConv     => Conv.Fpext
    case T.FptouiConv    => Conv.Fptoui
    case T.FptosiConv    => Conv.Fptosi
    case T.UitofpConv    => Conv.Uitofp
    case T.SitofpConv    => Conv.Sitofp
    case T.PtrtointConv  => Conv.Ptrtoint
    case T.InttoptrConv  => Conv.Inttoptr
    case T.BitcastConv   => Conv.Bitcast
    case T.SSizeCastConv => Conv.SSizeCast
    case T.ZSizeCastConv => Conv.ZSizeCast
  }

  private def getDefn(): Defn = {
    val tag = getTag()
    val name = getGlobal()
    val attrs = getAttrs()
    implicit val position: nir.Position = getPosition()
    (tag: @switch) match {
      case T.VarDefn     => Defn.Var(attrs, name, getType(), getVal())
      case T.ConstDefn   => Defn.Const(attrs, name, getType(), getVal())
      case T.DeclareDefn => Defn.Declare(attrs, name, getType())
      case T.DefineDefn  => Defn.Define(attrs, name, getType(), getInsts())
      case T.TraitDefn   => Defn.Trait(attrs, name, getGlobals())
      case T.ClassDefn   => Defn.Class(attrs, name, getGlobalOpt(), getGlobals())
      case T.ModuleDefn  => Defn.Module(attrs, name, getGlobalOpt(), getGlobals())
    }
  }

  private def getGlobals(): Seq[Global] = getSeq(getGlobal())
  private def getGlobalOpt(): Option[Global] = getOpt(getGlobal())
  private def getGlobal(): Global = in(prelude.sections.globals) {
    (getTag(): @switch) match {
      case T.NoneGlobal   => Global.None
      case T.TopGlobal    => Global.Top(getString())
      case T.MemberGlobal => Global.Member(getGlobal(), getSig())
    }
  }

  private def getSig(): Sig = new Sig(getString())

  private def getLocal(): Local = Local(getLebUnsignedLong())

  private def getNexts(): Seq[Next] = getSeq(getNext())
  private def getNext(): Next = (getTag(): @switch) match {
    case T.NoneNext   => Next.None
    case T.UnwindNext => Next.Unwind(getParam(), getNext())
    case T.CaseNext   => Next.Case(getVal(), getNext())
    case T.LabelNext  => Next.Label(getLocal(), getVals())
  }

  private def getOp(): Op = {
    (getTag(): @switch) match {
      case T.CallOp       => Op.Call(getType(), getVal(), getVals())
      case T.LoadOp       => Op.Load(getType(), getVal(), None)
      case T.LoadSyncOp   => Op.Load(getType(), getVal(), Some(getSyncAttrs()))
      case T.StoreOp      => Op.Store(getType(), getVal(), getVal(), None)
      case T.StoreSyncOp  => Op.Store(getType(), getVal(), getVal(), Some(getSyncAttrs()))
      case T.ElemOp       => Op.Elem(getType(), getVal(), getVals())
      case T.ExtractOp    => Op.Extract(getVal(), getSeq(getLebSignedInt()))
      case T.InsertOp     => Op.Insert(getVal(), getVal(), getSeq(getLebSignedInt()))
      case T.StackallocOp => Op.Stackalloc(getType(), getVal())
      case T.BinOp        => Op.Bin(getBin(), getType(), getVal(), getVal())
      case T.CompOp       => Op.Comp(getComp(), getType(), getVal(), getVal())
      case T.ConvOp       => Op.Conv(getConv(), getType(), getVal())
      case T.FenceOp      => Op.Fence(getSyncAttrs())

      case T.ClassallocOp  => Op.Classalloc(getGlobal())
      case T.FieldloadOp   => Op.Fieldload(getType(), getVal(), getGlobal())
      case T.FieldstoreOp  => Op.Fieldstore(getType(), getVal(), getGlobal(), getVal())
      case T.FieldOp       => Op.Field(getVal(), getGlobal())
      case T.MethodOp      => Op.Method(getVal(), getSig())
      case T.DynmethodOp   => Op.Dynmethod(getVal(), getSig())
      case T.ModuleOp      => Op.Module(getGlobal())
      case T.AsOp          => Op.As(getType(), getVal())
      case T.IsOp          => Op.Is(getType(), getVal())
      case T.CopyOp        => Op.Copy(getVal())
      case T.BoxOp         => Op.Box(getType(), getVal())
      case T.UnboxOp       => Op.Unbox(getType(), getVal())
      case T.VarOp         => Op.Var(getType())
      case T.VarloadOp     => Op.Varload(getVal())
      case T.VarstoreOp    => Op.Varstore(getVal(), getVal())
      case T.ArrayallocOp  => Op.Arrayalloc(getType(), getVal())
      case T.ArrayloadOp   => Op.Arrayload(getType(), getVal(), getVal())
      case T.ArraystoreOp  => Op.Arraystore(getType(), getVal(), getVal(), getVal())
      case T.ArraylengthOp => Op.Arraylength(getVal())
      case T.SizeOfOp      => Op.SizeOf(getType())
      case T.AlignmentOfOp => Op.AlignmentOf(getType())
    }
  }

  private def getParams(): Seq[Val.Local] = getSeq(getParam())
  private def getParam(): Val.Local = Val.Local(getLocal(), getType())

  private def getTypes(): Seq[Type] = getSeq(getType())
  private def getType(): Type = in(prelude.sections.types) {
    (getTag(): @switch) match {
      case T.VarargType      => Type.Vararg
      case T.PtrType         => Type.Ptr
      case T.BoolType        => Type.Bool
      case T.CharType        => Type.Char
      case T.ByteType        => Type.Byte
      case T.ShortType       => Type.Short
      case T.IntType         => Type.Int
      case T.LongType        => Type.Long
      case T.FloatType       => Type.Float
      case T.DoubleType      => Type.Double
      case T.ArrayValueType  => Type.ArrayValue(getType(), getLebUnsignedInt())
      case T.StructValueType => Type.StructValue(getTypes())
      case T.FunctionType    => Type.Function(getTypes(), getType())

      case T.NullType    => Type.Null
      case T.NothingType => Type.Nothing
      case T.VirtualType => Type.Virtual
      case T.VarType     => Type.Var(getType())
      case T.UnitType    => Type.Unit
      case T.ArrayType   => Type.Array(getType(), getBool())
      case T.RefType     => Type.Ref(getGlobal(), getBool(), getBool())
      case T.SizeType    => Type.Size
    }
  }

  private def getVals(): Seq[Val] = getSeq(getVal())
  private def getVal(): Val = in(prelude.sections.vals) {
    (getTag(): @switch) match {
      case T.TrueVal        => Val.True
      case T.FalseVal       => Val.False
      case T.NullVal        => Val.Null
      case T.ZeroVal        => Val.Zero(getType())
      case T.ByteVal        => Val.Byte(get())
      case T.CharVal        => Val.Char(getLebChar())
      case T.ShortVal       => Val.Short(getLebShort())
      case T.IntVal         => Val.Int(getLebSignedInt())
      case T.LongVal        => Val.Long(getLebSignedLong())
      case T.FloatVal       => Val.Float(getFloat)
      case T.DoubleVal      => Val.Double(getDouble)
      case T.StructValueVal => Val.StructValue(getVals())
      case T.ArrayValueVal  => Val.ArrayValue(getType(), getVals())
      case T.ByteStringVal  => Val.ByteString(getBytes())
      case T.LocalVal       => Val.Local(getLocal(), getType())
      case T.GlobalVal      => Val.Global(getGlobal(), getType())

      case T.UnitVal    => Val.Unit
      case T.ConstVal   => Val.Const(getVal())
      case T.StringVal  => Val.String(getString())
      case T.VirtualVal => Val.Virtual(getLebUnsignedLong())
      case T.ClassOfVal => Val.ClassOf(getGlobal())
      case T.SizeVal    => Val.Size(getLebUnsignedLong())
    }
  }

  private def getSyncAttrs(): SyncAttrs =
    SyncAttrs(
      memoryOrder = getMemoryOrder(),
      isVolatile = getBool()
    )

  private def getMemoryOrder(): MemoryOrder = (getTag(): @switch) match {
    case T.Unordered      => MemoryOrder.Unordered
    case T.MonotonicOrder => MemoryOrder.Monotonic
    case T.AcquireOrder   => MemoryOrder.Acquire
    case T.ReleaseOrder   => MemoryOrder.Release
    case T.AcqRelOrder    => MemoryOrder.AcqRel
    case T.SeqCstOrder    => MemoryOrder.SeqCst
  }

  private def getLinktimeCondition(): LinktimeCondition =
    (getTag(): @switch) match {
      case LinktimeCondition.Tag.SimpleCondition =>
        LinktimeCondition.SimpleCondition(
          propertyName = getString(),
          comparison = getComp(),
          value = getVal()
        )(getPosition())

      case LinktimeCondition.Tag.ComplexCondition =>
        LinktimeCondition.ComplexCondition(
          op = getBin(),
          left = getLinktimeCondition(),
          right = getLinktimeCondition()
        )(getPosition())

      case n => util.unsupported(s"Unknown linktime condition tag: ${n}")
    }

  // Ported from Scala.js
  private var lastPosition = Position.NoPosition
  def getPosition(): Position = in(prelude.sections.positions) {
    import PositionFormat._
    val first = get()
    if (first == FormatNoPositionValue) {
      Position.NoPosition
    } else {
      val position = if ((first & FormatFullMask) == FormatFullMaskValue) {
        val file = new URI(getString())
        val line = getLebUnsignedInt()
        val column = getLebUnsignedInt()
        Position(file, line, column)
      } else {
        assert(
          lastPosition != Position.NoPosition,
          "Position format error: first position must be full" +
            s", file=$fileName, first=$first"
        )
        if ((first & Format1Mask) == Format1MaskValue) {
          val columnDiff = first >> Format1Shift
          Position(
            lastPosition.source,
            lastPosition.line,
            lastPosition.column + columnDiff
          )
        } else if ((first & Format2Mask) == Format2MaskValue) {
          val lineDiff = first >> Format2Shift
          val column = get() & 0xff // unsigned
          Position(lastPosition.source, lastPosition.line + lineDiff, column)
        } else {
          assert(
            (first & Format3Mask) == Format3MaskValue,
            s"Position format error: first byte $first does not match any format"
          )
          val lineDiff = getShort()
          val column = get() & 0xff // unsigned
          Position(lastPosition.source, lastPosition.line + lineDiff, column)
        }
      }
      lastPosition = position
      position
    }
  }
}
