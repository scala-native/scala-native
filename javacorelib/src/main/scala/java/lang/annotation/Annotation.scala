package java.lang.annotation

trait Annotation {
  def annotationType[T <: Annotation]: T
}
