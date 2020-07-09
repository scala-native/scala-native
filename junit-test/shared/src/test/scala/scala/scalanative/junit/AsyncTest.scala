package scala.scalanative.junit

// Ported from Scala.js

import org.junit.Test

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalanative.junit.async._
import scala.scalanative.junit.utils._

class AsyncTest {
  @Test
  def success(): AsyncResult = await {
    Future(1 + 1).filter(_ == 2)
  }

  @Test(expected = classOf[IllegalArgumentException])
  def expectedException(): AsyncResult = await {
    // Do not throw synchronously.
    Future.failed(new IllegalArgumentException)
  }

  @Test
  def asyncFailure(): AsyncResult = await {
    // Do not throw synchronously.
    Future.failed(new IllegalArgumentException)
  }
}

class AsyncTestAssertions extends JUnitTest
