package java.lang

import java.util.{Set => JSet}
import java.io.{InputStream, IOException}
import java.lang.annotation.Annotation
import java.lang.reflect.AnnotatedElement

final case class Module private[lang] (
    name: String,
    loader: ClassLoader
) {
  def getName(): String = name
  def isNamed(): scala.Boolean = name != null

  def getClassLoader(): ClassLoader = loader

  def getResourceAsStream(name: String): InputStream = loader match {
    case null   => ClassLoader.getSystemClassLoader().getResourceAsStream(name)
    case loader => loader.getResourceAsStream(name)
  }

  // def canRead(other: Module): scala.Boolean = false
  // def addReads(other: Module): Module = this
  // def isExported(pn: String): scala.Boolean = false
  // def isExported(pn: String, other: Module): scala.Boolean = false
  // def isOpen(pn: String): scala.Boolean = ???
  // def isOpen(pn: String, other: Module): scala.Boolean = ???
  // def addExports(pn: String, other: Module): Module = ???
  // def addOpens(pn: String, other: Module): Module = ???
  // def canUse(service: Class[_]): scala.Boolean = ???
  // def addUses(service: Class[_]): Module = ???
  // def getDescriptor(): ModuleDescriptor = null // We don't support module descriptors yet
  // def getLayer(): ModuleLayer = null // We don't support module layers yet
  // def getPackages(): JSet[String] = java.util.Collections.emptySet()
  // def getAnnotation[T <: Annotation](annotationClass: Class[T]): T = null.asInstanceOf[T]
  // def getAnnotations(): Array[Annotation] = new Array[Annotation](0)
  // def getDeclaredAnnotations(): Array[Annotation] = new Array[Annotation](0)
  // def isNativeAccessEnabled(): scala.Boolean = false

  override def toString(): String = {
    if (isNamed()) s"module $name"
    else s"unnamed module @${System.identityHashCode(this).toHexString}"
  }
}
