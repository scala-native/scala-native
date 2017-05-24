package java.net

final class URI(scheme: String,
                userInfo: String,
                host: String,
                port: Int,
                path: String,
                query: String,
                fragment: String)
    extends Serializable {

  def getFragment(): String =
    fragment

  def getHost(): String =
    host

  def getPath(): String =
    path

  def getPort(): Int =
    port

  def getQuery(): String =
    query

  def getScheme(): String =
    scheme

  def getUserInfo(): String =
    userInfo

}
