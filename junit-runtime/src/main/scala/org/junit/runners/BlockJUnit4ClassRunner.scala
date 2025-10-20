package org.junit.runners

import org.junit.runners.model.FrameworkMethod

/* In the Scala Native JUnit framework (com.novocode.junit.JUnitFramework) there
 * is a custom runner that executes the tests. Therefore the implementation of
 * the original runner is not used. But we still want to be able to compile a
 * class that explicitly specifies the default runner using @RunWith(JUnit4).
 * For this we need only a dummy implementation because we just need to
 * identify the runner using classOf[...].
 */
class BlockJUnit4ClassRunner(testClass: Class[?])
    extends ParentRunner[FrameworkMethod](testClass)
