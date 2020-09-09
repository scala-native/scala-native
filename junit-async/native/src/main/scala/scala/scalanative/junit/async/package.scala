package scala.scalanative.junit

import scala.concurrent.Future

package object async {
  type AsyncResult = Unit
  def await(future: Future[_]): AsyncResult = {
    scala.scalanative.runtime.loop()
    future.value.get.get
  }
}
