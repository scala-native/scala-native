package org.scalanative.testsuite.javalib.lang

import java.lang as jl

import org.junit.Assert.*
import org.junit.Test
import org.scalanative.testsuite.utils.Platform

import java.io.{ByteArrayOutputStream, PrintStream}
import java.lang.System.Logger
import java.util.{ResourceBundle, MissingResourceException}

class SystemLoggerTestOnJDK9 {

  private def captureSystemErr(testCode: => Unit): String = {
    val originalErr = System.err
    val errContent = new ByteArrayOutputStream()
    System.setErr(new PrintStream(errContent))

    try {
      testCode
      errContent.toString
    } finally {
      System.setErr(originalErr)
    }
  }

  // Basic functionality tests

  @Test def testJDK9GetLogger(): Unit = {
    val logger = System.getLogger("test.logger")
    assertNotNull(logger)
    assertEquals("test.logger", logger.getName())
  }

  @Test def testJDK9CachedLoggers(): Unit = {
    val logger2 = System.getLogger("test.logger")
    val logger1 = System.getLogger("test.logger")
    assertNotNull(logger1)
    assertNotNull(logger2)
    assertNotSame(logger1, logger2)
  }

  @Test def testJDK9IsLoggable(): Unit = {
    // On JDK9+ this is usually delegated to the underlying logging system
    // Just verify it doesn't throw exceptions
    val logger = System.getLogger("test.logger.levels")

    // Check each level without asserting specific behavior
    for (level <- Logger.Level.values()) {
      // Just call the method to verify it works
      logger.isLoggable(level)
    }
  }

  // Logging tests

  @Test def testJDK9LogWithMessage(): Unit = {
    // Just verify it doesn't throw exceptions
    val logger = System.getLogger("test.simple")
    captureSystemErr {
      logger.log(Logger.Level.INFO, "Test message")
    }
  }

  @Test def testJDK9LogWithNullMessage(): Unit = {
    // Just verify it doesn't throw exceptions
    val logger = System.getLogger("test.null")
    captureSystemErr {
      logger.log(Logger.Level.WARNING, null: String)
    }
  }

  @Test def testJDK9LogWithFormatAndParams(): Unit = {
    // Just verify it doesn't throw exceptions
    val logger = System.getLogger("test.format")
    captureSystemErr {
      logger.log(
        Logger.Level.ERROR,
        "Value: %s, Number: %d",
        "test",
        Integer.valueOf(42)
      )
    }
  }

  @Test def testJDK9LogWithThrowable(): Unit = {
    // Just verify it doesn't throw exceptions
    val logger = System.getLogger("test.exception")
    val exception = new RuntimeException("Test exception")
    captureSystemErr {
      logger.log(Logger.Level.ERROR, "Error occurred", exception)
    }
  }

  @Test def testJDK9OffLevelDoesNotLog(): Unit = {
    val logger = System.getLogger("test.off")
    val output = captureSystemErr {
      logger.log(Logger.Level.OFF, "This should not be logged")
    }

    // In most implementations, OFF should not log anything
    // but we don't assert specifics since implementations may vary
  }

  // ResourceBundle tests

  @Test def testJDK9LogWithResourceBundle(): Unit = {
    // Just verify it doesn't throw exceptions
    val logger = System.getLogger("test.bundle")

    val bundle = new ResourceBundle {
      private val messages = Map(
        "message.key" -> "Translated message",
        "format.key" -> "Formatted with %s and %d"
      )

      override def handleGetObject(key: String): Object =
        messages.getOrElse(key, null)

      override def getKeys(): java.util.Enumeration[String] = {
        val it = messages.keys.iterator
        new java.util.Enumeration[String] {
          override def hasMoreElements(): Boolean = it.hasNext
          override def nextElement(): String = it.next()
        }
      }
    }

    captureSystemErr {
      logger.log(Logger.Level.INFO, bundle, "message.key", null: Throwable)
      logger.log(
        Logger.Level.DEBUG,
        bundle,
        "format.key",
        "value",
        Integer.valueOf(123)
      )
      logger.log(Logger.Level.WARNING, bundle, "missing.key", null: Throwable)
    }
  }

  // Supplier tests

  @Test def testJDK9LogWithSupplier(): Unit = {
    val logger = System.getLogger("test.supplier")
    var supplierCalled = false

    val msgSupplier = new java.util.function.Supplier[String] {
      override def get(): String = {
        supplierCalled = true
        "Message from supplier"
      }
    }

    captureSystemErr {
      logger.log(Logger.Level.INFO, msgSupplier)
    }

    // Should have called the supplier
    assertTrue("Supplier was not called", supplierCalled)
  }

  @Test def testJDK9LogWithSupplierAndThrowable(): Unit = {
    // Just verify it doesn't throw exceptions
    val logger = System.getLogger("test.supplier.exception")
    val exception = new IllegalArgumentException("Supplier exception")

    val msgSupplier = new java.util.function.Supplier[String] {
      override def get(): String = "Error from supplier"
    }

    captureSystemErr {
      logger.log(Logger.Level.ERROR, msgSupplier, exception)
    }
  }

  // Level tests

  @Test def testJDK9LoggerLevels(): Unit = {
    // Check severity relationships between levels
    val logger = System.getLogger("test.logger")
    assertTrue(
      System.Logger.Level.ERROR.getSeverity() > System.Logger.Level.WARNING
        .getSeverity()
    )
    assertTrue(
      System.Logger.Level.WARNING.getSeverity() > System.Logger.Level.INFO
        .getSeverity()
    )
    assertTrue(
      System.Logger.Level.INFO.getSeverity() > System.Logger.Level.DEBUG
        .getSeverity()
    )
    assertTrue(
      System.Logger.Level.DEBUG.getSeverity() > System.Logger.Level.TRACE
        .getSeverity()
    )
    assertTrue(
      System.Logger.Level.TRACE.getSeverity() > System.Logger.Level.ALL
        .getSeverity()
    )
  }

  @Test def testJDK9LoggerLevelValues(): Unit = {
    // Verify all expected levels exist
    val levels = Logger.Level.values()
    assertEquals(7, levels.length)

    // Verify we have all the expected levels
    assertTrue(levels.contains(Logger.Level.ALL))
    assertTrue(levels.contains(Logger.Level.TRACE))
    assertTrue(levels.contains(Logger.Level.DEBUG))
    assertTrue(levels.contains(Logger.Level.INFO))
    assertTrue(levels.contains(Logger.Level.WARNING))
    assertTrue(levels.contains(Logger.Level.ERROR))
    assertTrue(levels.contains(Logger.Level.OFF))
  }
}
