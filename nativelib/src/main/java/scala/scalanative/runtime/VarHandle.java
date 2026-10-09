// format: off

package scala.scalanative.runtime;

import scala.Function0;
import scala.Function1;

/** JVM-only symbols for calls emitted by the compiler plugin.
 *
 * nativelib is compiled before javalib, where the Scala object with the real
 * implementation lives. The plugin's JVM tests only see nativelib. These
 * factories let those tests type the generated call; Native links it to the
 * Scala object instead. Access modes dispatch through NativeVarHandle.
 */
public final class VarHandle {
  private VarHandle() {}

  public static java.lang.invoke.VarHandle createBooleanHandle(Function1<Object, Object> instanceBinding, Function0<Object> staticBinding) { throw new AssertionError("stub"); }
  public static java.lang.invoke.VarHandle createByteHandle(Function1<Object, Object> instanceBinding, Function0<Object> staticBinding) { throw new AssertionError("stub"); }
  public static java.lang.invoke.VarHandle createShortHandle(Function1<Object, Object> instanceBinding, Function0<Object> staticBinding) { throw new AssertionError("stub"); }
  public static java.lang.invoke.VarHandle createCharHandle(Function1<Object, Object> instanceBinding, Function0<Object> staticBinding) { throw new AssertionError("stub"); }
  public static java.lang.invoke.VarHandle createIntHandle(Function1<Object, Object> instanceBinding, Function0<Object> staticBinding) { throw new AssertionError("stub"); }
  public static java.lang.invoke.VarHandle createLongHandle(Function1<Object, Object> instanceBinding, Function0<Object> staticBinding) { throw new AssertionError("stub"); }
  public static java.lang.invoke.VarHandle createFloatHandle(Function1<Object, Object> instanceBinding, Function0<Object> staticBinding) { throw new AssertionError("stub"); }
  public static java.lang.invoke.VarHandle createDoubleHandle(Function1<Object, Object> instanceBinding, Function0<Object> staticBinding) { throw new AssertionError("stub"); }
  public static java.lang.invoke.VarHandle createReferenceHandle(Function1<Object, Object> instanceBinding, Function0<Object> staticBinding, Class<?> variableType) { throw new AssertionError("stub"); }
}
