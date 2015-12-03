package native
package ir
package serialization

import java.nio.ByteBuffer
import java.nio.file.Paths
import java.nio.channels.FileChannel
import scala.collection.mutable
import native.ir.{Tags => T}
import native.ir.Desc.Schema

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
        case Desc.Empty        => Empty
        case Desc.Prim.Unit    => Prim.Unit
        case Desc.Prim.Bool    => Prim.Bool
        case Desc.Prim.I8      => Prim.I8
        case Desc.Prim.I16     => Prim.I16
        case Desc.Prim.I32     => Prim.I32
        case Desc.Prim.I64     => Prim.I64
        case Desc.Prim.F32     => Prim.F32
        case Desc.Prim.F64     => Prim.F64
        case Desc.Prim.Null    => Prim.Null
        case Desc.Prim.Nothing => Prim.Nothing
        case Desc.Defn.Extern  => extern(getAttrs)
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
    case T.I8Lit     => Desc.Lit.I8(get)
    case T.I16Lit    => Desc.Lit.I16(getShort)
    case T.I32Lit    => Desc.Lit.I32(getInt)
    case T.I64Lit    => Desc.Lit.I64(getLong)
    case T.F32Lit    => Desc.Lit.F32(getFloat)
    case T.F64Lit    => Desc.Lit.F64(getDouble)
    case T.StrLit    => Desc.Lit.Str(getString)
    case T.ArrayDefn => Desc.Defn.Array(getInt)
    case tag         => T.tag2plain(tag)
  }

  private def getAttrs(): Seq[Attr] = getSeq(getPersistentAttr)

  private def getPersistentAttr(): PersistentAttr = getInt match {
    case tag if tag >= T.NoName && tag <= T.ForeignName => getName(tag)
  }

  private def getOffsets(): Array[Int] = getSeq(getInt).toArray

  private def getSlots(node: Node): Array[Slot] = getSeq {
    val sc = getSchema
    val next = getNodeRef
    new Slot(sc, node, next)
  }.toArray

  private def getSchema: Schema = getInt match {
    case T.ValSchema  => Desc.Val
    case T.CfSchema   => Desc.Cf
    case T.EfSchema   => Desc.Ef
    case T.RefSchema  => Desc.Ref
    case T.ManySchema => Desc.Many(getSchema)
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
    case T.PrimName           => Name.Prim(getString)
    case T.LocalName          => Name.Local(getString)
    case T.ClassName          => Name.Class(getString)
    case T.ModuleName         => Name.Module(getString)
    case T.InterfaceName      => Name.Interface(getString)
    case T.AccessorName       => Name.Accessor(getName)
    case T.DataName           => Name.Data(getName)
    case T.VtableName         => Name.Vtable(getName)
    case T.VtableConstantName => Name.VtableConstant(getName)
    case T.ArrayName          => Name.Array(getName)
    case T.FieldName          => Name.Field(getName, getString)
    case T.ConstructorName    => Name.Constructor(getName, getSeq(getName))
    case T.MethodName         => Name.Method(getName, getString, getSeq(getName), getName)
    case T.ForeignName        => Name.Foreign(getName, getString)
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

  def resolve(name: Name): Option[Node] =
    nametable.get(name).map { pos => position(pos); getNode }

  final def scope(): Scope =
    Scope(nametable.map { case (n, _) => (n, resolve(n).get) })
}


