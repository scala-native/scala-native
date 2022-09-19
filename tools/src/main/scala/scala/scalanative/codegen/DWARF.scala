package scala.scalanative.codegen

import DwarfSection._
import DwarfDef._
import Val.Str
import Val.Num

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
    case Str(raw) => s""" "$raw" """.trim // TODO: escaping!!
    case Num(raw) => raw.toString
    case Ref(raw) => "!" + raw.toString
  }
}
object Val {
  case class Str(raw: String) extends Val
  case class Num(raw: Int) extends Val
  case class Ref(raw: Int) extends Val

  implicit class IOps(i: Int) {
    def v: Val = Num(i)
  }
  implicit class SOps(s: String) {
    def v: Val = Str(s)
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

  def render(implicit gidx: GenIdx): String = this match {
    case DILocation(line, column, scope) =>
      inst(
        "DILocation",
        "line" -> line.v,
        "column" -> column.v,
        "scope" -> scope.tok.v
      )

    case DIFile(name, directory) =>
      inst("DIFile", "filename" -> name.v, "directory" -> directory.v)

    case DISubprogram(name, scope, file, line, tpe) =>
      inst(
        "DISubprogram",
        "name" -> name.v,
        "scope" -> scope.tok.v,
        "file" -> file.tok.v,
        "line" -> line.v,
        "type" -> tpe.tok.v
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
      inst("DIBasicType", "name" -> "int".v, "size" -> size.v)

    case DISubroutineType(types) =>
      inst("DISubroutineType", "types" -> types.tok.v)

    case DITypes(retTpe, arguments) =>
      (retTpe +: arguments).map(_.tok.v.render).mkString("!{", ", ", "}")
  }
}
object DwarfDef {
  sealed trait Type extends DwarfDef
  sealed trait Scope extends DwarfDef

  case class DILocation(line: Int, column: Int, scope: I[Scope])
      extends DwarfDef

  case class DIFile(name: String, directory: String) extends Scope

  case class DISubprogram(
      name: String,
      scope: I[Scope],
      file: I[DIFile],
      line: Int,
      tpe: I[DISubroutineType]
  ) extends Scope

  case class DILocalVariable(
      name: String,
      scope: I[Scope],
      file: I[DwarfDef],
      line: Int,
      tpe: I[Type]
  ) extends DwarfDef

  case class DIBasicType(name: String, size: Int) extends Type
  case class DISubroutineType(types: I[DITypes]) extends Type
  case class DITypes(retTpe: I[Type], arguments: Seq[I[Type]]) extends DwarfDef
}

case class DwarfSection private (tips: Seq[I[DwarfDef]]) {
  def render(implicit gidx: GenIdx): List[String] =
    tips
      .map(i => i.id -> i)
      .sortBy(_._1)
      .map {
        case (id, I(_, dfn)) =>
          s"!$id = ${dfn.render}"
      }
      .toList
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

    def register(in: In)(dwarf: DwarfDef) = {
      val newTok = gidx.gen

      b.update(in, I(newTok, dwarf))
    }

    def cached[T <: DwarfDef](in: DwarfDef): I[T] =
      globals.getOrElseUpdate(in, I(gidx.gen, in)).asInstanceOf[I[T]]

    def anon[T <: DwarfDef](dwarf: T): I[T] = {
      val newTok = gidx.gen
      val res = I(newTok, dwarf)

      anon += res

      res
    }

    def get[T <: DwarfDef](in: In) =
      b(in).asInstanceOf[I[T]]

    def build: DwarfSection = DwarfSection(
      b.values.toSeq ++ globals.values.toSeq ++ anon.result()
    )
  }

  def builder[In](implicit gidx: GenIdx) = new Builder[In]
}
