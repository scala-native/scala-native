package build

import java.nio.file.{Files, Path}

import scala.scalanative.io.VirtualDirectory
import scala.scalanative.nir
import scala.scalanative.util.Scope

/** Detects extern symbols removed or changed since a previous release.
 *
 *  Calls to externs are linked by their NIR name, fixed when the caller is
 *  compiled: owner and `@name` value, or the Scala name without `@name`. MiMa
 *  can't see that name, it compares JVM signatures only. Changing it breaks
 *  libraries compiled against older releases at link time, see
 *  `unsafe.externRedirect` for the way to rename an extern safely.
 *
 *  Independent of sbt, to allow extracting it from the build.
 */
object NirExternCompat {

  /** Fully qualified extern name mapped to its NIR type. */
  type Externs = Map[String, String]

  sealed abstract class Problem {
    def symbol: String
    def description: String
  }
  final case class Missing(symbol: String, ty: String) extends Problem {
    def description = s"extern $symbol: $ty is missing"
  }
  final case class TypeChanged(symbol: String, from: String, to: String)
      extends Problem {
    def description = s"extern $symbol changed type from $from to $to"
  }

  /** Reads externs from a directory or a jar. Visibility is not a part of NIR,
   *  so internal externs are included too. An alias created using
   *  `unsafe.externRedirect` counts as an extern, it keeps the old name working.
   */
  def collect(path: Path): Externs = Scope { implicit in =>
    val directory =
      if (Files.isDirectory(path)) VirtualDirectory.real(path)
      else VirtualDirectory.jar(path)
    directory.files.iterator
      .filter(_.toString.endsWith(".nir"))
      .flatMap(nir.serialization.deserializeBinary(directory, _))
      .flatMap(extern)
      .toMap
  }

  private def extern(defn: nir.Defn): Option[(String, String)] = {
    val ty = defn match {
      case defn: nir.Defn.Declare => Some(defn.ty)
      case defn: nir.Defn.Define  => Some(defn.ty)
      case defn: nir.Defn.Var     => Some(defn.ty)
      case defn: nir.Defn.Const   => Some(defn.ty)
      case _                      => None
    }
    (defn.name, ty) match {
      case (nir.Global.Member(owner, sig), Some(ty)) if sig.isExtern =>
        sig.unmangled match {
          case nir.Sig.Extern(id) => Some(s"${owner.id}.$id" -> ty.show)
          case _                  => None
        }
      case _ => None
    }
  }

  def compare(previous: Externs, current: Externs): Seq[Problem] =
    previous.toSeq.sortBy(_._1).flatMap {
      case (symbol, ty) =>
        current.get(symbol) match {
          case None                  => Some(Missing(symbol, ty))
          case Some(`ty`)            => None
          case Some(changed)         => Some(TypeChanged(symbol, ty, changed))
        }
    }

  /** Matches `symbol` against a filter, in which `*` stands for any text. */
  def isFiltered(symbol: String, filter: String): Boolean =
    filter
      .split("\\*", -1)
      .map(java.util.regex.Pattern.quote)
      .mkString(".*")
      .r
      .pattern
      .matcher(symbol)
      .matches()
}
