package scala.scalanative.runtime

import scala.scalanative.unsafe._

@extern
private[runtime] object StaticRoots {
  // Register the containing static section before publishing a managed reference.
  @blocking
  @name("scalanative_register_object_roots")
  def registerObject(obj: RawPtr): Unit = extern

  // Never freed; name and lock word are GC roots
  @blocking
  @name("scalanative_alloc_runtime_class")
  def allocRuntimeClass(size: RawSize): RawPtr = extern

  // {RTTI, RTTI size} of ObjectArray and the linker's classes of arrays of
  // references
  @name("scalanative_static_arrayclasses_section")
  def arrayClassesSection(): RawPtr = extern

  // Pointers to the RTTI of every class and trait
  @name("scalanative_static_classlist_section")
  def classListSection(): RawPtr = extern
}
