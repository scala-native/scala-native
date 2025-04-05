package java.util

import java.{util => ju}

abstract class ResourceBundle {
  def getKeys(): ju.Enumeration[String]

  protected def handleGetObject(key: String): Object

  private var parent: ResourceBundle = _
  protected def getParent(): ResourceBundle = parent
  def setParent(parent: ResourceBundle): Unit = {
    this.parent = parent
  }

  def getBaseBundleName(): String = null

  // private var locale: Locale = _
  // def getLocale(): Locale = locale
  // protected[util] def setLocale(locale: Locale): Unit = {
  //   this.locale = locale
  // }

  def getObject(key: String): Object = {
    Objects.requireNonNull(key)

    var obj = handleGetObject(key)
    if (obj == null) {
      if (parent != null) {
        obj = parent.getObject(key)
      }
      if (obj == null) {
        throw new MissingResourceException(
          s"Can't find resource for bundle $this, key $key",
          this.getClass.getName,
          key
        )
      }
    }
    obj
  }

  def getString(key: String): String = {
    getObject(key) match {
      case s: String => s
      case obj =>
        throw new ClassCastException(
          s"'$key' in bundle ${this.getClass.getName} is not a string but ${obj.getClass.getName}"
        )
    }
  }

  def getStringArray(key: String): Array[String] = {
    getObject(key) match {
      case arr: Array[String] => arr
      case obj =>
        throw new ClassCastException(
          s"'$key' in bundle ${this.getClass.getName} is not a string array but ${obj.getClass.getName}"
        )
    }
  }

  def keySet(): ju.Set[String] = {
    val set = new ju.HashSet[String]()
    var bundle = this
    while (bundle != null) {
      val keys = bundle.handleKeySet()
      if (keys != null) {
        set.addAll(keys)
      }
      bundle = bundle.parent
    }
    ju.Collections.unmodifiableSet(set)
  }

  def containsKey(key: String): Boolean = {
    if (key == null) {
      throw new NullPointerException("key is null")
    }
    var bundle = this
    while (bundle != null) {
      if (bundle.handleKeySet().contains(key)) {
        return true
      }
      bundle = bundle.parent
    }
    false
  }

  protected def handleKeySet(): ju.Set[String] = {
    val keys = new ju.HashSet[String]()
    val enum_ = getKeys()
    while (enum_.hasMoreElements()) {
      keys.add(enum_.nextElement())
    }
    keys
  }
}

object ResourceBundle {
  // def getBundle(baseName: String): ResourceBundle = ???
  // def getBundle(baseName: String, locale: Locale): ResourceBundle = ???
  // def getBundle(baseName: String, control: Control): ResourceBundle = ???
  // def getBundle(baseName: String, locale: Locale, control: Control): ResourceBundle = ???
  // def getBundle(baseName: String, locale: Locale, loader: ClassLoader): ResourceBundle = ???
  // def getBundle(
  //     baseName: String,
  //     targetLocale: Locale,
  //     loader: ClassLoader,
  //     control: Control
  // ): ResourceBundle = ???
  // def getBundle(
  //     baseName: String,
  //     locale: Locale,
  //     loader: ClassLoader,
  //     format: scala.Boolean
  // ): ResourceBundle = ???

  def clearCache(): Unit = ()
  def clearCache(loader: ClassLoader): Unit = ()

  // object Control {
  //   final val FORMAT_DEFAULT = List.of("java.class", "java.properties")
  //   final val FORMAT_CLASS = List.of("java.class")
  //   final val FORMAT_PROPERTIES = List.of("java.properties")
  //   final val TTL_DONT_CACHE: scala.Long = -1L
  //   final val TTL_NO_EXPIRATION_CONTROL: scala.Long = -2L

  //   def getControl(formats: ju.List[String]): Control
  //   def getNoFallbackControl(formats: ju.List[String]): Control
  // }

  // abstract class Control protected () {
  //   def getFormats(baseName: String): ju.List[String] = ???
  //   def getCandidateLocales(
  //       baseName: String,
  //       locale: Locale
  //   ): ju.List[Locale] = ???
  //   def getFallbackLocale(
  //       baseName: String,
  //       locale: Locale
  //   ): Locale = ???
  //   def newBundle(
  //       baseName: String,
  //       locale: Locale,
  //       format: String,
  //       loader: ClassLoader,
  //       reload: scala.Boolean
  //   ): ResourceBundle = ???
  //   def getTimeToLive(
  //       baseName: String,
  //       locale: Locale
  //   ): scala.Long = ???
  //   def needsReload(
  //       baseName: String,
  //       locale: Locale,
  //       format: String,
  //       loader: ClassLoader,
  //       bundle: ResourceBundle,
  //       loadTime: scala.Long
  //   ): scala.Boolean = ???
  //   def toBundleName(
  //       baseName: String,
  //       locale: Locale
  //   ): String = ???

  //   def toResourceName(
  //       bundleName: String,
  //       suffix: String
  //   ): String = ???
  // }
}
