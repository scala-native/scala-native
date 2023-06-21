package scala.scalanative.codegen
package llvm

import DebugInformationSection._
import LLVMDebugInformation._
import Value._
import scala.scalanative.nir.{Position, Global}
import scala.reflect.ClassTag

private[llvm] trait GenIdx {
  def id(tok: DebugInformationSection.Token): Int
  def gen(): DebugInformationSection.Token
}
private[llvm] object GenIdx {
  def create = new GenIdx {
    private val mp = collection.mutable.Map.empty[Token, Int]
    override def id(tok: Token): Int = mp(tok)
    override def gen(): Token = {
      val newVal = new Token
      val newIdx = mp.size

      mp.update(newVal, newIdx)

      newVal
    }
  }
}

case class Incr[+T](tok: DebugInformationSection.Token, value: T) {
  def id(implicit dwf: DebugInformationSection.Builder): Int = dwf.gidx.id(tok)
}

sealed trait Value {
  import Value._
  def render = this match {
    case Str(raw)   => s"""  "$raw" """.trim // TODO: escaping!!
    case Num(raw)   => raw.toString
    case Const(raw) => raw.toString
    case Bool(raw)  => raw.toString
    case Ref(raw)   => "!" + raw.toString
    case NULL       => "!null"
  }
}
object Value {
  case class Str(raw: String) extends Value
  case class Const(raw: String) extends Value
  case class Num(raw: Int) extends Value
  case class Ref(raw: Int) extends Value
  case class Bool(raw: Boolean) extends Value
  case object NULL extends Value

  implicit class IOps(i: Int) {
    def v: Value = Num(i)
  }
  implicit class BOps(i: Boolean) {
    def v: Value = Bool(i)
  }
  implicit class SOps(s: String) {
    def v: Value = Str(s)
    def const = Const(s)
  }
  implicit class TOps(s: Token) {
    def v(implicit dwf: DebugInformationSection.Builder): Value = Ref(
      dwf.gidx.id(s)
    )
  }
}

sealed trait LLVMDebugInformation extends Product with Serializable {

  import Value._

  private def fields(mp: (String, Value)*) =
    mp.map { case (k, v) => k + ": " + v.render }.mkString("(", ", ", ")")

  private def inst(nm: String, mp: (String, Value)*) =
    "!" + nm + fields(mp: _*)

  private def instSeq(nm: String, mp: Seq[(String, Value)]) =
    "!" + nm + fields(mp: _*)

  private def tuple(values: String*) =
    "!" + values.mkString("{", ", ", "}")

  def render(implicit dwf: DebugInformationSection.Builder): String =
    this match {
      case `llvm.dbg.cu`(cus) =>
        tuple(cus.map(_.tok.v.render): _*)

      case dic: DICompileUnit =>
        "distinct " + inst(
          "DICompileUnit",
          "producer" -> dic.producer.v,
          "file" -> dic.file.tok.v,
          "isOptimized" -> false.v,
          "emissionKind" -> "FullDebug".const,
          "language" -> "DW_LANG_C_plus_plus".const // TODO: update once SN has its own DWARF language code
        )

      case fb: FlagBag =>
        tuple(fb.flags.map(_.tok.render): _*)

      case IntAttr(conflictType, name, value) =>
        tuple(
          s"i32 $conflictType",
          "!" + Value.Str(name).render,
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
          "file" -> file.tok.v,
          "unit" -> unit.tok.v,
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
        (retTpe.map(_.tok.v).getOrElse(Value.NULL) +: arguments.map(_.tok.v))
          .map(_.render)
          .mkString("!{", ", ", "}")
    }
}
object LLVMDebugInformation {
  object Constants {
    val PRODUCER = "Scala Native"
    val DWARF_VERSION = 3
    val DEBUG_INFO_VERSION = 3
  }
  sealed trait Type extends LLVMDebugInformation
  sealed trait Scope extends LLVMDebugInformation
  sealed abstract class Named(val name: String) extends LLVMDebugInformation

  case class IntAttr(conflictType: Int, name: String, value: Int)
      extends LLVMDebugInformation

  abstract class FlagBag(
      override val name: String,
      val flags: Seq[Incr[IntAttr]]
  ) extends Named(name)

  case class `llvm.module.flags`(override val flags: Seq[Incr[IntAttr]])
      extends FlagBag("llvm.module.flags", flags)

  case class `llvm.dbg.cu`(cus: Seq[Incr[DICompileUnit]])
      extends Named("llvm.dbg.cu")

  case class DICompileUnit(
      file: Incr[DIFile],
      producer: String,
      isOptimised: Boolean
  ) extends Scope

  case class DILocation(line: Int, column: Int, scope: Incr[Scope])
      extends LLVMDebugInformation

  case class DIFile(name: String, directory: String) extends Scope

  case class DISubprogram(
      name: String,
      linkageName: String,
      scope: Incr[Scope],
      file: Incr[DIFile],
      line: Int,
      tpe: Incr[DISubroutineType],
      unit: Incr[DICompileUnit],
      distinct: Boolean
  ) extends Scope

