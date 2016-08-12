package scala.scalanative.nir

/**
 * Created by Kamil on 23.07.2016.
 */
case class Arg(ty: Type, attrs: ArgAttrs = ArgAttrs.empty)

case class ArgAttrs(byval: Option[Type] = None) {

  def toSeq: Seq[ArgAttr] = byval.map(ArgAttr.Byval).toSeq

}

object ArgAttrs {

  val empty = new ArgAttrs

  def fromSeq(attrs: Seq[ArgAttr]): ArgAttrs =
    attrs.foldLeft(ArgAttrs.empty) { (attrs, attr) =>
      attr match {
        case ArgAttr.Byval(ty) => attrs.copy(byval = Some(ty))
      }
    }

}

sealed abstract class ArgAttr

object ArgAttr {

  case class Byval(ty: Type) extends ArgAttr

}
