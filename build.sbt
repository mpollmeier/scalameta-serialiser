val defaultScalaV = "2.12.1"

lazy val commonSettings = Seq(
  organization := "com.michaelpollmeier",
  scalaVersion := defaultScalaV,
  crossScalaVersions := Seq("2.11.8", defaultScalaV), //prefix sbt command with `+` to run it with these scala versions
  libraryDependencies ++= Seq(
    "org.scalameta" %% "scalameta" % "1.7.0",
    "org.scalatest" %% "scalatest" % "3.0.1" % Test,
    "com.chuusai" %% "shapeless" % "2.3.2" % Test
  ),
  // A dependency on macro paradise 3.x is required to both write and expand
  // new-style macros.  This is similar to how it works for old-style macro
  // annotations and a dependency on macro paradise 2.x.
  addCompilerPlugin("org.scalameta" % "paradise" % "3.0.0-M8" cross CrossVersion.full),
  scalacOptions ++= Seq("-Xlint", "-deprecation", "-feature"),
  licenses +=("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html")),
  homepage := Some(url("https://github.com/mpollmeier/scalameta-serialiser"))
)

lazy val `scalameta-serialiser` = project.in(file("."))
  .settings(commonSettings: _*)

lazy val examples = project.in(file("examples"))
  .settings(commonSettings: _*)
  .dependsOn(`scalameta-serialiser`)

releaseCrossBuild := true
releasePublishArtifactsAction := PgpKeys.publishSigned.value
publishTo := {
  val sonatype = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at sonatype + "content/repositories/snapshots")
  else
    Some("releases" at sonatype + "service/local/staging/deploy/maven2")
}
pomExtra :=
  <scm>
    <url>git@github.com:mpollmeier/scalameta-serialiser.git</url>
    <connection>scm:git:git@github.com:mpollmeier/scalameta-serialiser.git</connection>
  </scm>
  <developers>
    <developer>
      <id>mpollmeier</id>
      <name>Michael Pollmeier</name>
      <url>http://www.michaelpollmeier.com</url>
    </developer>
  </developers>
