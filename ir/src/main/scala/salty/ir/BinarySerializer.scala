package salty.ir

import scala.collection.mutable
import java.nio.ByteBuffer
import salty.ir.{Desc => D, Tags => T}

class BinarySerializer(bb: ByteBuffer) extends  {
  import bb._

  private val offsets  = mutable.Map[Node, Int]()
  private val worklist = mutable.Stack[(Int, Node)]()

  private def serialize(scope: Scope) = {
    putInt(scope.entries.size)
    scope.entries.foreach {
      case (n, node) =>
        putName(n)
        worklist.push(position -> node)
        putInt(0)
    }
    while (worklist.nonEmpty) {
      val (pos, node) = worklist.pop()
      val offset =
        if (offsets.contains(node))
          offsets(node)
        else
          putNode(node)
      mark
      putInt(pos, offset)
      reset
    }
  }

  private def putNode(n: Node): Int = {
    val pos = position
    offsets += (n -> pos)
    putDesc(n.desc)
    if (n.desc.schema.nonEmpty)
      putSlots(n.slots)
    pos
  }

  private def putDesc(desc: Desc) = desc match {
    case plain: D.Plain    => putInt(T.plain2tag(plain))
    case D.Label(name)     => putInt(T.Label); putName(name)
    case D.Param(name)     => putInt(T.Param); putName(name)
    case D.I8(v)           => putInt(T.I8); put(v)
    case D.I16(v)          => putInt(T.I16); putShort(v)
    case D.I32(v)          => putInt(T.I32); putInt(v)
    case D.I64(v)          => putInt(T.I64); putLong(v)
    case D.F32(v)          => putInt(T.F32); putFloat(v)
    case D.F64(v)          => putInt(T.F64); putDouble(v)
    case D.Str(v)          => putInt(T.Str); putString(v)
    case D.Class(name)     => putInt(T.Class); putName(name)
    case D.Interface(name) => putInt(T.Interface); putName(name)
    case D.Module(name)    => putInt(T.Module); putName(name)
    case D.Declare(name)   => putInt(T.Declare); putName(name)
    case D.Define(name)    => putInt(T.Define); putName(name)
    case D.Field(name)     => putInt(T.Field); putName(name)
    case D.Extern(name)    => putInt(T.Extern); putName(name)
    case D.Type(shape)     => putInt(T.Type); putShape(shape)
    case D.Primitive(name) => putInt(T.Primitive); putName(name)
  }

  private def putSeq[T](seq: Seq[T])(putT: T => Unit) = {
    putInt(seq.length)
    seq.foreach(putT)
  }

  private def putSlots(slots: Array[Any]) = putSeq(slots)(putSlot)

  private def putSlot(slot: Any) = slot match {
    case n: Node =>
      putInt(T.NodeSlot)
      putNodeRef(n)
    case ns: Seq[_] =>
      putInt(T.SeqNodeSlot)
      putSeq(ns.asInstanceOf[Seq[Node]])(putNodeRef)
  }

  private def putNodeRef(n: Node) =
    if (offsets.contains(n))
      putInt(offsets(n))
    else {
      worklist.push(position -> n)
      putInt(0)
    }

  private def putName(name: Name): Unit = name match {
    case Name.No             => putInt(T.NoName)
    case Name.Simple(v)      => putInt(T.SimpleName); putString(v)
    case Name.Nested(n1, n2) => putInt(T.NestedName); putName(n1); putName(n2)
  }

  private def putShape(shape: Shape): Unit = shape match {
    case Shape.Hole     => putInt(T.HoleShape)
    case Shape.Ref(s)   => putInt(T.RefShape); putShape(s)
    case Shape.Slice(s) => putInt(T.SliceShape); putShape(s)
  }

  private def putString(v: String) = {
    val bytes = v.getBytes
    putInt(bytes.length); put(bytes)
  }
}
object BinarySerializer extends ((Scope, ByteBuffer) => Unit) {
  def apply(scope: Scope, bb: ByteBuffer): Unit =
    (new BinarySerializer(bb)).serialize(scope)
}
