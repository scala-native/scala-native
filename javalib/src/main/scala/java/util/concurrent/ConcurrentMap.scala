/*
 * Scala.js (https://www.scala-js.org/)
 *
 * Copyright EPFL.
 *
 * Licensed under Apache License 2.0
 * (https://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

/// SN porting notes:
///   Ported, with thanks & gratitude, from Scala-js original dated 2018-10-12 
///    commit: https://github.com/scala-js/scala-js/commit/
///              9dc4d5b36ff2b2a3dfe2e91d5c6b1ef6d10d3e51

package java.util.concurrent

import java.util.Map

trait ConcurrentMap[K, V] extends Map[K, V] {
  def putIfAbsent(key: K, value: V): V
  def remove(key: Any, value: Any): Boolean
  def replace(key: K, oldValue: V, newValue: V): Boolean
  def replace(key: K, value: V): V
}
