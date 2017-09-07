package java.security

import java.util.Date
import java.security.cert.CertPath

final class Timestamp(timestamp: Date, signerCertPath: CertPath)
    extends Serializable
