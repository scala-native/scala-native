package scala.scalanative.runtime

import scala.scalanative.unsafe._

@extern
private[runtime] object StaticRoots {
  // Register the containing static section before publishing a managed reference.
  @blocking
  @name("scalanative_register_object_roots")
  def registerObject(obj: RawPtr): Unit = extern

  // Zeroed memory for a class created at runtime, never freed; its name and
  // lock word are GC roots. Null when out of memory.
  @blocking
  @name("scalanative_alloc_runtime_class")
  def allocRuntimeClass(size: RawSize): RawPtr = extern

  // Size of the RTTI of ObjectArray, copied by classes of arrays created at
  // runtime, see ArrayClasses
  @name("scalanative_object_array_class_size")
  def objectArrayClassSize(): RawSize = extern

  // Pointers to the RTTI of every class and trait
  @name("scalanative_static_classlist_section")
  def classListSection(): RawPtr = extern
}
