lazy val commonSettings = Seq(
  organization := "com.michaelpollmeier",
  scalaVersion := "2.12.3",
  crossScalaVersions := Seq("2.11.11"), //prefix sbt command with `+` to run it with these scala versions
  libraryDependencies ++= Seq(
    "org.scalameta" %% "scalameta" % "1.8.0",
    "org.scalatest" %% "scalatest" % "3.0.3" % Test,
    "com.chuusai" %% "shapeless" % "2.3.2" % Test
  ),
  addCompilerPlugin("org.scalameta" % "paradise" % "3.0.0-M10" cross CrossVersion.full),
  scalacOptions ++= Seq("-Xplugin-require:macroparadise", "-Xlint", "-deprecation", "-feature"),
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
