package salty.ir

import scala.collection.mutable
import java.nio.ByteBuffer
import salty.ir.{Desc => D, Tags => T}

class Serializer(bb: ByteBuffer) {
  import bb._

  private val offsets  = mutable.Map[Node, Int]()
  private val worklist = mutable.Stack[(Int, Node)]()

  def serialize(scope: Scope) = {
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
    putDesc(n.desc); putSlots(n.slots)
    pos
  }

  private def putDesc(desc: Desc) = {
    putInt(desc.tag)
    desc match {
      case _: D.Plain        => ()
      case D.Label(name)     => putName(name)
      case D.Param(name)     => putName(name)
      case D.I8(v)           => put(v)
      case D.I16(v)          => putShort(v)
      case D.I32(v)          => putInt(v)
      case D.I64(v)          => putLong(v)
      case D.F32(v)          => putFloat(v)
      case D.F64(v)          => putDouble(v)
      case D.Str(v)          => putString(v)
      case D.Class(name)     => putName(name)
      case D.Interface(name) => putName(name)
      case D.Module(name)    => putName(name)
      case D.Declare(name)   => putName(name)
      case D.Define(name)    => putName(name)
      case D.Field(name)     => putName(name)
      case D.Extern(name)    => putName(name)
      case D.Type(shape)     => putShape(shape)
      case D.Primitive(name) => putName(name)
    }
  }

  private def putSeq[T](seq: Seq[T])(putT: T => Unit) = {
    putInt(seq.length)
    seq.foreach(putT)
  }

  private def putSlots(slots: Seq[Slot]) = putSeq(slots)(putSlot)

  private def putSlot(slot: Slot) = slot match {
    case Var(n) =>
      putInt(T.Var)
      putVar(n)
    case SeqVar(ns) =>
      putInt(T.SeqVar)
      putSeq(ns)(putVar)
  }

  private def putVar(n: Node) =
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
