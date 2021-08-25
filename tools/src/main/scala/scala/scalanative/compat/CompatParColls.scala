package scala.scalanative.compat

private[scalanative] object CompatParColls {
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