  case class DILocalVariable(
      name: String,
      scope: Incr[Scope],
      file: Incr[LLVMDebugInformation],
      line: Int,
      tpe: Incr[Type]
  ) extends LLVMDebugInformation

  case class DIBasicType(name: String, size: Int) extends Type

  sealed abstract class DWTag(val tg: String)
      extends Product
      with Serializable {}

  object DWTag {
    case object Pointer extends DWTag("DW_TAG_pointer_type")
  }

  case class DIDerivedType(tag: DWTag, baseType: Incr[Type], size: Int)
      extends Type
  case class DISubroutineType(types: Incr[DITypes]) extends Type
  case class DITypes(retTpe: Option[Incr[Type]], arguments: Seq[Incr[Type]])
      extends LLVMDebugInformation
}

object DebugInformationSection {
  class Token {
    def render(implicit dwf: DebugInformationSection.Builder) =
      s"!${dwf.gidx.id(this)}"
  }
  class Builder(val gidx: GenIdx) {
    private val b = collection.concurrent.TrieMap
      .empty[Global, Incr[LLVMDebugInformation]]

    private val anon =
      collection.mutable.ListBuffer.empty[Incr[LLVMDebugInformation]]

    private val globals = collection.concurrent.TrieMap
      .empty[LLVMDebugInformation, Incr[LLVMDebugInformation]]

    private val keyCache =
      collection.concurrent.TrieMap.empty[Any, Incr[LLVMDebugInformation]]

    def register[T <: LLVMDebugInformation](in: Global, dwarf: T): Incr[T] = {
      val newTok = gidx.gen()
      if (b.contains(in))
        throw new Exception(s"[dwarf] attempt to overwrite `$in` key")

      val computed = Incr(newTok, dwarf)
      b.update(in, computed)

      computed
    }

    def put(in: Global, dwarf: Incr[LLVMDebugInformation]) = {
      b.update(in, dwarf)
    }

    def fileFromPosition(
        pos: Position
    ): Incr[LLVMDebugInformation.DIFile] = {

      // It's done in this rather verbose way because on Java 8 any approach using `zip` fails with
      // value getOrElse is not a member of Iterable[scala.scalanative.codegen.llvm.Incr[Nothing]]
      // Error:  possible cause: maybe a semicolon is missing before `value getOrElse'?

      for {
        filename <- pos.filename
        dir <- pos.dir
      } yield cachedBy[java.net.URI, DIFile](
        pos.source,
        LLVMDebugInformation.DIFile(filename, dir)
      )
    }
      .getOrElse(
        cachedBy[java.net.URI, DIFile](
          pos.source,
          LLVMDebugInformation.DIFile("unknown", "unknown")
        )
      )

    def fileLocation(pos: Position): Incr[LLVMDebugInformation.DILocation] =
      cachedBy(
        pos,
        LLVMDebugInformation.DILocation(
          pos.line,
          pos.column,
          fileFromPosition(pos)
        )
      )
    def scopeLocation(
        pos: Position,
        scope: Incr[LLVMDebugInformation.Scope]
    ): Incr[LLVMDebugInformation.DILocation] =
      cachedBy(
        pos -> scope.tok,
        LLVMDebugInformation.DILocation(pos.line, pos.column, scope)
      )

    def cached[T <: LLVMDebugInformation](in: LLVMDebugInformation): Incr[T] =
      globals.getOrElseUpdate(in, Incr(gidx.gen(), in)).asInstanceOf[Incr[T]]

    def cachedBy[K, T <: LLVMDebugInformation](
        k: K,
        in: => LLVMDebugInformation
    )(implicit
        ct: ClassTag[T]
    ): Incr[T] =
      keyCache
        .getOrElseUpdate(k -> ct, Incr(gidx.gen(), in))
        .asInstanceOf[Incr[T]]

    def anon[T <: LLVMDebugInformation](dwarf: T): Incr[T] = {
      val newTok = gidx.gen()
      val res = Incr(newTok, dwarf)

      anon += res

      res
    }

    def get[T <: LLVMDebugInformation](in: Global) =
      b(in).asInstanceOf[Incr[T]]

    import scalanative.util.ShowBuilder

    def render(implicit show: ShowBuilder) = {
      val tips = b.values.toSeq ++
        anon.result() ++ globals.values.toSeq ++ keyCache.values.toSeq

      val compileUnits = anon(`llvm.dbg.cu`(tips.collect {
        case cu @ Incr(tok, dic: DICompileUnit) =>
          Incr(tok, dic)
      }))

      show.newline()
      (compileUnits +: tips)
        .map(i => i.id(this) -> i)
        .sortBy(_._1)
        .foreach {
          case (_, Incr(_, dfn: Named)) =>
            show.line(s"!${dfn.name} = ${dfn.render(this)}")
          case (id, Incr(_, dfn)) =>
            show.line(s"!$id = ${dfn.render(this)}")
        }
    }
  }

  def builder() = new Builder(GenIdx.create)
}
