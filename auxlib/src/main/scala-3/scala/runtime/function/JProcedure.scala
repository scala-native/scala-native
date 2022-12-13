// ###sourceLocation(file: "scala/runtime/function/JProcedure.scala.gyb", line: 1)
// BEWARE: This file is generated - direct edits will be lost.
// Do not edit this it directly other than to remove
// personally identifiable information in sourceLocation lines.
// All direct edits to this file will be lost the next time it
// is generated.
//
// See nativelib runtime/Arrays.scala.gyb for details.

package scala.runtime.function

import scala.runtime.BoxedUnit

trait JProcedure0 extends scala.Function0[Object] with java.io.Serializable {
  def applyVoid(): Unit

  def apply(): Object = {
    applyVoid()
    return BoxedUnit.UNIT
  }
}

// ###sourceLocation(file: "scala/runtime/function/JProcedure.scala.gyb", line: 30)
trait JProcedure1[T1]
    extends scala.Function1[T1, Object]
    with java.io.Serializable {
  def applyVoid(t1: T1): Unit

  def apply(t1: T1): Object = {
    applyVoid(t1)
    return BoxedUnit.UNIT
  }
}
// ###sourceLocation(file: "scala/runtime/function/JProcedure.scala.gyb", line: 30)
trait JProcedure2[T1, T2]
    extends scala.Function2[T1, T2, Object]
    with java.io.Serializable {
  def applyVoid(t1: T1, t2: T2): Unit

  def apply(t1: T1, t2: T2): Object = {
    applyVoid(t1, t2)
    return BoxedUnit.UNIT
  }
}
// ###sourceLocation(file: "scala/runtime/function/JProcedure.scala.gyb", line: 30)
trait JProcedure3[T1, T2, T3]
    extends scala.Function3[T1, T2, T3, Object]
    with java.io.Serializable {
  def applyVoid(t1: T1, t2: T2, t3: T3): Unit

  def apply(t1: T1, t2: T2, t3: T3): Object = {
    applyVoid(t1, t2, t3)
    return BoxedUnit.UNIT
  }
}
// ###sourceLocation(file: "scala/runtime/function/JProcedure.scala.gyb", line: 30)
trait JProcedure4[T1, T2, T3, T4]
    extends scala.Function4[T1, T2, T3, T4, Object]
    with java.io.Serializable {
  def applyVoid(t1: T1, t2: T2, t3: T3, t4: T4): Unit

  def apply(t1: T1, t2: T2, t3: T3, t4: T4): Object = {
    applyVoid(t1, t2, t3, t4)
    return BoxedUnit.UNIT
  }
}
// ###sourceLocation(file: "scala/runtime/function/JProcedure.scala.gyb", line: 30)
trait JProcedure5[T1, T2, T3, T4, T5]
    extends scala.Function5[T1, T2, T3, T4, T5, Object]
    with java.io.Serializable {
  def applyVoid(t1: T1, t2: T2, t3: T3, t4: T4, t5: T5): Unit

  def apply(t1: T1, t2: T2, t3: T3, t4: T4, t5: T5): Object = {
    applyVoid(t1, t2, t3, t4, t5)
    return BoxedUnit.UNIT
  }
}
// ###sourceLocation(file: "scala/runtime/function/JProcedure.scala.gyb", line: 30)
trait JProcedure6[T1, T2, T3, T4, T5, T6]
    extends scala.Function6[T1, T2, T3, T4, T5, T6, Object]
    with java.io.Serializable {
  def applyVoid(t1: T1, t2: T2, t3: T3, t4: T4, t5: T5, t6: T6): Unit

  def apply(t1: T1, t2: T2, t3: T3, t4: T4, t5: T5, t6: T6): Object = {
    applyVoid(t1, t2, t3, t4, t5, t6)
    return BoxedUnit.UNIT
  }
}
// ###sourceLocation(file: "scala/runtime/function/JProcedure.scala.gyb", line: 30)
trait JProcedure7[T1, T2, T3, T4, T5, T6, T7]
    extends scala.Function7[T1, T2, T3, T4, T5, T6, T7, Object]
    with java.io.Serializable {
  def applyVoid(t1: T1, t2: T2, t3: T3, t4: T4, t5: T5, t6: T6, t7: T7): Unit

  def apply(t1: T1, t2: T2, t3: T3, t4: T4, t5: T5, t6: T6, t7: T7): Object = {
    applyVoid(t1, t2, t3, t4, t5, t6, t7)
    return BoxedUnit.UNIT
  }
}
// ###sourceLocation(file: "scala/runtime/function/JProcedure.scala.gyb", line: 30)
trait JProcedure8[T1, T2, T3, T4, T5, T6, T7, T8]
    extends scala.Function8[T1, T2, T3, T4, T5, T6, T7, T8, Object]
    with java.io.Serializable {
  def applyVoid(t1: T1, t2: T2, t3: T3, t4: T4, t5: T5, t6: T6, t7: T7, t8: T8)
      : Unit

  def apply(t1: T1, t2: T2, t3: T3, t4: T4, t5: T5, t6: T6, t7: T7, t8: T8)
      : Object = {
    applyVoid(t1, t2, t3, t4, t5, t6, t7, t8)
    return BoxedUnit.UNIT
  }
}
// ###sourceLocation(file: "scala/runtime/function/JProcedure.scala.gyb", line: 30)
trait JProcedure9[T1, T2, T3, T4, T5, T6, T7, T8, T9]
    extends scala.Function9[T1, T2, T3, T4, T5, T6, T7, T8, T9, Object]
    with java.io.Serializable {
  def applyVoid(
      t1: T1,
      t2: T2,
      t3: T3,
      t4: T4,
      t5: T5,
      t6: T6,
      t7: T7,
      t8: T8,
      t9: T9
  ): Unit

  def apply(
      t1: T1,
      t2: T2,
      t3: T3,
      t4: T4,
      t5: T5,
      t6: T6,
      t7: T7,
      t8: T8,
      t9: T9
  ): Object = {
    applyVoid(t1, t2, t3, t4, t5, t6, t7, t8, t9)
    return BoxedUnit.UNIT
  }
}
// ###sourceLocation(file: "scala/runtime/function/JProcedure.scala.gyb", line: 30)
trait JProcedure10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10]
    extends scala.Function10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, Object]
    with java.io.Serializable {
  def applyVoid(
      t1: T1,
      t2: T2,
      t3: T3,
      t4: T4,
      t5: T5,
      t6: T6,
      t7: T7,
      t8: T8,
      t9: T9,
      t10: T10
  ): Unit

  def apply(
      t1: T1,
      t2: T2,
      t3: T3,
      t4: T4,
      t5: T5,
      t6: T6,
      t7: T7,
      t8: T8,
      t9: T9,
      t10: T10
  ): Object = {
    applyVoid(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10)
    return BoxedUnit.UNIT
  }
}
// ###sourceLocation(file: "scala/runtime/function/JProcedure.scala.gyb", line: 30)
trait JProcedure11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11]
    extends scala.Function11[
      T1,
      T2,
      T3,
      T4,
      T5,
      T6,
      T7,
      T8,
      T9,
      T10,
      T11,
      Object
    ]
    with java.io.Serializable {
  def applyVoid(
      t1: T1,
      t2: T2,
      t3: T3,
      t4: T4,
      t5: T5,
      t6: T6,
      t7: T7,
      t8: T8,
      t9: T9,
      t10: T10,
      t11: T11
  ): Unit

  def apply(
      t1: T1,
      t2: T2,
      t3: T3,
      t4: T4,
      t5: T5,
      t6: T6,
      t7: T7,
      t8: T8,
      t9: T9,
      t10: T10,
      t11: T11
  ): Object = {
    applyVoid(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11)
    return BoxedUnit.UNIT
  }
}
// ###sourceLocation(file: "scala/runtime/function/JProcedure.scala.gyb", line: 30)
trait JProcedure12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12]
    extends scala.Function12[
      T1,
      T2,
      T3,
      T4,
      T5,
      T6,
      T7,
      T8,
      T9,
      T10,
      T11,
      T12,
      Object
    ]
    with java.io.Serializable {
  def applyVoid(
      t1: T1,
      t2: T2,
      t3: T3,
      t4: T4,
      t5: T5,
      t6: T6,
      t7: T7,
      t8: T8,
      t9: T9,
      t10: T10,
      t11: T11,
      t12: T12
  ): Unit

  def apply(
      t1: T1,
      t2: T2,
      t3: T3,
      t4: T4,
      t5: T5,
      t6: T6,
      t7: T7,
      t8: T8,
      t9: T9,
      t10: T10,
      t11: T11,
      t12: T12
  ): Object = {
    applyVoid(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12)
    return BoxedUnit.UNIT
  }
}
// ###sourceLocation(file: "scala/runtime/function/JProcedure.scala.gyb", line: 30)
trait JProcedure13[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13]
    extends scala.Function13[
      T1,
      T2,
      T3,
      T4,
      T5,
      T6,
      T7,
      T8,
      T9,
      T10,
      T11,
      T12,
      T13,
      Object
    ]
    with java.io.Serializable {
  def applyVoid(
      t1: T1,
      t2: T2,
      t3: T3,
      t4: T4,
      t5: T5,
      t6: T6,
      t7: T7,
      t8: T8,
      t9: T9,
      t10: T10,
      t11: T11,
      t12: T12,
      t13: T13
  ): Unit

  def apply(
      t1: T1,
      t2: T2,
      t3: T3,
      t4: T4,
      t5: T5,
      t6: T6,
      t7: T7,
      t8: T8,
      t9: T9,
      t10: T10,
      t11: T11,
      t12: T12,
      t13: T13
  ): Object = {
    applyVoid(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13)
    return BoxedUnit.UNIT
  }
}
// ###sourceLocation(file: "scala/runtime/function/JProcedure.scala.gyb", line: 30)
trait JProcedure14[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14]
    extends scala.Function14[
      T1,
      T2,
      T3,
      T4,
      T5,
      T6,
      T7,
      T8,
      T9,
      T10,
      T11,
      T12,
      T13,
      T14,
      Object
    ]
    with java.io.Serializable {
  def applyVoid(
      t1: T1,
      t2: T2,
      t3: T3,
      t4: T4,
      t5: T5,
      t6: T6,
      t7: T7,
      t8: T8,
      t9: T9,
      t10: T10,
      t11: T11,
      t12: T12,
      t13: T13,
      t14: T14
  ): Unit

  def apply(
      t1: T1,
      t2: T2,
      t3: T3,
      t4: T4,
      t5: T5,
      t6: T6,
      t7: T7,
      t8: T8,
      t9: T9,
      t10: T10,
      t11: T11,
      t12: T12,
      t13: T13,
      t14: T14
  ): Object = {
    applyVoid(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14)
    return BoxedUnit.UNIT
  }
}
// ###sourceLocation(file: "scala/runtime/function/JProcedure.scala.gyb", line: 30)
trait JProcedure15[
    T1,
    T2,
    T3,
    T4,
    T5,
    T6,
    T7,
    T8,
    T9,
    T10,
    T11,
    T12,
    T13,
    T14,
    T15
] extends scala.Function15[
      T1,
      T2,
      T3,
      T4,
      T5,
      T6,
      T7,
      T8,
      T9,
      T10,
      T11,
      T12,
      T13,
      T14,
      T15,
      Object
    ]
    with java.io.Serializable {
  def applyVoid(
      t1: T1,
      t2: T2,
      t3: T3,
      t4: T4,
      t5: T5,
      t6: T6,
      t7: T7,
      t8: T8,
      t9: T9,
      t10: T10,
      t11: T11,
      t12: T12,
      t13: T13,
      t14: T14,
      t15: T15
  ): Unit

  def apply(
      t1: T1,
      t2: T2,
      t3: T3,
      t4: T4,
      t5: T5,
      t6: T6,
      t7: T7,
      t8: T8,
      t9: T9,
      t10: T10,
      t11: T11,
      t12: T12,
      t13: T13,
      t14: T14,
      t15: T15
  ): Object = {
    applyVoid(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15)
    return BoxedUnit.UNIT
  }
}
// ###sourceLocation(file: "scala/runtime/function/JProcedure.scala.gyb", line: 30)
trait JProcedure16[
    T1,
    T2,
    T3,
    T4,
    T5,
    T6,
    T7,
    T8,
    T9,
    T10,
    T11,
    T12,
    T13,
    T14,
    T15,
    T16
] extends scala.Function16[
      T1,
      T2,
      T3,
      T4,
      T5,
      T6,
      T7,
      T8,
      T9,
      T10,
      T11,
      T12,
      T13,
      T14,
      T15,
      T16,
      Object
    ]
    with java.io.Serializable {
  def applyVoid(
      t1: T1,
      t2: T2,
      t3: T3,
      t4: T4,
      t5: T5,
      t6: T6,
      t7: T7,
      t8: T8,
      t9: T9,
      t10: T10,
      t11: T11,
      t12: T12,
      t13: T13,
      t14: T14,
      t15: T15,
      t16: T16
  ): Unit

  def apply(
      t1: T1,
      t2: T2,
      t3: T3,
      t4: T4,
      t5: T5,
      t6: T6,
      t7: T7,
      t8: T8,
      t9: T9,
      t10: T10,
      t11: T11,
      t12: T12,
      t13: T13,
      t14: T14,
      t15: T15,
      t16: T16
  ): Object = {
    applyVoid(
      t1,
      t2,
      t3,
      t4,
      t5,
      t6,
      t7,
      t8,
      t9,
      t10,
      t11,
      t12,
      t13,
      t14,
      t15,
      t16
    )
    return BoxedUnit.UNIT
  }
}
// ###sourceLocation(file: "scala/runtime/function/JProcedure.scala.gyb", line: 30)
trait JProcedure17[
    T1,
    T2,
    T3,
    T4,
    T5,
    T6,
    T7,
    T8,
    T9,
    T10,
    T11,
    T12,
    T13,
    T14,
    T15,
    T16,
    T17
] extends scala.Function17[
      T1,
      T2,
      T3,
      T4,
      T5,
      T6,
      T7,
      T8,
      T9,
      T10,
      T11,
      T12,
      T13,
      T14,
      T15,
      T16,
      T17,
      Object
    ]
    with java.io.Serializable {
  def applyVoid(
      t1: T1,
      t2: T2,
      t3: T3,
      t4: T4,
      t5: T5,
      t6: T6,
      t7: T7,
      t8: T8,
      t9: T9,
      t10: T10,
      t11: T11,
      t12: T12,
      t13: T13,
      t14: T14,
      t15: T15,
      t16: T16,
      t17: T17
  ): Unit

  def apply(
      t1: T1,
      t2: T2,
      t3: T3,
      t4: T4,
      t5: T5,
      t6: T6,
      t7: T7,
      t8: T8,
      t9: T9,
      t10: T10,
      t11: T11,
      t12: T12,
      t13: T13,
      t14: T14,
      t15: T15,
      t16: T16,
      t17: T17
  ): Object = {
    applyVoid(
      t1,
      t2,
      t3,
      t4,
      t5,
      t6,
      t7,
      t8,
      t9,
      t10,
      t11,
      t12,
      t13,
      t14,
      t15,
      t16,
      t17
    )
    return BoxedUnit.UNIT
  }
}
// ###sourceLocation(file: "scala/runtime/function/JProcedure.scala.gyb", line: 30)
trait JProcedure18[
    T1,
    T2,
    T3,
    T4,
    T5,
    T6,
    T7,
    T8,
    T9,
    T10,
    T11,
    T12,
    T13,
    T14,
    T15,
    T16,
    T17,
    T18
] extends scala.Function18[
      T1,
      T2,
      T3,
      T4,
      T5,
      T6,
      T7,
      T8,
      T9,
      T10,
      T11,
      T12,
      T13,
      T14,
      T15,
      T16,
      T17,
      T18,
      Object
    ]
    with java.io.Serializable {
  def applyVoid(
      t1: T1,
      t2: T2,
      t3: T3,
      t4: T4,
      t5: T5,
      t6: T6,
      t7: T7,
      t8: T8,
      t9: T9,
      t10: T10,
      t11: T11,
      t12: T12,
      t13: T13,
      t14: T14,
      t15: T15,
      t16: T16,
      t17: T17,
      t18: T18
  ): Unit

  def apply(
      t1: T1,
      t2: T2,
      t3: T3,
      t4: T4,
      t5: T5,
      t6: T6,
      t7: T7,
      t8: T8,
      t9: T9,
      t10: T10,
      t11: T11,
      t12: T12,
      t13: T13,
      t14: T14,
      t15: T15,
      t16: T16,
      t17: T17,
      t18: T18
  ): Object = {
    applyVoid(
      t1,
      t2,
      t3,
      t4,
      t5,
      t6,
      t7,
      t8,
      t9,
      t10,
      t11,
      t12,
      t13,
      t14,
      t15,
      t16,
      t17,
      t18
    )
    return BoxedUnit.UNIT
  }
}
// ###sourceLocation(file: "scala/runtime/function/JProcedure.scala.gyb", line: 30)
trait JProcedure19[
    T1,
    T2,
    T3,
    T4,
    T5,
    T6,
    T7,
    T8,
    T9,
    T10,
    T11,
    T12,
    T13,
    T14,
    T15,
    T16,
    T17,
    T18,
    T19
] extends scala.Function19[
      T1,
      T2,
      T3,
      T4,
      T5,
      T6,
      T7,
      T8,
      T9,
      T10,
      T11,
      T12,
      T13,
      T14,
      T15,
      T16,
      T17,
      T18,
      T19,
      Object
    ]
    with java.io.Serializable {
  def applyVoid(
      t1: T1,
      t2: T2,
      t3: T3,
      t4: T4,
      t5: T5,
      t6: T6,
      t7: T7,
      t8: T8,
      t9: T9,
      t10: T10,
      t11: T11,
      t12: T12,
      t13: T13,
      t14: T14,
      t15: T15,
      t16: T16,
      t17: T17,
      t18: T18,
      t19: T19
  ): Unit

  def apply(
      t1: T1,
      t2: T2,
      t3: T3,
      t4: T4,
      t5: T5,
      t6: T6,
      t7: T7,
      t8: T8,
      t9: T9,
      t10: T10,
      t11: T11,
      t12: T12,
      t13: T13,
      t14: T14,
      t15: T15,
      t16: T16,
      t17: T17,
      t18: T18,
      t19: T19
  ): Object = {
    applyVoid(
      t1,
      t2,
      t3,
      t4,
      t5,
      t6,
      t7,
      t8,
      t9,
      t10,
      t11,
      t12,
      t13,
      t14,
      t15,
      t16,
      t17,
      t18,
      t19
    )
    return BoxedUnit.UNIT
  }
}
// ###sourceLocation(file: "scala/runtime/function/JProcedure.scala.gyb", line: 30)
trait JProcedure20[
    T1,
    T2,
    T3,
    T4,
    T5,
    T6,
    T7,
    T8,
    T9,
    T10,
    T11,
    T12,
    T13,
    T14,
    T15,
    T16,
    T17,
    T18,
    T19,
    T20
] extends scala.Function20[
      T1,
      T2,
      T3,
      T4,
      T5,
      T6,
      T7,
      T8,
      T9,
      T10,
      T11,
      T12,
      T13,
      T14,
      T15,
      T16,
      T17,
      T18,
      T19,
      T20,
      Object
    ]
    with java.io.Serializable {
  def applyVoid(
      t1: T1,
      t2: T2,
      t3: T3,
      t4: T4,
      t5: T5,
      t6: T6,
      t7: T7,
      t8: T8,
      t9: T9,
      t10: T10,
      t11: T11,
      t12: T12,
      t13: T13,
      t14: T14,
      t15: T15,
      t16: T16,
      t17: T17,
      t18: T18,
      t19: T19,
      t20: T20
  ): Unit

  def apply(
      t1: T1,
      t2: T2,
      t3: T3,
      t4: T4,
      t5: T5,
      t6: T6,
      t7: T7,
      t8: T8,
      t9: T9,
      t10: T10,
      t11: T11,
      t12: T12,
      t13: T13,
      t14: T14,
      t15: T15,
      t16: T16,
      t17: T17,
      t18: T18,
      t19: T19,
      t20: T20
  ): Object = {
    applyVoid(
      t1,
      t2,
      t3,
      t4,
      t5,
      t6,
      t7,
      t8,
      t9,
      t10,
      t11,
      t12,
      t13,
      t14,
      t15,
      t16,
      t17,
      t18,
      t19,
      t20
    )
    return BoxedUnit.UNIT
  }
}
// ###sourceLocation(file: "scala/runtime/function/JProcedure.scala.gyb", line: 30)
trait JProcedure21[
    T1,
    T2,
    T3,
    T4,
    T5,
    T6,
    T7,
    T8,
    T9,
    T10,
    T11,
    T12,
    T13,
    T14,
    T15,
    T16,
    T17,
    T18,
    T19,
    T20,
    T21
] extends scala.Function21[
      T1,
      T2,
      T3,
      T4,
      T5,
      T6,
      T7,
      T8,
      T9,
      T10,
      T11,
      T12,
      T13,
      T14,
      T15,
      T16,
      T17,
      T18,
      T19,
      T20,
      T21,
      Object
    ]
    with java.io.Serializable {
  def applyVoid(
      t1: T1,
      t2: T2,
      t3: T3,
      t4: T4,
      t5: T5,
      t6: T6,
      t7: T7,
      t8: T8,
      t9: T9,
      t10: T10,
      t11: T11,
      t12: T12,
      t13: T13,
      t14: T14,
      t15: T15,
      t16: T16,
      t17: T17,
      t18: T18,
      t19: T19,
      t20: T20,
      t21: T21
  ): Unit

  def apply(
      t1: T1,
      t2: T2,
      t3: T3,
      t4: T4,
      t5: T5,
      t6: T6,
      t7: T7,
      t8: T8,
      t9: T9,
      t10: T10,
      t11: T11,
      t12: T12,
      t13: T13,
      t14: T14,
      t15: T15,
      t16: T16,
      t17: T17,
      t18: T18,
      t19: T19,
      t20: T20,
      t21: T21
  ): Object = {
    applyVoid(
      t1,
      t2,
      t3,
      t4,
      t5,
      t6,
      t7,
      t8,
      t9,
      t10,
      t11,
      t12,
      t13,
      t14,
      t15,
      t16,
      t17,
      t18,
      t19,
      t20,
      t21
    )
    return BoxedUnit.UNIT
  }
}
// ###sourceLocation(file: "scala/runtime/function/JProcedure.scala.gyb", line: 30)
trait JProcedure22[
    T1,
    T2,
    T3,
    T4,
    T5,
    T6,
    T7,
    T8,
    T9,
    T10,
    T11,
    T12,
    T13,
    T14,
    T15,
    T16,
    T17,
    T18,
    T19,
    T20,
    T21,
    T22
] extends scala.Function22[
      T1,
      T2,
      T3,
      T4,
      T5,
      T6,
      T7,
      T8,
      T9,
      T10,
      T11,
      T12,
      T13,
      T14,
      T15,
      T16,
      T17,
      T18,
      T19,
      T20,
      T21,
      T22,
      Object
    ]
    with java.io.Serializable {
  def applyVoid(
      t1: T1,
      t2: T2,
      t3: T3,
      t4: T4,
      t5: T5,
      t6: T6,
      t7: T7,
      t8: T8,
      t9: T9,
      t10: T10,
      t11: T11,
      t12: T12,
      t13: T13,
      t14: T14,
      t15: T15,
      t16: T16,
      t17: T17,
      t18: T18,
      t19: T19,
      t20: T20,
      t21: T21,
      t22: T22
  ): Unit

  def apply(
      t1: T1,
      t2: T2,
      t3: T3,
      t4: T4,
      t5: T5,
      t6: T6,
      t7: T7,
      t8: T8,
      t9: T9,
      t10: T10,
      t11: T11,
      t12: T12,
      t13: T13,
      t14: T14,
      t15: T15,
      t16: T16,
      t17: T17,
      t18: T18,
      t19: T19,
      t20: T20,
      t21: T21,
      t22: T22
  ): Object = {
    applyVoid(
      t1,
      t2,
      t3,
      t4,
      t5,
      t6,
      t7,
      t8,
      t9,
      t10,
      t11,
      t12,
      t13,
      t14,
      t15,
      t16,
      t17,
      t18,
      t19,
      t20,
      t21,
      t22
    )
    return BoxedUnit.UNIT
  }
}
