/*
 * Scala (https://www.scala-lang.org)
 *
 * Copyright EPFL and Lightbend, Inc.
 *
 * Licensed under Apache License 2.0
 * (http://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package scala.collection.compat

package object immutable {
  type ArraySeq[+T] = scala.collection.immutable.ArraySeq[T]
  val ArraySeq = scala.collection.immutable.ArraySeq

  type LazyList[+T] = scala.collection.immutable.LazyList[T]
  val LazyList = scala.collection.immutable.LazyList
}