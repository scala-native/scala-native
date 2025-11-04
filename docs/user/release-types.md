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

   The version format is: "major.minor.patch-\<date\>-\<gitref\>-SNAPSHOT".

   "\<date\>" gives UTC year, month, and day of the youngest commit 
   contained. The version date is usually not the posted date of the 
   rest of the landing page or the invisible date of posting the SNAPSHOT.

   "\<gitref\>", formerly informally known as SHA1, gives the leading, 
   currently 7,  digits of the Git reference to that youngest commit. 
   
   'Latest' releases generally happen early in the (UTC) day where at least
   one commit happened the day before. 
   They are short lived and are kept available for only a limited time;
   currently, but not guaranteed to stay, 3 months. For complex reasons,
   there is no easy way to list prior SNAPSHOT releases.


### Using Latest releases   

   This information is provided as a working example. It  is correct as
   of publication but highly subject
   to unannounced change, particularly sbt and mill details.

   Latest releases are published to:
     "https://central.sonatype.com/repository/maven-snapshots/"

   A project needs to have specified a resolver for that repository.
   See the original sbt and mill documentation for the latest details

   These snippets are intended to be merged into complete example or existing 
   project files and are not stand-alone and sufficient.

   * sbt project/plugins.sbt:

     * projects using sbt version 1.11.0 or later can use

     ```
     resolvers += Resolver.sonatypeCentralSnapshots
     ```

     * earlier sbt versions use (also works for current sbt versions)

     ```
       resolvers +=
         "YourNameHere" at
           "https://central.sonatype.com/repository/maven-snapshots/"

     ```
   * In either case, add the plugin

     ```
       addSbtPlugin("org.scala-native" %% "sbt-scala-native" %
               "0.5.10-20251101-14920c7-SNAPSHOT")
     ```

   * mill (1.0.6) build.mill:
     ```
     import coursier.maven.MavenRepository

     object `<yourPackageHere>` extends ScalaNativeModule {

       // Earlier Mill versions used T.task

       def repositoriesTask = Task.Anon { super.repositoriesTask() ++ Seq(
            MavenRepository(
              "https://central.sonatype.com/repository/maven-snapshots/"
            )
           )
       }
	   
	   def scalaNativeVersion = "0.5.10-20251101-14920c7-SNAPSHOT"
	   
     } 
     ```

   * To determine that the desired SNAPSHOT is being used:
     ```
     $ # Or Mill equivalent
     sbt> show libraryDependencies 
     ...
     [info] * org.scala-native:javalib:0.5.10-20251101-14920c7-SNAPSHOT
     ```
Continue to [lib](../lib/communitylib.md)
