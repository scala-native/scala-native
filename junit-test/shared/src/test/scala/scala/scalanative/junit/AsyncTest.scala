package scala.scalanative.junit

// Ported from Scala.js

import org.junit.Test

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalanative.junit.async.*
import scala.scalanative.junit.utils.*

class AsyncTest {
  @Test
  def success(): Unit = await {
    Future(1 + 1).filter(_ == 2)
  }

  @Test(expected = classOf[IllegalArgumentException])
  def expectedException(): Unit = await {
    // Do not throw synchronously.
    Future.failed(new IllegalArgumentException)
  }

  @Test
  def asyncFailure(): Unit = await {
    // Do not throw synchronously.
    Future.failed(new IllegalArgumentException)
  }
}

class AsyncTestAssertions extends JUnitTest
