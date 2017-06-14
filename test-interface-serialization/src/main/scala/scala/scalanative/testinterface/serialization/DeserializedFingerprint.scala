package scala.scalanative
package testinterface

import sbt.testing.{AnnotatedFingerprint, SubclassFingerprint}

final case class DeserializedAnnotatedFingerprint(isModule: Boolean,
                                                  annotationName: String)
    extends AnnotatedFingerprint

final case class DeserializedSubclassFingerprint(
    isModule: Boolean,
    superclassName: String,
    requireNoArgConstructor: Boolean)
    extends SubclassFingerprint
