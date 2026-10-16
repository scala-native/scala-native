enablePlugins(ScalaNativePlugin)

scalaVersion := sys.props("scala.version")

nativeConfig ~= (_.withMultithreading(true)
  .withMode(scala.scalanative.build.Mode.releaseFast)
  .withTrapBasedGCYieldPoints(false))
