package scala.scalanative.codegen.dwarf

import DwarfSection._
import DwarfDef._
import Val.Str
import Val.Num
import scala.scalanative.nir.Position
import scala.reflect.ClassTag

trait GenIdx {
  def id(tok: DwarfSection.Token): Int
  def gen: DwarfSection.Token
}
object GenIdx {
  def create = new GenIdx {
    private val mp = collection.mutable.Map.empty[Token, Int]
    override def id(tok: Token): Int = mp(tok)
    override def gen: Token = {
      val newVal = new Token
      val newIdx = mp.size

      mp.synchronized {
        mp.update(newVal, newIdx)
      }

      newVal
    }
  }
}

case class I[+T](tok: DwarfSection.Token, value: T) {
  def id(implicit gidx: GenIdx) = gidx.id(tok)
}

sealed trait Val {
  import Val._
  def render = this match {
    case Str(raw)   => s"""  "$raw" """.trim // TODO: escaping!!
    case Num(raw)   => raw.toString
    case Const(raw) => raw.toString
    case Bool(raw)  => raw.toString
    case Ref(raw)   => "!" + raw.toString
  }
}
object Val {
  case class Str(raw: String) extends Val
  case class Const(raw: String) extends Val
  case class Num(raw: Int) extends Val
  case class Ref(raw: Int) extends Val
  case class Bool(raw: Boolean) extends Val

  implicit class IOps(i: Int) {
    def v: Val = Num(i)
  }
  implicit class BOps(i: Boolean) {
    def v: Val = Bool(i)
  }
  implicit class SOps(s: String) {
    def v: Val = Str(s)
    def const = Const(s)
  }
  implicit class TOps(s: Token) {
    def v(implicit gidx: GenIdx): Val = Ref(gidx.id(s))
  }
}

sealed trait DwarfDef extends Product with Serializable {

  import Val._

  private def fields(mp: (String, Val)*) =
    mp.map { case (k, v) => k + ": " + v.render }.mkString("(", ", ", ")")

  private def inst(nm: String, mp: (String, Val)*) =
    "!" + nm + fields(mp: _*)

  private def tuple(values: String*) =
    "!" + values.mkString("{", ", ", "}")

  def render(implicit gidx: GenIdx): String = this match {
    case `llvm.dbg.cu`(cus) =>
      tuple(cus.map(_.tok.v.render): _*)

    case dic: DICompileUnit =>
      "distinct " + inst(
        "DICompileUnit",
        "producer" -> dic.producer.v,
        "file" -> dic.file.tok.v,
        "language" -> "DW_LANG_C_plus_plus".const // TODO: update once SN has its own DWARF language code
      )

    case fb: FlagBag =>
      tuple(fb.flags.map(_.tok.render): _*)

    case IntAttr(conflictType, name, value) =>
      tuple(
        s"i32 $conflictType",
        "!" + Val.Str(name).render,
        s"i32 $value"
      )

    case DILocation(line, column, scope) =>
      inst(
        "DILocation",
        "line" -> line.v,
        "column" -> column.v,
        "scope" -> scope.tok.v
      )

    case DIFile(name, directory) =>
      inst("DIFile", "filename" -> name.v, "directory" -> directory.v)

    case dis: DISubprogram =>
      import dis._
      val dst = if (distinct) "distinct " else ""
      dst + inst(
        "DISubprogram",
        // "name" -> name.v,
        "linkageName" -> linkageName.v,
        "isDefinition" -> true.v,
        "scope" -> scope.tok.v,
        "unit" -> unit.tok.v,
        "file" -> file.tok.v,
        "line" -> line.v,
        "scopeLine" -> line.v,
        "type" -> tpe.tok.v,
        "spFlags" -> "DISPFlagDefinition".const
      )

    case DILocalVariable(name, scope, file, line, tpe) =>
      inst(
        "DILocalVariable",
        "name" -> name.v,
        "scope" -> scope.tok.v,
        "file" -> file.tok.v,
        "line" -> line.v,
        "type" -> tpe.tok.v
      )

    case DIBasicType(name, size) =>
      inst("DIBasicType", "name" -> name.v, "size" -> size.v)

    case DIDerivedType(tag, baseType, size) =>
      inst(
        "DIDerivedType",
        "tag" -> tag.tg.const,
        "baseType" -> baseType.tok.v,
        "size" -> size.v
      )

    case DISubroutineType(types) =>
      inst("DISubroutineType", "types" -> types.tok.v)

    case DITypes(retTpe, arguments) =>
      (retTpe +: arguments).map(_.tok.v.render).mkString("!{", ", ", "}")
  }
}
object DwarfDef {
  sealed trait Type extends DwarfDef
  sealed trait Scope extends DwarfDef
  sealed abstract class Named(val name: String) extends DwarfDef

  case class IntAttr(conflictType: Int, name: String, value: Int)
      extends DwarfDef

