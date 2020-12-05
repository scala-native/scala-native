// Ported from Scala.js commit: 222e14c dated: 2019-09-11

package org.scalanative.testsuite.javalib.util

import java.{util => ju}

import scala.reflect.ClassTag

trait CollectionsOnSynchronizedSetTest extends CollectionsOnSetsTest {

  def originalFactory: SetFactory

  def factory: SetFactory = {
    new SetFactory {
      override def implementationName: String =
        s"synchronizedSet(${originalFactory.implementationName})"

      override def empty[E: ClassTag]: ju.Set[E] =
        ju.Collections.synchronizedSet(originalFactory.empty[E])

      override def allowsNullElement: Boolean =
        originalFactory.allowsNullElement
    }
  }
}

trait CollectionsOnSynchronizedSortedSetTest extends CollectionsOnSortedSetsTest {

  def originalFactory: SortedSetFactory

  def factory: SortedSetFactory = {
    new SortedSetFactory {
      override def implementationName: String =
        s"synchronizedSortedSet(${originalFactory.implementationName})"

      override def empty[E: ClassTag]: ju.SortedSet[E] =
        ju.Collections.synchronizedSortedSet(originalFactory.empty[E])

      override def allowsNullElement: Boolean =
        originalFactory.allowsNullElement
    }
  }
}

class CollectionsOnSynchronizedSetHashSetFactoryTest
    extends CollectionsOnSynchronizedSetTest {
  def originalFactory: SetFactory = new HashSetFactory
}

class CollectionsOnSynchronizedSetCollectionLinkedHashSetFactoryTest
    extends CollectionsOnSynchronizedSetTest {
  def originalFactory: SetFactory = new LinkedHashSetFactory
}

class CollectionsOnSynchronizedSetCollectionConcurrentSkipListSetFactoryTest
    extends CollectionsOnSynchronizedSetTest {
  def originalFactory: SetFactory = new concurrent.ConcurrentSkipListSetFactory
}
