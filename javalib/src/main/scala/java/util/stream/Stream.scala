package java.util.stream

trait Stream[T] extends BaseStream[T, Stream[T]]

object Stream {
  trait Builder[T] {
    def accept(t: T): Unit
    def add(t: T): Builder[T] = {
      accept(t)
      this
    }
    def build(): Stream[T]
  }

  def builder[T](): Builder[T] = new ScalaNativeStubStream.Builder[T]
  def empty[T](): Stream[T]    = new ScalaNativeStubStream(Seq.empty[T])
  def of[T](values: Array[T])  = new ScalaNativeStubStream(values)
}
