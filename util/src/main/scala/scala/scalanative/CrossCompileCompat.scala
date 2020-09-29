package scala.scalanative

private[scalanative] object CrossCompileCompat {
  val Converters = {
    import Compat._
    {
      import scala.collection.parallel._
      CollectionConverters
    }
  }

  object Compat {
    object CollectionConverters
  }
}
