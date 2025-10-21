package org.scalanative.testsuite.javalib.util.jar

// Ported from Apache Harmony

import java.io._
import java.util.jar._

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

import JarBytes._

class ManifestTest {

  @Test def constructor(): Unit = {
    val emptyManifest = new Manifest()
    assertTrue(emptyManifest.getEntries().isEmpty())
    assertTrue(emptyManifest.getMainAttributes().isEmpty())
  }

  @Test def constructorManifest(): Unit = {
    val firstManifest = new Manifest(
      new ByteArrayInputStream(MANIFEST_CONTENTS.getBytes("ISO-8859-1"))
    )
    val secondManifest = new Manifest(firstManifest)
    assertTrue(firstManifest == secondManifest)
  }

  @Test def constructorInputStream(): Unit = {
    val m = getManifest(hyts_attBytes)
    val baos = new ByteArrayOutputStream()
    m.write(baos)
    val is = new ByteArrayInputStream(baos.toByteArray())
    val mCopy = new Manifest(is)
    assertTrue(m == mCopy)

    val manifest = new Manifest(
      new ByteArrayInputStream(MANIFEST_CONTENTS.getBytes("ISO-8859-1"))
    )
    checkManifest(manifest)

    val manifestContent =
      "Manifest-Version: 1.0\nCreated-By: Apache\nPackage: \nBuild-Jdk: 1.4.1_01\n\n" + "Name: \nSpecification-Title: foo\nSpecification-Version: 1.0\nSpecification-Vendor: \n" + "Implementation-Title: \nImplementation-Version: 1.0\nImplementation-Vendor: \n\n"
    val bis = new ByteArrayInputStream(manifestContent.getBytes("ISO-8859-1"))

    val mf = new Manifest(bis)
    assertTrue(mf.getMainAttributes().size() == 4)

    val entries = mf.getEntries()
    assertTrue(entries.size() == 1)

    val namedEntryAttributes = entries.get("").asInstanceOf[Attributes]
    assertTrue(namedEntryAttributes.size() == 6)

    new Manifest(
      new ByteArrayInputStream(MANIFEST_CONTENTS_2.getBytes("ISO-8859-1"))
    )
  }

  @Test def clear(): Unit = {
    val m = getManifest(hyts_attBytes)
    m.clear()
    assertTrue(m.getEntries().isEmpty())
    assertTrue(m.getMainAttributes().isEmpty())
  }

  @Test def testClone(): Unit = {
    val m = getManifest(hyts_attBytes)
    assertTrue(m == m.clone())
  }

  @Test def equals(): Unit = {
    var firstManifest = new Manifest(
      new ByteArrayInputStream(MANIFEST_CONTENTS.getBytes("ISO-8859-1"))
    )
    val secondManifest = new Manifest(
      new ByteArrayInputStream(MANIFEST_CONTENTS.getBytes("ISO-8859-1"))
    )
    assertTrue(firstManifest == secondManifest)

    val thirdManifest = new Manifest(
      new ByteArrayInputStream(MANIFEST_CONTENTS_1.getBytes("ISO-8859-1"))
    )
    assertTrue(firstManifest != thirdManifest)

    firstManifest = null
    assertTrue(secondManifest != firstManifest)
    assertTrue(!secondManifest.equals(new String("abc")))
  }

  @Test def testHashCode(): Unit = {
    val m = getManifest(hyts_attBytes)
    assertTrue(m.hashCode() == m.clone().hashCode())
  }

  @Test def getAttributes(): Unit = {
    val m = getManifest(hyts_attBytes)
    assertTrue(m.getAttributes("Doesn't Exist") == null)
    assertTrue(
      m.getAttributes(ATT_ENTRY_NAME)
        .get(new Attributes.Name(ATT_ATT_NAME)) == "OK"
    )
  }

  @Test def getEntries(): Unit = {
    val m = getManifest(hyts_attBytes)
    val myMap = m.getEntries()
    assertTrue(myMap.get("Doesn't exist") == null)
    assertTrue(
      myMap.get(ATT_ENTRY_NAME).get(new Attributes.Name(ATT_ATT_NAME)) == "OK"
    )
  }

  @Test def getMainAttributes(): Unit = {
    val m = getManifest(hyts_attBytes)
    val a = m.getMainAttributes()
    assertTrue(a.get(Attributes.Name.MANIFEST_VERSION) == "1.0")
  }

