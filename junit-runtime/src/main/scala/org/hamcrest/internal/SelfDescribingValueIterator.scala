/*
 * Ported from https://github.com/hamcrest/JavaHamcrest/
 */
package org.hamcrest.internal

import java.util as ju

import org.hamcrest.SelfDescribing

class SelfDescribingValueIterator[T](values: ju.Iterator[T])
    extends ju.Iterator[SelfDescribing] {
  override def hasNext: Boolean =
    values.hasNext

  override def next(): SelfDescribing =
    new SelfDescribingValue(values.next)

  override def remove(): Unit =
    values.remove()
}
