package org.scalanative.testsuite.javalib.invoke

import java.lang.invoke.VarHandle

// Inherit the full 31-operation matrix for every variable type in exact mode.

class VarHandleBooleanStaticExactMatrixTest
    extends VarHandleBooleanStaticMatrixTest {
  override protected def invocationHandle(handle: VarHandle): VarHandle =
    handle.withInvokeExactBehavior()
}

class VarHandleByteStaticExactMatrixTest extends VarHandleByteStaticMatrixTest {
  override protected def invocationHandle(handle: VarHandle): VarHandle =
    handle.withInvokeExactBehavior()
}

class VarHandleShortStaticExactMatrixTest
    extends VarHandleShortStaticMatrixTest {
  override protected def invocationHandle(handle: VarHandle): VarHandle =
    handle.withInvokeExactBehavior()
}

class VarHandleCharStaticExactMatrixTest extends VarHandleCharStaticMatrixTest {
  override protected def invocationHandle(handle: VarHandle): VarHandle =
    handle.withInvokeExactBehavior()
}

class VarHandleIntStaticExactMatrixTest extends VarHandleIntStaticMatrixTest {
  override protected def invocationHandle(handle: VarHandle): VarHandle =
    handle.withInvokeExactBehavior()
}

class VarHandleLongStaticExactMatrixTest extends VarHandleLongStaticMatrixTest {
  override protected def invocationHandle(handle: VarHandle): VarHandle =
    handle.withInvokeExactBehavior()
}

class VarHandleFloatStaticExactMatrixTest
    extends VarHandleFloatStaticMatrixTest {
  override protected def invocationHandle(handle: VarHandle): VarHandle =
    handle.withInvokeExactBehavior()
}

class VarHandleDoubleStaticExactMatrixTest
    extends VarHandleDoubleStaticMatrixTest {
  override protected def invocationHandle(handle: VarHandle): VarHandle =
    handle.withInvokeExactBehavior()
}

class VarHandleReferenceStaticExactMatrixTest
    extends VarHandleReferenceStaticMatrixTest {
  override protected def invocationHandle(handle: VarHandle): VarHandle =
    handle.withInvokeExactBehavior()
}
