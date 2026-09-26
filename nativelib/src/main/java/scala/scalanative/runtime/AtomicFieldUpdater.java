package scala.scalanative.runtime;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import scala.Function1;

/** Compile-time facade for the Scala Native AtomicFieldUpdater object. */
public final class AtomicFieldUpdater {
  private AtomicFieldUpdater() {}

  public static <T> AtomicIntegerFieldUpdater<T> createIntegerFieldUpdater(Function1<Object, Object> binding) {
    throw new AssertionError("stub");
  }

  public static <T> AtomicLongFieldUpdater<T> createLongFieldUpdater(Function1<Object, Object> binding) {
    throw new AssertionError("stub");
  }

  public static <T, V> AtomicReferenceFieldUpdater<T, V> createReferenceFieldUpdater(Function1<Object, Object> binding) {
    throw new AssertionError("stub");
  }
}
