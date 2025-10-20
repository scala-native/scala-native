/*
 * Written by Doug Lea and Martin Buchholz with assistance from
 * members of JCP JSR-166 Expert Group and released to the public
 * domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package org.scalanative.testsuite.javalib.util.concurrent

import org.junit.Assert.*
import org.junit.Test

import java.util.{Collection, ArrayList, Set, Comparator, List}
import java.util.concurrent.*

class ExecutorCompletionService9Test extends JSR166Test {
  @throws[InterruptedException]
  @throws[ExecutionException]
  def solveAll(e: Executor, solvers: Collection[Callable[Integer]]): Unit = {
    val cs = new ExecutorCompletionService[Integer](e)
    solvers.forEach(cs.submit(_))
    for (i <- solvers.size until 0 by -1) {
      val r = cs.take.get
      if (r != null) use(r)
    }
  }
  @throws[InterruptedException]
  def solveAny(e: Executor, solvers: Collection[Callable[Integer]]): Unit = {
    val cs =
      new ExecutorCompletionService[Integer](e)
    val n = solvers.size
    val futures = new ArrayList[Future[Integer]](n)
    var result: Integer = null
    try {
      solvers.forEach((solver: Callable[Integer]) =>
        futures.add(cs.submit(solver))
      )
      import scala.util.control.Breaks.*
      breakable {
        for (i <- n until 0 by -1) {
          try {
            val r = cs.take.get
            if (r != null) {
              result = r
              break()
            }
          } catch { case ignore: ExecutionException => () }
        }
      }
    } finally futures.forEach((future: Future[Integer]) => future.cancel(true))
    if (result != null) use(result)
  }
  var results: ArrayList[Integer] = null
  def use(x: Integer): Unit = {
    if (results == null) results = new ArrayList[Integer]()
    results.add(x)
  }

  /** The first "solvers" sample code in the class javadoc works.
   */
  @throws[InterruptedException]
  @throws[ExecutionException]
  @Test def testSolveAll(): Unit = {
    results = null
    val solvers = new java.util.HashSet[Callable[Integer]]
    solvers.add(() => null)
    solvers.add(() => 1: Integer)
    solvers.add(() => 2: Integer)
    solvers.add(() => 3: Integer)
    solvers.add(() => null)
    solveAll(cachedThreadPool, solvers)
    // results.sort(Comparator.naturalOrder)
    // assertEquals(List.of(1, 2, 3), results)
    val resultsList = collection.mutable.ListBuffer.empty[Integer]
    results.iterator().forEachRemaining(resultsList.append(_))
    assertEquals(
      resultsList.toList.sorted,
      scala.List(1, 2, 3)
    )
  }

  /** The second "solvers" sample code in the class javadoc works.
   */
  @throws[InterruptedException]
  @Test def testSolveAny(): Unit = {
    results = null
    val solvers = new java.util.HashSet[Callable[Integer]]
    solvers.add(() => {
      def foo() = throw new ArithmeticException
      foo()
    })
    solvers.add(() => null)
    solvers.add(() => 1: Integer)
    solvers.add(() => 2: Integer)

    solveAny(cachedThreadPool, solvers)
    assertEquals(1, results.size)
    val elt = results.get(0)
    assertTrue(elt == 1 || elt == 2)
  }
}
