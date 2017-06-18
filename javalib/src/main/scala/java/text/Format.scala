package java.text

abstract class Format {
  // TODO: add missing methods

  final def format(obj: Object): String =
    format(obj, new StringBuffer(), new FieldPosition(0)).toString()

  def format(obj: Object,
             toAppendTo: StringBuffer,
             pos: FieldPosition): StringBuffer
}

object Format {
  class Field protected (name: String)
}
