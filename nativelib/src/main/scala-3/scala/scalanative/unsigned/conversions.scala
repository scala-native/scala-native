package scala.scalanative.unsigned

extension (inline v: Int){
  inline def u: NewUInt = NewUInt.unsigned(v)
  inline def U: NewUInt = NewUInt.unsigned(v)
  inline def ul: NewUInt = NewUInt.unsigned(v)
  inline def UL: NewUInt = NewUInt.unsigned(v)
}