  @Test def writeOutputStream(): Unit = {
    val baos = new ByteArrayOutputStream()
    val m = getManifest(hyts_attBytes)
    // maximum allowed length is 72 for a header, colong and a following
    // space
    var headerName = "Manifest-" + ("0" * (70 - "Manifest-".length))
    assertTrue(headerName.length == 70)
    m.getMainAttributes()
      .put(new Attributes.Name(headerName.toString()), "Value")
    m.write(baos) // ok
  }

  @Test def testNull(): Unit = {
    val manifestContent =
      "Manifest-Version: 1.0\nCreated-By: nasty gcc tool\n\n\u0000"
    val bytes = manifestContent.getBytes("ISO-8859-1")
    new Manifest(new ByteArrayInputStream(bytes)) // the last NUL is ok

    bytes(bytes.length - 1) = 26
    new Manifest(new ByteArrayInputStream(bytes)) // the last EOF is ok

    bytes(bytes.length - 1) = 'A' // the last line ignored
    new Manifest(new ByteArrayInputStream(bytes))

    bytes(2) = 0 // NUL char in Manifest
    assertThrows(
      classOf[IOException],
      new Manifest(new ByteArrayInputStream(bytes))
    )
  }

  @Test def decoding(): Unit = {
    var m = getManifest(hyts_attBytes)
    val bVendor =
      Array[Byte](-48, -100, -48, -72, -48, -69, -48, -80, -47, -113, 32, -48,
        -76, -48, -66, -47, -121, -47, -125, -48, -67, -47, -116, -48, -70, -48,
        -80, 32, -48, -100, -48, -80, -47, -120, -48, -80)
    val bSpec =
      Array[Byte](-31, -120, -80, -31, -120, -117, -31, -120, -99, 32, -31,
        -102, -96, -31, -102, -79, -31, -101, -127, -31, -102, -90, 32, -40,
        -77, -39, -124, -40, -89, -39, -123, 32, -40, -71, -40, -77, -39, -124,
        -40, -89, -39, -123, -40, -87, 32, -36, -85, -36, -96, -36, -95, -36,
        -112, 32, -32, -90, -74, -32, -90, -66, -32, -90, -88, -32, -89, -115,
        -32, -90, -92, -32, -90, -65, 32, -48, -96, -48, -75, -48, -70, -47,
        -118, -48, -75, -48, -69, 32, -48, -100, -48, -72, -47, -128, 32, -32,
        -90, -74, -32, -90, -66, -32, -90, -88, -32, -89, -115, -32, -90, -92,
        -32, -90, -65, 32, -32, -67, -98, -32, -67, -78, -32, -68, -117, -32,
        -67, -106, -32, -67, -111, -32, -67, -70, 32, -48, -100, -48, -80, -47,
        -120, -48, -80, -47, -128, 32, -31, -113, -103, -31, -114, -81, -31,
        -113, -79, 32, -49, -88, -50, -71, -49, -127, -50, -73, -50, -67, -50,
        -73, 32, -34, -112, -34, -86, -34, -123, -34, -90, 32, -32, -67, -126,
        -32, -67, -98, -32, -67, -78, -32, -68, -117, -32, -67, -106, -32, -67,
        -111, -32, -67, -70, 32, -50, -107, -50, -71, -49, -127, -50, -82, -50,
        -67, -50, -73, 32, -40, -75, -39, -124, -40, -83, 32, -32, -86, -74,
        -32, -86, -66, -32, -86, -126, -32, -86, -92, -32, -86, -65, 32, -27,
        -71, -77, -27, -110, -116, 32, -41, -87, -41, -100, -41, -107, -41, -99,
        32, -41, -92, -41, -88, -41, -103, -41, -109, -41, -97, 32, -27, -110,
        -116, -27, -71, -77, 32, -27, -110, -116, -27, -71, -77, 32, -40, -86,
        -39, -119, -39, -122, -38, -122, -39, -124, -39, -119, -39, -126, 32,
        -32, -82, -123, -32, -82, -82, -32, -81, -120, -32, -82, -92, -32, -82,
        -65, 32, -32, -80, -74, -32, -80, -66, -32, -80, -126, -32, -80, -92,
        -32, -80, -65, 32, -32, -72, -86, -32, -72, -79, -32, -72, -103, -32,
        -72, -107, -32, -72, -76, -32, -72, -96, -32, -72, -78, -32, -72, -98,
        32, -31, -120, -80, -31, -120, -117, -31, -120, -99, 32, -32, -73, -125,
        -32, -73, -113, -32, -74, -72, -32, -74, -70, 32, -32, -92, -74, -32,
        -92, -66, -32, -92, -88, -32, -91, -115, -32, -92, -92, -32, -92, -65,
        -32, -92, -125, 32, -31, -125, -101, -31, -125, -88, -31, -125, -107,
        -31, -125, -104, -31, -125, -109, -31, -125, -99, -31, -125, -111, -31,
        -125, -112)
    // TODO Cannot make the following word work, encoder changes needed
    // (byte) 0xed, (byte) 0xa0, (byte) 0x80,
    // (byte) 0xed, (byte) 0xbc, (byte) 0xb2, (byte) 0xed,
    // (byte) 0xa0, (byte) 0x80, (byte) 0xed, (byte) 0xbc,
    // (byte) 0xb0, (byte) 0xed, (byte) 0xa0, (byte) 0x80,
    // (byte) 0xed, (byte) 0xbd, (byte) 0x85, (byte) 0xed,
    // (byte) 0xa0, (byte) 0x80, (byte) 0xed, (byte) 0xbc,
    // (byte) 0xb0, (byte) 0xed, (byte) 0xa0, (byte) 0x80,
    // (byte) 0xed, (byte) 0xbc, (byte) 0xb9, (byte) 0xed,
    // (byte) 0xa0, (byte) 0x80, (byte) 0xed, (byte) 0xbc,
    // (byte) 0xb8, (byte) 0xed, (byte) 0xa0, (byte) 0x80,
    // (byte) 0xed, (byte) 0xbc, (byte) 0xb9, ' '

    val vendor = new String(bVendor, "UTF-8")
    val spec = new String(bSpec, "UTF-8")
    m.getMainAttributes().put(Attributes.Name.IMPLEMENTATION_VENDOR, vendor)
    m.getAttributes(ATT_ENTRY_NAME)
      .put(Attributes.Name.IMPLEMENTATION_VENDOR, vendor)
    m.getEntries()
      .get(ATT_ENTRY_NAME)
      .put(Attributes.Name.SPECIFICATION_TITLE, spec)

    val baos = new ByteArrayOutputStream()
    m.write(baos)
    m = new Manifest(new ByteArrayInputStream(baos.toByteArray()))

    assertTrue(
      vendor == m
        .getMainAttributes()
        .get(Attributes.Name.IMPLEMENTATION_VENDOR)
    )
    assertTrue(
      vendor == m
        .getEntries()
        .get(ATT_ENTRY_NAME)
        .get(Attributes.Name.IMPLEMENTATION_VENDOR)
    )
    assertTrue(
      spec == m
        .getAttributes(ATT_ENTRY_NAME)
        .get(Attributes.Name.SPECIFICATION_TITLE)
    )

  }

