package scala.scalanative.junit

import scala.concurrent.Future
import scala.util.Try

package object async {
  type AsyncResult = Try[_]
  def await(future: Future[_]): AsyncResult = {
    scala.scalanative.runtime.loop()
    future.value.get
  }
}
