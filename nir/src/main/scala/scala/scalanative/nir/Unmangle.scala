package scala.scalanative
package nir

object Unmangle {
  def unmangleGlobal(s: String): Global     = (new Impl(s)).readGlobal()
  def unmangleType(s: String): Type         = (new Impl(s)).readType()
  def unmangleSig(s: String): Sig.Unmangled = (new Impl(s)).readUnmangledSig()

  private class Impl(s: String) {
    val chars = s.toArray
    var pos   = 0

    def readGlobal(): Global = read() match {
      case 'T' =>
        Global.Top(readIdent())
      case 'M' =>
        Global.Member(Global.Top(readIdent()), readUnmangledSig().mangled)
      case ch =>
        error(s"expected global, but got $ch")
    }

    def readUnmangledSig(): Sig.Unmangled = read() match {
      case 'F' =>
        Sig.Field(readIdent())
      case 'R' =>
        Sig.Ctor(readTypes())
      case 'D' =>
        Sig.Method(readIdent(), readTypes())
      case 'P' =>
        Sig.Proxy(readIdent(), readTypes())
      case 'C' =>
        Sig.Extern(readIdent())
      case 'G' =>
        Sig.Generated(readIdent())
      case 'K' =>
        Sig.Duplicate(readUnmangledSig(), readTypes())
      case ch =>
        error(s"expected sig, but got $ch")
    }

    def readType(): Type = peek() match {
      case 'v' =>
        next()
        Type.Vararg
      case 'R' =>
        next()
        peek() match {
          case '_' =>
            next()
            Type.Ptr
          case _ =>
            val types = readTypes()
            Type.Function(types.init, types.last)
        }
      case 'z' =>
        next()
        Type.Bool
      case 'c' =>
        next()
        Type.Char
      case 'b' =>
        next()
        Type.Byte
      case 's' =>
        next()
        Type.Short
      case 'i' =>
        next()
        Type.Int
      case 'j' =>
        next()
        Type.Long
      case 'f' =>
        next()
        Type.Float
      case 'd' =>
        next()
        Type.Double
      case 'l' =>
        next()
        Type.Null
      case 'n' =>
        next()
        Type.Nothing
      case 'u' =>
        next()
        Type.Unit
      case 'A' =>
        next()
        val ty = readType()
        peek() match {
          case '_' =>
            next()
            Type.Array(ty, nullable = false)
          case n if '0' <= n && n <= '9' =>
            val res = Type.ArrayValue(ty, readNumber())
            accept('_')
            res
          case ch =>
            error(s"expected digit or _, but got $ch")
        }
      case 'S' =>
        next()
        Type.StructValue(readTypes())
      case 'L' =>
        next()
        readNullableType()
      case 'X' =>
        next()
        Type.Ref(Global.Top(readIdent()), exact = true, nullable = false)
      case n if '0' <= n && n <= '9' =>
        Type.Ref(Global.Top(readIdent()), exact = false, nullable = false)
      case ch =>
        error(s"expected type, but got $ch")
    }

    def readNullableType(): Type = peek() match {
      case 'A' =>
        next()
        val ty = readType()
        accept('_')
        Type.Array(ty, nullable = true)
      case 'X' =>
        next()
        Type.Ref(Global.Top(readIdent()), exact = true, nullable = true)
      case n if '0' <= n && n <= '9' =>
        Type.Ref(Global.Top(readIdent()), exact = false, nullable = true)
      case ch =>
        error(s"expected nullable qualifier, but got $ch")
    }

    def readTypes(): Seq[Type] = {
      val buf = collection.mutable.UnrolledBuffer.empty[Type]
      while (peek() != 'E') {
        buf += readType()
      }
      next()
      buf
    }

    def readIdent(): String = {
      val len   = readNumber()
      val start = pos
      pos += len
      s.substring(start, pos)
    }

    def readNumber(): Int = {
      val start = pos
      var char  = peek()
      while ('0' <= char && char <= '9') {
        next()
        char = peek()
      }
      java.lang.Integer.parseInt(s.substring(start, pos))
    }

    def peek(): Char =
      chars(pos)

    def next(): Unit =
      pos += 1

    def accept(expected: Char): Unit = {
      val got = peek()
      if (got != expected) {
        error(s"expected $expected but got $got")
      }
      next()
    }

    def error(msg: String): Nothing =
      throw new Exception(s"at $pos: $msg")

    def read(): Int = {
      val value = chars(pos)
      pos += 1
      value
    }
  }
}
