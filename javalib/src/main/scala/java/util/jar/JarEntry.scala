package java.util.jar

// Ported from Apache Harmony

import java.io.IOException
import java.security.CodeSigner
import java.security.cert.{
  CertPath,
  Certificate,
  CertificateException,
  CertificateFactory,
  X509Certificate
}
import java.util.zip.{ZipConstants, ZipEntry}
import java.util.{ArrayList, List}

import javax.security.auth.x500.X500Principal

class JarEntry(private val ze: ZipEntry) extends ZipEntry(ze) {
  def this(je: JarEntry) = this(je.ze)
  def this(name: String) = this(new ZipEntry(name))

  private var attributes: Attributes            = null
  protected[jar] var parentJar: JarFile         = null
  protected[jar] var signers: Array[CodeSigner] = null

  private var factory: CertificateFactory = null
  private var isFactoryChecked: Boolean   = false

  def getAttributes(): Attributes =
    if (attributes != null || parentJar == null) {
      attributes
    } else {
      val manifest = parentJar.getManifest()
      if (manifest == null) {
        null
      } else {
        attributes = manifest.getAttributes(getName())
        attributes
      }
    }

  def getCertificates(): Array[Certificate] =
    if (parentJar == null) {
      null
    } else {
      val jarVerifier = parentJar.verifier
      if (jarVerifier == null) {
        null
      } else {
        jarVerifier.getCertificates(getName())
      }
    }

  private[jar] def setAttributes(attrib: Attributes): Unit =
    attributes = attrib

  def getCodeSigners(): Array[CodeSigner] =
    if (signers == null) {
      signers = getCodeSigners(getCertificates())
      signers
    } else {
      val tmp = new Array[CodeSigner](signers.length)
      System.arraycopy(signers, 0, tmp, 0, tmp.length)
      tmp
    }

  private def getCodeSigners(certs: Array[Certificate]): Array[CodeSigner] =
    if (certs == null) {
      null
    } else {
      var prevIssuer: X500Principal = null
      val list                      = new ArrayList[Certificate](certs.length)
      val asigners                  = new ArrayList[CodeSigner]()

      certs.foreach {
        case x509: X509Certificate =>
          if (prevIssuer != null) {
            // Ok, this ends the previous chain,
            // so transform this one into CertPath
            addCodeSigner(asigners, list)
            // ... and start a new one
            list.clear()
          }
          prevIssuer = x509.getIssuerX500Principal()
          list.add(x509)
        case _ => // Only X509 certificates are taken into account - see API spec.
          ()
      }
      if (!list.isEmpty()) {
        addCodeSigner(asigners, list)
      }
      if (asigners.isEmpty()) {
        null
      } else {
        val tmp = new Array[CodeSigner](asigners.size)
        System.arraycopy(asigners, 0, tmp, 0, asigners.size)
        tmp
      }
    }

  def addCodeSigner(asigners: ArrayList[CodeSigner],
                    list: ArrayList[Certificate]): Unit = {
    if (!isFactoryChecked) {
      try {
        factory = CertificateFactory.getInstance("X.509")
      } catch {
        case _: CertificateException => // do nothing
      } finally {
        isFactoryChecked = true
      }
    }
    if (factory == null) {
      ()
    } else {
      val certPath = scala.util.Try(factory.generateCertPath(list)).toOption
      certPath.foreach { cert =>
        asigners.add(new CodeSigner(cert, null))
      }
    }
  }
}

object JarEntry extends ZipConstants
