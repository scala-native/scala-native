package scala.scalanative.junit

import scala.util.{Try, Success}
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.scalanative.meta.LinktimeInfo.isMultithreadingEnabled

package object async {
  type AsyncResult = Future[Try[Unit]]
  def await(future: Future[_]): AsyncResult = {
    if (isMultithreadingEnabled)
      Await.ready(future, Duration.Inf)
    else
      while (!future.isCompleted) scala.scalanative.runtime.loop()
    future.map(_ => Success(()))
  }
}
