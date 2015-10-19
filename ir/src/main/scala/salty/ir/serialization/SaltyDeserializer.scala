package salty.ir
package serialization

import java.nio.ByteBuffer
import java.nio.file.Paths
import java.nio.channels.FileChannel
import scala.collection.mutable
import salty.ir.{Tags => T}

class SaltyDeserializer(path: String) {
  private val bb = {
    val channel = FileChannel.open(Paths.get(path))
    val buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size())
    buffer.load
    buffer
  }
  import bb._

  private val nametable =
    getSeq((getName, getInt)).toMap

  private val nodes = mutable.Map.empty[Int, Node]

  private def getNode(): Node = {
    val pos = position
    if (nodes.contains(pos)) nodes(pos)
    else
      getDesc match {
        case Desc.Empty           => Empty
        case Desc.Builtin.Unit    => Builtin.Unit
        case Desc.Builtin.Bool    => Builtin.Bool
        case Desc.Builtin.I8      => Builtin.I8
        case Desc.Builtin.I16     => Builtin.I16
        case Desc.Builtin.I32     => Builtin.I32
        case Desc.Builtin.I64     => Builtin.I64
        case Desc.Builtin.F32     => Builtin.F32
        case Desc.Builtin.F64     => Builtin.F64
        case Desc.Builtin.AnyRef  => Builtin.AnyRef
        case Desc.Builtin.Null    => Builtin.Null
        case Desc.Builtin.Nothing => Builtin.Nothing
        case Desc.Defn.Extern     =>
          val attrs = getAttrs
          val name = attrs.collectFirst { case n: Name => n }
          name match {
            case Some(Builtin.AnyRef.name) =>
              Builtin.AnyRef
            case Some(name @ (Name.Field(Builtin.AnyRef.name, _)       |
                              Name.Constructor(Builtin.AnyRef.name, _) |
                              Name.Method(Builtin.AnyRef.name, _, _, _))) =>
              Builtin.AnyRef.resolve(name).get
            case _ =>
              extern(attrs)
          }
        case desc =>
          val node = Node(desc, getAttrs)
          nodes += pos -> node
          if (desc.schema.nonEmpty) {
            node._offsets = getOffsets
            node._slots = getSlots(node)
          }
          node
      }
  }

  def detour[T](f: => T): T = {
    val pos = position
    val res = f
    position(pos)
    res
  }

  private def getDesc(): Desc = getInt match {
    case T.I8   => Desc.I8(get)
    case T.I16  => Desc.I16(getShort)
    case T.I32  => Desc.I32(getInt)
    case T.I64  => Desc.I64(getLong)
    case T.F32  => Desc.F32(getFloat)
    case T.F64  => Desc.F64(getDouble)
    case T.Str  => Desc.Str(getString)
    case tag    => T.tag2plain(tag)
  }

  private def getAttrs(): Seq[Attr] = getSeq(getPersistentAttr)

  private def getPersistentAttr(): PersistentAttr = getInt match {
    case tag if tag >= T.NoName && tag <= T.MethodName => getName(tag)
  }

  private def getOffsets(): Array[Int] = getSeq(getInt).toArray

  private def getSlots(node: Node): Array[Slot] = getSeq {
    val sc = getSchema
    val next = getNodeRef
    new Slot(sc, node, next)
  }.toArray

  private def getSchema: Schema = getInt match {
    case T.ValSchema  => Schema.Val
    case T.CfSchema   => Schema.Cf
    case T.EfSchema   => Schema.Ef
    case T.RefSchema  => Schema.Ref
    case T.ManySchema => Schema.Many(getSchema)
  }

  private def getNodeRef = {
    val pos = getInt
    detour {
      position(pos)
      getNode
    }
  }

  private def getName: Name = getName(getInt)
  private def getName(tag: Int): Name = tag match {
    case T.NoName             => Name.No
    case T.MainName           => Name.Main
    case T.BuiltinName        => Name.Builtin(getString)
    case T.LocalName          => Name.Local(getString)
    case T.ClassName          => Name.Class(getString)
    case T.ClassDataName      => Name.ClassData(getName)
    case T.VtableName         => Name.Vtable(getName)
    case T.VtableConstantName => Name.VtableConstant(getName)
    case T.ModuleName         => Name.Module(getString)
    case T.ModuleAccessorName => Name.ModuleAccessor(getName)
    case T.ModuleDataName     => Name.ModuleData(getName)
    case T.InterfaceName      => Name.Interface(getString)
    case T.SliceName          => Name.Slice(getName)
    case T.FieldName          => Name.Field(getName, getString)
    case T.ConstructorName    => Name.Constructor(getName, getSeq(getName))
    case T.MethodName         => Name.Method(getName, getString, getSeq(getName), getName)
  }

  private def getSeq[T](getT: => T): Seq[T] =
    (1 to getInt).map(_ => getT).toSeq

  private def getString(): String = {
    val arr = new Array[Byte](getInt)
    get(arr)
    new String(arr)
  }

  def extern(attrs: Seq[Attr]): Node =
    Defn.Extern(attrs: _*)

  final def resolve(name: Name): Option[Node] =
    nametable.get(name).map { pos => position(pos); getNode }

  final def scope(): Scope =
    Scope(nametable.map { case (n, _) => (n, resolve(n).get) })
}


