package scala.scalanative
package runtime

import native._

@extern object Rt {
  def scalanative_init(): Unit = extern
}
