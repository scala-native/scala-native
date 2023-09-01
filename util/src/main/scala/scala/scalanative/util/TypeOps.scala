package scala.scalanative.util

private[scalanative] object TypeOps {
  implicit class TypeNarrowing[T](val value: T) extends AnyVal {
    def narrow[S <: T](implicit classTag: scala.reflect.ClassTag[S]): S =
      if (classTag.runtimeClass.isInstance(value)) value.asInstanceOf[S]
      else
        throw new IllegalStateException(
          s"Unexpected instance of ${value.getClass().getSimpleName()} where type of ${classTag.runtimeClass.getSimpleName()} was expected"
        )
  }
}
