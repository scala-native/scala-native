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
    if (nodes.contains(pos)) {
      nodes(pos)
    } else
      getDesc match {
        case Desc.Empty =>
          Empty
        case Desc.Primitive =>
          getName match {
            case Name.Primitive("null")        => Prim.Null
            case Name.Primitive("nothing")     => Prim.Nothing
            case Name.Primitive("unit")        => Prim.Unit
            case Name.Primitive("bool")        => Prim.Bool
            case Name.Primitive("i8")          => Prim.I8
            case Name.Primitive("i16")         => Prim.I16
            case Name.Primitive("i32")         => Prim.I32
            case Name.Primitive("i64")         => Prim.I64
            case Name.Primitive("f32")         => Prim.F32
            case Name.Primitive("f64")         => Prim.F64
            case Name.Class("java.lang.Class") => Prim.Object
            case _                             => throw new Exception("unreachable")
          }
        case Desc.Extern =>
          getName match {
            case Prim.Object.name =>
              Prim.Object
            case name @ (Name.Field(Prim.Object.name, _)       |
                         Name.Constructor(Prim.Object.name, _) |
                         Name.Method(Prim.Object.name, _, _, _)) =>
              Prim.Object.resolve(name).get
            case name =>
              extern(name)
          }
        case desc =>
          val node = Node(desc, getName)
          nodes += pos -> node
          if (desc.schema.nonEmpty) {
            node.offsets = getOffsets
            node.slots = getNodeRefs.map(new Slot(node, _))
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

  private def getOffsets(): Array[Int] = getSeq(getInt).toArray

  private def getNodeRefs(): Array[Node] = getSeq(getNodeRef).toArray

  private def getNodeRef = {
    val pos = getInt
    detour {
      position(pos)
      getNode
    }
  }

  private def getName(): Name = getInt match {
    case T.NoName          => Name.No
    case T.LocalName       => Name.Local(getString)
    case T.ClassName       => Name.Class(getString)
    case T.ModuleName      => Name.Module(getString)
    case T.InterfaceName   => Name.Interface(getString)
    case T.PrimitiveName   => Name.Primitive(getString)
    case T.SliceName       => Name.Slice(getName)
    case T.FieldName       => Name.Field(getName, getString)
    case T.ConstructorName => Name.Constructor(getName, getSeq(getName))
    case T.MethodName      => Name.Method(getName, getString, getSeq(getName), getName)
  }

  private def getSeq[T](getT: => T): Seq[T] =
    (1 to getInt).map(_ => getT).toSeq

  private def getString(): String = {
    val arr = new Array[Byte](getInt)
    get(arr)
    new String(arr)
  }

  def extern(name: Name): Node = Extern(name)

  final def resolve(name: Name): Option[Node] =
    nametable.get(name).map { pos => position(pos); getNode }

  final def scope(): Scope =
    Scope(nametable.map { case (n, _) => (n, resolve(n).get) })
}


