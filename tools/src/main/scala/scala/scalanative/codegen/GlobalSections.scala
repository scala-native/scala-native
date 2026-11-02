package scala.scalanative.codegen

import scala.collection.concurrent.TrieMap

import scala.scalanative.{build, nir}

/** Storage assigned explicitly when codegen creates a static object. */
private[codegen] object GlobalSections {
  final case class Section(
      name: String,
      machOName: String,
      coffName: String
  ) {
    require(
      machOName.length <= 16,
      s"Mach-O section name must not exceed 16 characters: '$machOName'"
    )

    def llvmName(config: build.Config): String =
      if (config.targetsMac) s"__DATA,$machOName"
      else if (config.targetsWindows) s"$coffName$$M"
      else name
  }

  val Strings = Section(
    name = "sn_strings",
    machOName = "__sn_strings",
    coffName = ".snstr"
  )
  val ClassInfo = Section(
    name = "sn_classinfo",
    machOName = "__sn_classinfo",
    coffName = ".sncls"
  )
  val GlobalObjects = Section(
    name = "sn_global_objects",
    machOName = "__sn_globals",
    coffName = ".snglob"
  )
}

private[codegen] final class GlobalSections {
  import GlobalSections.Section

  private val globals = TrieMap.empty[nir.Global, Section]
  private val constants = TrieMap.empty[nir.Val, Section]

  def define(section: Section, definition: nir.Defn.Var): nir.Defn.Var = {
    globals.put(definition.name, section)
    definition
  }

  def constant(section: Section, value: nir.Val): nir.Val.Const = {
    constants.put(value, section)
    nir.Val.Const(value)
  }

  def forGlobal(name: nir.Global): Option[Section] = globals.get(name)
  def forConstant(value: nir.Val): Option[Section] = constants.get(value)
}
