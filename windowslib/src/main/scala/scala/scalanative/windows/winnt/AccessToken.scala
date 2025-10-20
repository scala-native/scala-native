package scala.scalanative.windows.winnt

import scala.scalanative.unsafe.*

@extern
object AccessToken {

  /** Required to change the default owner, primary group, or DACL of an access
   *  token.
   */
  @name("scalanative_token_adjust_default")
  def TOKEN_ADJUST_DEFAULT: AccessToken = extern

  /** Required to adjust the attributes of the groups in an access token. */
  @name("scalanative_token_adjust_groups")
  def TOKEN_ADJUST_GROUP: AccessToken = extern

  /** Required to enable or disable the privileges in an access token. */
  @name("scalanative_token_adjust_privileges")
  def TOKEN_ADJUST_PRIVILEGES: AccessToken = extern

  /** Required to adjust the session ID of an access token. The SE_TCB_NAME
   *  privilege is required.
   */
  @name("scalanative_token_adjust_sessionid")
  def TOKEN_ADJUST_SESSIONID: AccessToken = extern

  /** Required to attach a primary token to a process. The
   *  SE_ASSIGNPRIMARYTOKEN_NAME privilege is also required to accomplish this
   *  task.
   */
  @name("scalanative_token_assign_primary")
  def TOKEN_ASSIGN_PRIMARY: AccessToken = extern

  /** Required to duplicate an access token. */
  @name("scalanative_token_duplicate")
  def TOKEN_DUPLICATE: AccessToken = extern

  /** Combines STANDARD_RIGHTS_EXECUTE and TOKEN_IMPERSONATE. */
  @name("scalanative_token_execute")
  def TOKEN_EXECUTE: AccessToken = extern

  /** Required to attach an impersonation access token to a process. */
  @name("scalanative_token_impersonate")
  def TOKEN_IMPERSONATE: AccessToken = extern

  /** Required to query an access token. */
  @name("scalanative_token_query")
  def TOKEN_QUERY: AccessToken = extern

  /** Required to query the source of an access token. */
  @name("scalanative_token_query_source")
  def TOKEN_QUERY_SOURCE: AccessToken = extern

  /** Combines STANDARD_RIGHTS_READ and TOKEN_QUERY. */
  @name("scalanative_token_read")
  def TOKEN_READ: AccessToken = extern

  /** Combines STANDARD_RIGHTS_WRITE, TOKEN_ADJUST_PRIVILEGES,
   *  TOKEN_ADJUST_GROUPS, and TOKEN_ADJUST_DEFAULT.
   */
  @name("scalanative_token_write")
  def TOKEN_WRITE: AccessToken = extern

  /** Combines all possible access rights for a token. */
  @name("scalanative_token_all_access")
  def TOKEN_ALL_ACCESS: AccessToken = extern
}
