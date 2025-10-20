package scala.scalanative
package junit

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.scalanative.meta.LinktimeInfo.isMultithreadingEnabled

package object async {
  type AsyncResult = Unit
  def await(future: Future[?]): AsyncResult = {
    if (isMultithreadingEnabled)
      Await.result(future, Duration.Inf)
    else {
      concurrent.NativeExecutionContext.queueInternal.helpComplete()
      future.value.get.get
    }
  }
}
