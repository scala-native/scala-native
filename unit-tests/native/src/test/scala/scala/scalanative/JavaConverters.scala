package scala.scalanative

object JavaConverters {

  implicit class ImplicitScalaIterator[A](private val it: Iterator[A])
      extends AnyVal {

    def asJava: java.util.Iterator[A] = new java.util.Iterator[A] {
      override def hasNext(): Boolean = it.hasNext
      override def next(): A = it.next()
    }

  }

}
