package scala.scalanative.runtime.dwarf

private[runtime] sealed trait Endianness extends Product with Serializable
private[runtime] object Endianness {
  case object LITTLE extends Endianness
  case object BIG extends Endianness
}
