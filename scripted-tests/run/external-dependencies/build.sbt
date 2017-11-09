enablePlugins(ScalaNativePlugin)

scalaVersion := "2.11.12"

lazy val check =
  TaskKey[Unit]("check", "make sure we list external dependencies correctly")

check := {
  val external = (nativeExternalDependencies in Compile).value.toSet
  val missing  = (nativeMissingDependencies in Compile).value.toSet

  // not supported
  val applets = Set(
    "@java.applet.Applet",
    "@java.applet.Applet::destroy_unit",
    "@java.applet.Applet::init",
    "@java.applet.Applet::init_unit",
    "@java.applet.Applet::start_unit",
    "@java.applet.Applet::stop_unit"
  )
  val awt = Set(
    "@java.awt.Component",
    "@java.awt.Component::addMouseListener_java.awt.event.MouseListener_unit",
    "@java.awt.Component::getHeight_i32",
    "@java.awt.Component::getWidth_i32",
    "@java.awt.Component::paint_java.awt.Graphics_unit",
    "@java.awt.Component::repaint_unit",
    "@java.awt.Container",
    "@java.awt.Container::paint_java.awt.Graphics_unit",
    "@java.awt.Graphics",
    "@java.awt.Graphics::drawRect_i32_i32_i32_i32_unit",
    "@java.awt.Graphics::drawString_java.lang.String_i32_i32_unit",
    "@java.awt.event.MouseEvent",
    "@java.awt.event.MouseListener",
    "@java.awt.event.MouseListener::mouseClicked_java.awt.event.MouseEvent_unit",
    "@java.awt.event.MouseListener::mouseEntered_java.awt.event.MouseEvent_unit",
    "@java.awt.event.MouseListener::mouseExited_java.awt.event.MouseEvent_unit",
    "@java.awt.event.MouseListener::mousePressed_java.awt.event.MouseEvent_unit",
    "@java.awt.event.MouseListener::mouseReleased_java.awt.event.MouseEvent_unit"
  )

  assert((awt -- missing).isEmpty)
  assert(((applets ++ awt) -- external).isEmpty)

  // implemented
  val rest = Set(
    "@java.io.PrintStream",
    "@java.io.PrintStream::println_java.lang.String_unit",
    "@java.lang.Object",
    "@java.lang.String",
    "@java.lang.StringBuffer",
    "@java.lang.StringBuffer::append_java.lang.String_java.lang.StringBuffer",
    "@java.lang.StringBuffer::init",
    "@java.lang.StringBuffer::toString_java.lang.String",
    "@java.lang.System$",
    "@java.lang.System$::out_java.io.PrintStream",
    "@scala.LowPriorityImplicits",
    "@scala.LowPriorityImplicits::wrapIntArray_scala.scalanative.runtime.IntArray_scala.collection.mutable.WrappedArray",
    "@scala.Predef$",
    "@scala.Predef$::Set_scala.collection.immutable.Set$",
    "@scala.collection.GenTraversable",
    "@scala.collection.Seq",
    "@scala.collection.generic.GenericCompanion",
    "@scala.collection.generic.GenericCompanion::apply_scala.collection.Seq_scala.collection.GenTraversable",
    "@scala.collection.immutable.Set",
    "@scala.collection.immutable.Set$",
    "@scala.collection.mutable.WrappedArray",
    "@scala.scalanative.runtime.IntArray",
    "@scala.scalanative.runtime.IntArray$",
    "@scala.scalanative.runtime.IntArray$::alloc_i32_scala.scalanative.runtime.IntArray",
    "@scala.scalanative.runtime.IntArray::update_i32_i32_unit"
  )

  assert((rest -- external).isEmpty)
}
