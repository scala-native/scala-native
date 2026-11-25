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

  // Size of ObjectArray's RTTI, copied by ArrayClasses
  @name("scalanative_object_array_class_size")
  def objectArrayClassSize(): RawSize = extern

  // Classes of arrays of references defined by the linker
  @name("scalanative_reference_array_classes")
  def referenceArrayClasses(): RawPtr = extern
  @name("scalanative_reference_array_class_count")
  def referenceArrayClassCount(): RawSize = extern

  // Pointers to the RTTI of every class and trait
  @name("scalanative_static_classlist_section")
  def classListSection(): RawPtr = extern
}
