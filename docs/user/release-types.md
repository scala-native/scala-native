(release-types)=
# Release types

## Stable

  A 'Stable' release is recommended for general use and is
  tagged in the Repository. The version format is major.minor.patch.
  Stable releases may be updated without a change of version number to
  support versions of Scala released after date of that Scala Native version.

## Latest

   A 'Latest' release is intended for developers who want to 
   exercise the committed Scala Native code at the time of publication. 

   The version has the format major.minor.patch-<date>-<gitref>-SNAPSHOT.
   <date> gives UTC year, month, and day of publication.
   <gitref>, formerly informally known as SHA1,
   gives the Git reference for the youngest commit at the time of publication.

   These releases generally happen early in the (UTC) day where at least one
   commit happened the
   day before. They are short lived and currently are kept available for only
   a few weeks. For complex reasons, there is no easy way to list prior
   SNAPSHOT releases.

Continue to [lib](../lib/communitylib.md)
