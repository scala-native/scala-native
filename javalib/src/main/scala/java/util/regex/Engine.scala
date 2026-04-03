// format: off
/*
 * Derived from the Scala.js / scala-wasm ECMA-262 RegExp sources
 * (https://www.scala-js.org/).
 *
 * Copyright EPFL.
 *
 * Licensed under Apache License 2.0
 * (https://www.apache.org/licenses/LICENSE-2.0).
 *
 * Scala Native uses the standalone ECMA-262 RegExp engine only (see
 * [[Ecma262RegExpEngine]], named `WasmEngine` in scala-wasm);
 */

package java.util.regex

import java.util.function.Supplier

/** Underlying engine used by `Pattern`.
 *
 *  The semantics are aligned with ECMAScript `RegExp`. The engine only needs to
 *  support patterns produced by `PatternCompiler`.
 */
private[regex] abstract class Engine {

  type Dictionary[V]

  type RegExp
  type ExecResult >: Null
  type IndicesArray >: Null

  def dictEmpty[V](): Dictionary[V]
  def dictSet[V](dict: Dictionary[V], key: String, value: V): Unit
  def dictContains[V](dict: Dictionary[V], key: String): Boolean
  def dictRawApply[V](dict: Dictionary[V], key: String): V
  def dictGetOrElse[V](dict: Dictionary[V], key: String)(default: Supplier[V]): V
  def dictGetOrElseUpdate[V](dict: Dictionary[V], key: String)(default: Supplier[V]): V

  def featureTest(flags: String): Boolean

  def compile(pattern: String, flags: String): RegExp

  def validateScriptName(scriptName: String): Boolean

  def getLastIndex(regexp: RegExp): Int

  def setLastIndex(regexp: RegExp, newLastIndex: Int): Unit

  def exec(regexp: RegExp, input: String): ExecResult

  def getIndex(result: ExecResult): Int

  def getInput(result: ExecResult): String

  def getIndices(result: ExecResult): IndicesArray

  def setIndices(result: ExecResult, indices: IndicesArray): Unit

  def getGroup(result: ExecResult, group: Int): String

  def getStart(indices: IndicesArray, group: Int): Int

  def getEnd(indices: IndicesArray, group: Int): Int
}

private[regex] object Engine {
  val engine: Engine = Ecma262RegExpEngine
}
