import sbt._
import Keys._

import scala.scalanative.sbtplugin.ScalaNativePluginInternal._

object Check {
  lazy val check =
    TaskKey[Unit]("check", "make sure we list external dependencies correctly")

  val setup = Seq(
    check := {
      val deps = (nativeExternalDependencies in Compile).value.toSet

      val missing = (nativeMissingDependencies in Compile).value.toSet

      // most propably not implemented
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
        "@java.awt.Component::addMouseListener_trait.java.awt.event.MouseListener_unit",
        "@java.awt.Component::getHeight_i32",
        "@java.awt.Component::getWidth_i32",
        "@java.awt.Component::paint_class.java.awt.Graphics_unit",
        "@java.awt.Component::repaint_unit",
        "@java.awt.Container",
        "@java.awt.Container::paint_class.java.awt.Graphics_unit",
        "@java.awt.Graphics",
        "@java.awt.Graphics::drawRect_i32_i32_i32_i32_unit",
        "@java.awt.Graphics::drawString_class.java.lang.String_i32_i32_unit",
        "@java.awt.event.MouseEvent",
        "@java.awt.event.MouseListener",
        "@java.awt.event.MouseListener::mouseClicked_class.java.awt.event.MouseEvent_unit",
        "@java.awt.event.MouseListener::mouseEntered_class.java.awt.event.MouseEvent_unit",
        "@java.awt.event.MouseListener::mouseExited_class.java.awt.event.MouseEvent_unit",
        "@java.awt.event.MouseListener::mousePressed_class.java.awt.event.MouseEvent_unit",
        "@java.awt.event.MouseListener::mouseReleased_class.java.awt.event.MouseEvent_unit"
      )

      assert((awt -- missing).isEmpty)

      assert(((applets ++ awt) -- deps).isEmpty)

      // most propably implemented
      val rest = Set(
        "@java.io.PrintStream",
        "@java.io.PrintStream::println_class.java.lang.String_unit",
        "@java.lang.Object",
        "@java.lang.String",
        "@java.lang.StringBuffer",
        "@java.lang.StringBuffer::append_class.java.lang.String_class.java.lang.StringBuffer",
        "@java.lang.StringBuffer::init",
        "@java.lang.StringBuffer::toString_class.java.lang.String",
        "@java.lang.System$",
        "@java.lang.System$::out_class.java.io.PrintStream",
        "@scala.LowPriorityImplicits",
        "@scala.LowPriorityImplicits::wrapIntArray_class.ssnr.IntArray_class.scala.collection.mutable.WrappedArray",
        "@scala.Predef$",
        "@scala.Predef$::Set_module.scala.collection.immutable.Set$",
        "@scala.collection.GenTraversable",
        "@scala.collection.Seq",
        "@scala.collection.generic.GenericCompanion",
        "@scala.collection.generic.GenericCompanion::apply_trait.scala.collection.Seq_trait.scala.collection.GenTraversable",
        "@scala.collection.immutable.Set",
        "@scala.collection.immutable.Set$",
        "@scala.collection.mutable.WrappedArray",
        "@scala.scalanative.runtime.IntArray",
        "@scala.scalanative.runtime.IntArray$",
        "@scala.scalanative.runtime.IntArray$::alloc_i32_class.ssnr.IntArray",
        "@scala.scalanative.runtime.IntArray::update_i32_i32_unit"
      )

      assert((rest -- deps).isEmpty)
    }
  )
}
