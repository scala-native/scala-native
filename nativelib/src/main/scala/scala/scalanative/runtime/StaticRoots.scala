package scala.scalanative.runtime

import scala.scalanative.unsafe._

@extern
private[runtime] object StaticRoots {
  // Wait for registration before publishing class monitor/ClassValue references.
  // Other threads may finish a collection while this thread waits for the once.
  @blocking
  @name("scalanative_register_class_roots")
  def registerClasses(): Unit = extern
}