  @Test def read(): Unit = {
    val is = new InputStreamImpl()
    assertThrows(classOf[IOException], new Manifest().read(is))
  }

  private def assertAttribute(
      attr: Attributes,
      name: String,
      value: String
  ): Unit = {
    assertTrue(value == attr.getValue(name))
  }

  private val ATT_ENTRY_NAME = "HasAttributes.txt"

  private val ATT_ATT_NAME = "MyAttribute"

  private def checkManifest(manifest: Manifest): Unit = {
    val main = manifest.getMainAttributes()
    assertAttribute(main, "Bundle-Name", "ClientSupport")
    assertAttribute(
      main,
      "Bundle-Description",
      "Provides SessionService, AuthenticationService. Extends RegistryService."
    )
    assertAttribute(
      main,
      "Bundle-Activator",
      "com.ibm.ive.eccomm.client.support.ClientSupportActivator"
    )
    assertAttribute(
      main,
      "Import-Package",
      "com.ibm.ive.eccomm.client.services.log,com.ibm.ive.eccomm.client.services.registry,com.ibm.ive.eccomm.service.registry; specification-version=1.0.0,com.ibm.ive.eccomm.service.session; specification-version=1.0.0,com.ibm.ive.eccomm.service.framework; specification-version=1.2.0,org.osgi.framework; specification-version=1.0.0,org.osgi.service.log; specification-version=1.0.0,com.ibm.ive.eccomm.flash; specification-version=1.2.0,com.ibm.ive.eccomm.client.xml,com.ibm.ive.eccomm.client.http.common,com.ibm.ive.eccomm.client.http.client"
    )
    assertAttribute(
      main,
      "Import-Service",
      "org.osgi.service.log.LogReaderServiceorg.osgi.service.log.LogService,com.ibm.ive.eccomm.service.registry.RegistryService"
    )
    assertAttribute(
      main,
      "Export-Package",
      "com.ibm.ive.eccomm.client.services.authentication; specification-version=1.0.0,com.ibm.ive.eccomm.service.authentication; specification-version=1.0.0,com.ibm.ive.eccomm.common; specification-version=1.0.0,com.ibm.ive.eccomm.client.services.registry.store; specification-version=1.0.0"
    )
    assertAttribute(
      main,
      "Export-Service",
      "com.ibm.ive.eccomm.service.authentication.AuthenticationService,com.ibm.ive.eccomm.service.session.SessionService"
    )
    assertAttribute(main, "Bundle-Vendor", "IBM")
    assertAttribute(main, "Bundle-Version", "1.2.0")
  }