  abstract class FlagBag(override val name: String, val flags: Seq[I[IntAttr]])
      extends Named(name)

  case class `llvm.module.flags`(override val flags: Seq[I[IntAttr]])
      extends FlagBag("llvm.module.flags", flags)

  case class `llvm.dbg.cu`(cus: Seq[I[DICompileUnit]])
      extends Named("llvm.dbg.cu")

  case class DICompileUnit(
      file: I[DIFile],
      producer: String,
      isOptimised: Boolean
  ) extends Scope

  case class DILocation(line: Int, column: Int, scope: I[Scope])
      extends DwarfDef

  case class DIFile(name: String, directory: String) extends Scope

  case class DISubprogram(
      name: String,
      linkageName: String,
      scope: I[Scope],
      file: I[DIFile],
      line: Int,
      tpe: I[DISubroutineType],
      unit: I[DICompileUnit],
      distinct: Boolean
  ) extends Scope

  case class DILocalVariable(
      name: String,
      scope: I[Scope],
      file: I[DwarfDef],
      line: Int,
      tpe: I[Type]
  ) extends DwarfDef

  case class DIBasicType(name: String, size: Int) extends Type

  sealed abstract class DWTag(val tg: String)
      extends Product
      with Serializable {}

  object DWTag {
    case object Pointer extends DWTag("DW_TAG_pointer_type")
  }

  case class DIDerivedType(tag: DWTag, baseType: I[Type], size: Int)
      extends Type
  case class DISubroutineType(types: I[DITypes]) extends Type
  case class DITypes(retTpe: I[Type], arguments: Seq[I[Type]]) extends DwarfDef
}

object DwarfSection {
  class Token {
    def render(implicit gidx: GenIdx) = s"!${gidx.id(this)}"
  }
  class Builder[In](implicit gidx: GenIdx) {
    private val b = collection.mutable.Map
      .empty[In, I[DwarfDef]]

    private val anon = collection.mutable.ListBuffer.empty[I[DwarfDef]]

    private val globals = collection.mutable.Map
      .empty[DwarfDef, I[DwarfDef]]

    private val keyCache = collection.mutable.Map.empty[Any, I[DwarfDef]]

    def register[T <: DwarfDef](in: In, dwarf: T): I[T] = {
      val newTok = gidx.gen
      b.synchronized {
        if (b.contains(in))
          throw new Exception(s"[dwarf] attempt to overwrite `$in` key")

        val computed = I(newTok, dwarf)
        b.update(in, computed)

        computed
      }
    }

    def put(in: In, dwarf: I[DwarfDef]) = {
      b.synchronized {
        b.update(in, dwarf)
      }
    }

    def fileFromPosition(pos: Position): I[DwarfDef.DIFile] = {
      cachedBy(pos.source, DwarfDef.DIFile(pos.filename, pos.dir))
    }

    def fileLocation(pos: Position): I[DwarfDef.DILocation] =
      cachedBy(
        pos,
        DwarfDef.DILocation(pos.line, pos.column, fileFromPosition(pos))
      )
    def scopeLocation(
        pos: Position,
        scope: I[DwarfDef.Scope]
    ): I[DwarfDef.DILocation] =
      cachedBy(
        pos,
        DwarfDef.DILocation(pos.line, pos.column, scope)
      )

    def cached[T <: DwarfDef](in: DwarfDef): I[T] = globals.synchronized {
      globals.getOrElseUpdate(in, I(gidx.gen, in)).asInstanceOf[I[T]]
    }

    def cachedBy[K, T <: DwarfDef](k: K, in: => DwarfDef)(implicit
        ct: ClassTag[T]
    ): I[T] =
      keyCache.synchronized {
        keyCache
          .getOrElseUpdate(k -> ct, I(gidx.gen, in))
          .asInstanceOf[I[T]]
      }

    def anon[T <: DwarfDef](dwarf: T): I[T] = {
      val newTok = gidx.gen
      val res = I(newTok, dwarf)

      anon += res

      res
    }

    def get[T <: DwarfDef](in: In) =
      b(in).asInstanceOf[I[T]]

    import scalanative.util.ShowBuilder

    def render(implicit show: ShowBuilder) = {
      val tips = b.values.toSeq ++
        anon.result() ++ globals.values.toSeq ++ keyCache.values.toSeq

      val compileUnits = anon(`llvm.dbg.cu`(tips.collect {
        case cu @ I(tok, dic: DICompileUnit) =>
          I(tok, dic)
      }))

      show.newline()
      (compileUnits +: tips)
        .map(i => i.id -> i)
        .sortBy(_._1)
        .foreach {
          case (_, I(_, dfn: Named)) =>
            show.line(s"!${dfn.name} = ${dfn.render}")
          case (id, I(_, dfn)) =>
            show.line(s"!$id = ${dfn.render}")
        }
    }
  }

  def builder[In]()(implicit gidx: GenIdx) = new Builder[In]
}
