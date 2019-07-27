package java.util

import java.io.{PrintStream, PrintWriter}
import java.{util => ju}

import scala.collection.JavaConverters._
import scala.scalanative.annotation.stub

class Properties(protected val defaults: Properties)
    extends ju.Hashtable[AnyRef, AnyRef] {

  def this() = this(null)

  def setProperty(key: String, value: String): AnyRef =
    put(key, value)

  @stub
  def load(inStream: java.io.InputStream): Unit = ???

  @stub
  def load(reader: java.io.Reader): Unit = ???

  def getProperty(key: String): String =
    getProperty(key, defaultValue = null)

  def getProperty(key: String, defaultValue: String): String = {
    get(key) match {
      case value: String => value

      case _ =>
        if (defaults != null) defaults.getProperty(key, defaultValue)
        else defaultValue
    }
  }

  def propertyNames(): ju.Enumeration[_] = {
    val thisSet = keySet().asScala.map(_.asInstanceOf[String])
    val defaultsIterator =
      if (defaults != null) defaults.propertyNames().asScala.toIterator
      else scala.collection.Iterator.empty
    val filteredDefaults = defaultsIterator.collect {
      case k: String if !thisSet(k) => k
    }
    (thisSet.iterator ++ filteredDefaults).asJavaEnumeration
  }

  def stringPropertyNames(): ju.Set[String] = {
    val set = new ju.HashSet[String]
    entrySet().asScala.foreach { entry =>
      (entry.getKey, entry.getValue) match {
        case (key: String, _: String) => set.add(key)
        case _                        => // Ignore key
      }
    }
    if (defaults != null)
      set.addAll(defaults.stringPropertyNames())
    set
  }

  private def format(entry: ju.Map.Entry[AnyRef, AnyRef]): String = {
    val key: String   = entry.getKey.asInstanceOf[String]
    val value: String = entry.getValue.asInstanceOf[String]
    if (key.length > 40)
      s"${key.substring(0, 37)}...=$value"
    else
      s"$key=$value"
  }

  def list(out: PrintStream): Unit = {
    out.println("-- listing properties --")
    entrySet().asScala.foreach { entry =>
      out.println(format(entry))
    }
  }

  def list(out: PrintWriter): Unit = {
    out.println("-- listing properties --")
    entrySet().asScala.foreach { entry =>
      out.println(format(entry))
    }
  }

  // TODO:
  // @deprecated("", "") def save(out: OutputStream, comments: String): Unit
  // def store(writer: Writer, comments: String): Unit
  // def store(out: OutputStream, comments: String): Unit
  // def loadFromXML(in: InputStream): Unit
  // def storeToXML(os: OutputStream, comment: String): Unit
  // def storeToXML(os: OutputStream, comment: String, encoding: String): Unit
}
