package scala.scalanative
package sbtplugin

import scala.scalanative.build.*

import sjsonnew.BasicJsonProtocol.{StringJsonKeyFormat, mapFormat}
import sjsonnew.{Builder, JsonFormat, Unbuilder}

object DiscoverEnvJsonFormats {
  private[sbtplugin] implicit object DiscoverEnvCodec extends JsonFormat[build.Discover.Env] {
    private val mapValueFormat: JsonFormat[Map[String, Option[String]]] = mapFormat[String, Option[String]]

    override def write[J](obj: Discover.Env, builder: sjsonnew.Builder[J]): Unit = {
      val mapValue = Map(
        "SCALANATIVE_GC" -> obj.`SCALANATIVE_GC`,
        "SCALANATIVE_INCLUDE_DIRS" -> obj.`SCALANATIVE_INCLUDE_DIRS`,
        "SCALANATIVE_LIB_DIRS" -> obj.`SCALANATIVE_LIB_DIRS`,
        "SCALANATIVE_LTO" -> obj.`SCALANATIVE_LTO`,
        "SCALANATIVE_MODE" -> obj.`SCALANATIVE_MODE`,
        "SCALANATIVE_OPTIMIZE" -> obj.`SCALANATIVE_OPTIMIZE`,
        "LLVM_BIN" -> obj.`LLVM_BIN`,
        "PATH" -> obj.`PATH`
      )

      mapValueFormat.write(mapValue, builder)
    }

    override def read[J](jsOpt: Option[J], unbuilder: Unbuilder[J]): Discover.Env = {
      val mapValue = mapValueFormat.read[J](jsOpt, unbuilder)

      Discover.Env(
        `SCALANATIVE_GC` = mapValue.get("SCALANATIVE_GC").flatten,
        `SCALANATIVE_INCLUDE_DIRS` = mapValue.get("SCALANATIVE_INCLUDE_DIRS").flatten,
        `SCALANATIVE_LIB_DIRS` = mapValue.get("SCALANATIVE_LIB_DIRS").flatten,
        `SCALANATIVE_LTO` = mapValue.get("SCALANATIVE_LTO").flatten,
        `SCALANATIVE_MODE` = mapValue.get("SCALANATIVE_MODE").flatten,
        `SCALANATIVE_OPTIMIZE` = mapValue.get("SCALANATIVE_OPTIMIZE").flatten,
        `LLVM_BIN` = mapValue.get("LLVM_BIN").flatten,
        `PATH` = mapValue.get("PATH").flatten
      )
    }
  }
}
