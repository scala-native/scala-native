package scala.concurrent.forkjoin

abstract class RecursiveTask[V] extends ForkJoinTask[V] {

  var result: V = _

  protected def compute: V

  override final def getRawResult: V = result

  override protected final def setRawResult(value: V): Unit = result = value

  protected final def exec: Boolean = {
    result = compute
    true
  }

}

object RecursiveTask {

  private final val serialVersionUID: Long = 5232453952276485270L

}
