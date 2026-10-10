package org.scalanative.testsuite.javalib.invoke

import java.lang.invoke.VarHandle

// Inherit the full 31-operation matrix for every variable type in exact mode.

class VarHandleBooleanInstanceExactMatrixTest
    extends VarHandleBooleanInstanceMatrixTest {
  override protected def invocationHandle(handle: VarHandle): VarHandle =
    handle.withInvokeExactBehavior()
}

class VarHandleByteInstanceExactMatrixTest
    extends VarHandleByteInstanceMatrixTest {
  override protected def invocationHandle(handle: VarHandle): VarHandle =
    handle.withInvokeExactBehavior()
}

class VarHandleShortInstanceExactMatrixTest
    extends VarHandleShortInstanceMatrixTest {
  override protected def invocationHandle(handle: VarHandle): VarHandle =
    handle.withInvokeExactBehavior()
}

class VarHandleCharInstanceExactMatrixTest
    extends VarHandleCharInstanceMatrixTest {
  override protected def invocationHandle(handle: VarHandle): VarHandle =
    handle.withInvokeExactBehavior()
}

class VarHandleIntInstanceExactMatrixTest
    extends VarHandleIntInstanceMatrixTest {
  override protected def invocationHandle(handle: VarHandle): VarHandle =
    handle.withInvokeExactBehavior()
}

class VarHandleLongInstanceExactMatrixTest
    extends VarHandleLongInstanceMatrixTest {
  override protected def invocationHandle(handle: VarHandle): VarHandle =
    handle.withInvokeExactBehavior()
}

class VarHandleFloatInstanceExactMatrixTest
    extends VarHandleFloatInstanceMatrixTest {
  override protected def invocationHandle(handle: VarHandle): VarHandle =
    handle.withInvokeExactBehavior()
}

class VarHandleDoubleInstanceExactMatrixTest
    extends VarHandleDoubleInstanceMatrixTest {
  override protected def invocationHandle(handle: VarHandle): VarHandle =
    handle.withInvokeExactBehavior()
}

class VarHandleReferenceInstanceExactMatrixTest
    extends VarHandleReferenceInstanceMatrixTest {
  override protected def invocationHandle(handle: VarHandle): VarHandle =
    handle.withInvokeExactBehavior()
}
