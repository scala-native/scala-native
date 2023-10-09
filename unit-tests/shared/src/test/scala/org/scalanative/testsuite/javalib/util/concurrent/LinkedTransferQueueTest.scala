/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */
package org.scalanative.testsuite.javalib.util.concurrent

import org.junit.Assert._
import org.junit.{Test, Ignore}

import JSR166Test._

import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util
import java.util._
import java.util.concurrent._
import java.util.concurrent.LinkedTransferQueue
class LinkedTransferQueueTest extends JSR166Test {}

object LinkedTransferQueueTest {
  class Generic extends BlockingQueueTest {
    protected def emptyCollection() = new LinkedTransferQueue()
  }
}
