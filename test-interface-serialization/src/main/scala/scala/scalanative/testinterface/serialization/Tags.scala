package scala.scalanative
package testinterface
package serialization

object Tags {

  final val Message   = 0
  final val Event     = Message + 1
  final val Log       = Event + 1
  final val Failure   = Log + 1
  final val TaskInfos = Failure + 1

  final val Command    = TaskInfos + 1
  final val SendInfo   = Command + 1
  final val NewRunner  = SendInfo + 1
  final val RunnerDone = NewRunner + 1
  final val Tasks      = RunnerDone + 1
  final val Execute    = Tasks + 1

  final val Fingerprint          = Execute + 1
  final val AnnotatedFingerprint = Fingerprint + 1
  final val SubclassFingerprint  = AnnotatedFingerprint + 1

  final val Level = SubclassFingerprint + 1
  final val Error = Level + 1
  final val Warn  = Error + 1
  final val Info  = Warn + 1
  final val Debug = Info + 1
  final val Trace = Debug + 1

  final val SuiteSelector        = Trace + 1
  final val TestSelector         = SuiteSelector + 1
  final val NestedSuiteSelector  = TestSelector + 1
  final val NestedTestSelector   = NestedSuiteSelector + 1
  final val TestWildcardSelector = NestedTestSelector + 1

}
