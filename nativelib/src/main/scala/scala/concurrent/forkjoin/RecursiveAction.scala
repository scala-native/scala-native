package scala.concurrent.forkjoin

abstract class RecursiveAction extends ForkJoinTask[Void] {

  protected def compute: Unit

  override final def getRawResult: Void = null

  override protected final def setRawResult(mustBeNull: Void): Unit = {}

  override protected final def exec(): Boolean = {
    compute
    true
  }

}

object RecursiveAction {
  private final val serialVersionUID: Long = 5232453952276485070L
}
