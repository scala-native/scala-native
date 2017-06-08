package scala.scalanative
package testinterface
package serialization

import scala.compat.Platform.EOL
import sbt.testing._

import scala.collection.mutable.ListBuffer

object Serializer {
  def serialize[T: Serializable](v: T): Iterator[String] =
    implicitly[Serializable[T]].serialize(v)
  def deserialize[T: Serializable](in: Iterator[String]): T =
    implicitly[Serializable[T]].deserialize(in)

  import Serializer.{serialize => s, deserialize => d}

  implicit val TaskDefSerializer = new Serializable[TaskDef] {
    override def serialize(v: TaskDef): Iterator[String] =
      s(v.fullyQualifiedName) ++
        s(v.fingerprint) ++
        s(v.explicitlySpecified) ++
        s(v.selectors.toSeq)

    override def deserialize(in: Iterator[String]): TaskDef = {
      val fullyQualifiedName  = d[String](in)
      val fingerprint         = d[Fingerprint](in)
      val explicitlySpecified = d[Boolean](in)
      val selectors           = d[Seq[Selector]](in)
      new TaskDef(fullyQualifiedName,
                  fingerprint,
                  explicitlySpecified,
                  selectors.toArray)
    }
  }

  implicit val BooleanSerializer: Serializable[Boolean] =
    Serializable(v => Iterator(v.toString),
                 in => java.lang.Boolean.parseBoolean(in.next()))

  implicit val IntSerializer: Serializable[Int] =
    Serializable(v => Iterator(v.toString),
                 in => java.lang.Integer.parseInt(in.next()))

  implicit val LongSerializer: Serializable[Long] =
    Serializable(v => Iterator(v.toString),
                 in => java.lang.Long.parseLong(in.next()))

  implicit val StringSerializer: Serializable[String] =
    Serializable(Iterator(_), _.next())

  implicit def SeqSerializer[T: Serializable]: Serializable[Seq[T]] =
    Serializable(v => s(v.length) ++ v.toIterator.flatMap(s[T]),
                 in => Seq.fill(d[Int](in))(d[T](in)))

  implicit def IteratorSerializer[T: Serializable]: Serializable[Iterator[T]] =
    Serializable(v => s[Seq[T]](v.toSeq), in => d[Seq[T]](in).toIterator)

  implicit def OptionSerializer[T: Serializable]: Serializable[Option[T]] =
    Serializable(v => s(v.toSeq), in => d[Seq[T]](in).headOption)

  implicit def Tuple2Serializer[A: Serializable, B: Serializable]
    : Serializable[(A, B)] =
    Serializable({ case (a, b) => s(a) ++ s(b) }, in => (d[A](in), d[B](in)))

  implicit def EitherSerializer[A: Serializable, B: Serializable]
    : Serializable[Either[A, B]] =
    Serializable(
      {
        case Left(a)  => s(a)
        case Right(b) => s(b)
      },
      in => {
        val recordingIterator = new RecordingIterator(in)
        try {
          Left(d[A](recordingIterator))
        } catch {
          // TODO: DeserializationException
          case _: Exception =>
            Right(d[B](recordingIterator.rewind()))
        }
      }
    )

  implicit val SelectorSerializer: Serializable[Selector] =
    Serializable(
      {
        case _: SuiteSelector =>
          s("SuiteSelector")
        case ts: TestSelector =>
          s("TestSelector") ++ s(ts.testName)
        case nss: NestedSuiteSelector =>
          s("NestedSuiteSelector") ++ s(nss.suiteId)
        case nts: NestedTestSelector =>
          s("NestedTestSelector") ++ s(nts.suiteId) ++ s(nts.testName)
        case tws: TestWildcardSelector =>
          s("TestWildcardSelector") ++ s(tws.testWildcard)
      },
      in =>
        in.next() match {
          case "SuiteSelector" =>
            new SuiteSelector()
          case "TestSelector" =>
            new TestSelector(d[String](in))
          case "NestedSuiteSelector" =>
            new NestedSuiteSelector(d[String](in))
          case "NestedTestSelector" =>
            new NestedTestSelector(d[String](in), d[String](in))
          case "TestWildcardSelector" =>
            new TestWildcardSelector(d[String](in))
      }
    )

  implicit val FingerprintSerializer: Serializable[Fingerprint] =
    Serializable(
      {
        case af: AnnotatedFingerprint =>
          s("AnnotatedFingerprint") ++
            s(af.isModule) ++ s(af.annotationName)
        case sf: SubclassFingerprint =>
          s("SubclassFingerprint") ++
            s(sf.isModule) ++ s(sf.superclassName) ++ s(
            sf.requireNoArgConstructor)
        case unknown =>
          throw new IllegalArgumentException(
            s"Unknown fingerprint type: ${unknown.getClass.getName}")
      },
      in =>
        in.next() match {
          case "AnnotatedFingerprint" =>
            val isModule       = d[Boolean](in)
            val annotationName = d[String](in)
            DeserializedAnnotatedFingerprint(isModule, annotationName)
          case "SubclassFingerprint" =>
            val isModule                = d[Boolean](in)
            val superclassName          = d[String](in)
            val requireNoArgConstructor = d[Boolean](in)
            DeserializedSubclassFingerprint(isModule,
                                            superclassName,
                                            requireNoArgConstructor)
          case unknown =>
            throw new IllegalArgumentException(
              s"Unknown fingerprint type: $unknown")
      }
    )

  implicit val StackTraceElementSerializable: Serializable[StackTraceElement] =
    Serializable(
      v =>
        s(v.getClassName) ++ s(v.getMethodName) ++ s(v.getFileName) ++ s(
          v.getLineNumber),
      in => {
        val className  = d[String](in)
        val methodName = d[String](in)
        val fileName   = d[String](in)
        val lineNumber = d[Int](in)
        new StackTraceElement(className, methodName, fileName, lineNumber)
      }
    )

  implicit val ThrowableSerializer: Serializable[Throwable] =
    Serializable(
      v =>
        s(v.getClass().toString()) ++ s(v.getMessage().lines) ++ s(
          v.toString().lines) ++ s(v.getStackTrace().toSeq) ++ s(
          Option(v.getCause())),
      in => {
        val originalClass = d[String](in)
        val message       = d[Iterator[String]](in).mkString(EOL)
        val toString      = d[Iterator[String]](in).mkString(EOL)
        val trace         = d[Seq[StackTraceElement]](in).toArray
        val cause         = d[Option[Throwable]](in).orNull
        val ex            = new RemoteException(message, toString, cause, originalClass)
        ex.setStackTrace(trace)
        ex
      }
    )

  private class RecordingIterator[T](it: Iterator[T]) extends Iterator[T] {
    private val elements: ListBuffer[T] = ListBuffer.empty
    def rewind(): Iterator[T]           = elements.toIterator ++ it
    override def hasNext: Boolean       = it.hasNext
    override def next(): T = {
      val element = it.next()
      elements += element
      element
    }
  }
}
