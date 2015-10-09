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
    } else {
      getDesc match {
        case Desc.Empty =>
          Empty
        case Desc.Primitive(name) =>
          name match {
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
        case Desc.Extern(Name.Class("java.lang.Object")) =>
          Prim.Object
        case Desc.Extern(name @ Name.Method(Name.Class("java.lang.Object"), _, _, _)) =>
          Prim.Object.resolve(name).get
        case Desc.Extern(name) =>
          extern(name)
        case desc =>
          val node = Node(desc, Array())
          nodes += pos -> node
          if (desc.schema.nonEmpty) {
            node.offsets = getOffsets
            node.slots = getNodeRefs.map(new Node.Slot(node, _))
          }
          node
      }
    }
  }

  def detour[T](f: => T): T = {
    val pos = position
    val res = f
    position(pos)
    res
  }

  private def getNodeRef = {
    val pos = getInt
    detour {
      position(pos)
      getNode
    }
  }

  private def getDesc(): Desc = getInt match {
    case T.Label     => Desc.Label(getString)
    case T.Param     => Desc.Param(getString)
    case T.I8        => Desc.I8(get)
    case T.I16       => Desc.I16(getShort)
    case T.I32       => Desc.I32(getInt)
    case T.I64       => Desc.I64(getLong)
    case T.F32       => Desc.F32(getFloat)
    case T.F64       => Desc.F64(getDouble)
    case T.Str       => Desc.Str(getString)
    case T.Class     => Desc.Class(getName)
    case T.Interface => Desc.Interface(getName)
    case T.Module    => Desc.Module(getName)
    case T.Declare   => Desc.Declare(getName)
    case T.Define    => Desc.Define(getName)
    case T.Field     => Desc.Field(getName)
    case T.Extern    => Desc.Extern(getName)
    case T.Type      => Desc.Type(getShape)
    case T.Primitive => Desc.Primitive(getName)
    case tag         => T.tag2plain(tag)
  }

  private def getOffsets(): Array[Int] = getSeq(getInt).toArray

  private def getNodeRefs(): Array[Node] = getSeq(getNodeRef).toArray

  private def getName(): Name = getInt match {
    case T.NoName        => Name.No
    case T.ClassName     => Name.Class(getString)
    case T.ModuleName    => Name.Module(getString)
    case T.InterfaceName => Name.Interface(getString)
    case T.PrimitiveName => Name.Primitive(getString)
    case T.SliceName     => Name.Slice(getName)
    case T.FieldName     => Name.Field(getName, getString)
    case T.MethodName    => Name.Method(getName, getString, getSeq(getName), getName)
  }

  private def getSeq[T](getT: => T): Seq[T] =
    (1 to getInt).map(_ => getT).toSeq

  private def getShape(): Shape = getInt match {
    case T.HoleShape  => Shape.Hole
    case T.RefShape   => Shape.Ref(getShape)
    case T.SliceShape => Shape.Slice(getShape)
  }

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


