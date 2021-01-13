package scala.scalanative.testinterface

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

package object adapter {
  private[adapter] implicit class AwaitFuture[T](val t: Future[T])
      extends AnyVal {
    def await(): T = Await.result(t, Duration.Inf)
  }
}
