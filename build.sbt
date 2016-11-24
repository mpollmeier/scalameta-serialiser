lazy val commonSettings = Seq(
  organization := "com.michaelpollmeier.scalameta",
  version := "1.0.0-SNAPSHOT",
  scalaVersion := "2.11.8",
  libraryDependencies ++= Seq(
    "org.scalameta" %% "scalameta" % "1.3.0",
    "org.scalatest" %% "scalatest" % "3.0.1" % "test"
  ),
  addCompilerPlugin("org.scalameta" % "paradise" % "3.0.0-M5" cross CrossVersion.full),
  scalacOptions += "-Xplugin-require:macroparadise"
)

lazy val root = project.in(file("."))
  .settings(commonSettings: _*)
  .aggregate(serialiser, examples)

lazy val examples = project.in(file("examples"))
  .settings(commonSettings: _*)
  .dependsOn(serialiser)

lazy val serialiser = project.in(file("serialiser"))
  .settings(commonSettings: _*)