  private val MANIFEST_CONTENTS =
    "Manifest-Version: 1.0\nBundle-Name: ClientSupport\nBundle-Description: Provides SessionService, AuthenticationService. Extends RegistryService.\nBundle-Activator: com.ibm.ive.eccomm.client.support.ClientSupportActivator\nImport-Package: com.ibm.ive.eccomm.client.services.log,\n com.ibm.ive.eccomm.client.services.registry,\n com.ibm.ive.eccomm.service.registry; specification-version=1.0.0,\n com.ibm.ive.eccomm.service.session; specification-version=1.0.0,\n com.ibm.ive.eccomm.service.framework; specification-version=1.2.0,\n org.osgi.framework; specification-version=1.0.0,\n org.osgi.service.log; specification-version=1.0.0,\n com.ibm.ive.eccomm.flash; specification-version=1.2.0,\n com.ibm.ive.eccomm.client.xml,\n com.ibm.ive.eccomm.client.http.common,\n com.ibm.ive.eccomm.client.http.client\nImport-Service: org.osgi.service.log.LogReaderService\n org.osgi.service.log.LogService,\n com.ibm.ive.eccomm.service.registry.RegistryService\nExport-Package: com.ibm.ive.eccomm.client.services.authentication; specification-version=1.0.0,\n com.ibm.ive.eccomm.service.authentication; specification-version=1.0.0,\n com.ibm.ive.eccomm.common; specification-version=1.0.0,\n com.ibm.ive.eccomm.client.services.registry.store; specification-version=1.0.0\nExport-Service: com.ibm.ive.eccomm.service.authentication.AuthenticationService,\n com.ibm.ive.eccomm.service.session.SessionService\nBundle-Vendor: IBM\nBundle-Version: 1.2.0\n";

  private val MANIFEST_CONTENTS_1 =
    "Manifest-Version: 2.0\nBundle-Name: ClientSupport\nBundle-Description: Provides SessionService, AuthenticationService. Extends RegistryService.\nBundle-Activator: com.ibm.ive.eccomm.client.support.ClientSupportActivator\nImport-Package: com.ibm.ive.eccomm.client.services.log,\n com.ibm.ive.eccomm.client.services.registry,\n com.ibm.ive.eccomm.service.registry; specification-version=2.0.0,\n com.ibm.ive.eccomm.service.session; specification-version=2.0.0,\n com.ibm.ive.eccomm.service.framework; specification-version=2.1.0,\n org.osgi.framework; specification-version=2.0.0,\n org.osgi.service.log; specification-version=2.0.0,\n com.ibm.ive.eccomm.flash; specification-version=2.2.0,\n com.ibm.ive.eccomm.client.xml,\n com.ibm.ive.eccomm.client.http.common,\n com.ibm.ive.eccomm.client.http.client\nImport-Service: org.osgi.service.log.LogReaderService\n org.osgi.service.log.LogService,\n com.ibm.ive.eccomm.service.registry.RegistryService\nExport-Package: com.ibm.ive.eccomm.client.services.authentication; specification-version=1.0.0,\n com.ibm.ive.eccomm.service.authentication; specification-version=1.0.0,\n com.ibm.ive.eccomm.common; specification-version=1.0.0,\n com.ibm.ive.eccomm.client.services.registry.store; specification-version=1.0.0\nExport-Service: com.ibm.ive.eccomm.service.authentication.AuthenticationService,\n com.ibm.ive.eccomm.service.session.SessionService\nBundle-Vendor: IBM\nBundle-Version: 1.2.0\n";

  private val MANIFEST_CONTENTS_2 =
    "Manifest-Version: 1.0\nName: value\n \n" // Note penultimate line is single space
  private def getManifest(bytes: Array[Byte]): Manifest = {
    val jarFile = getJarFile(bytes)
    val manifest = jarFile.getManifest()
    jarFile.close()
    manifest
  }

  private class InputStreamImpl extends InputStream {
    override def read(): Int = 0
  }

}
