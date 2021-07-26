package scala

import org.junit.Test
import org.junit.Assert._
import scala.reflect.macros.blackbox.Context
import scala.reflect.runtime.universe._
import scala.language.experimental.macros

/* Dummy test used determinate if a trait of macro can compile to nir.
 * If it does compile, it passes
 */
class Issue2305 {

  trait Foo {
    def fooImpl(c: Context)(input: c.Tree): c.Tree
  }

  @Test def macroTraitCanCompile(): Unit = {
    assertTrue(true)
  }

}
