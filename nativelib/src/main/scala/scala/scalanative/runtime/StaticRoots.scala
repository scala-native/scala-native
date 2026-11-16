package scala.scalanative.runtime

import scala.scalanative.unsafe._

@extern
private[runtime] object StaticRoots {
  // Register the containing static section before publishing a managed reference.
  @blocking
  @name("scalanative_register_object_roots")
  def registerObject(obj: RawPtr): Unit = extern

  // Pointers to the RTTI of every class and trait
  @name("scalanative_static_classlist_section")
  def classListSection(): RawPtr = extern
}
