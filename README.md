[![Build Status](https://secure.travis-ci.org/mpollmeier/scalameta-serialiser.png?branch=master)](http://travis-ci.org/mpollmeier/scalameta-serialiser)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.michaelpollmeier/scalameta_serialiser_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.michaelpollmeier/scalameta_serialiser_2.11)

# Setup with sbt
Get the latest version of [scalameta-serialiser](https://maven-badges.herokuapp.com/maven-central/com.michaelpollmeier/scalameta_serialiser_2.11) and the [scalameta compiler plugin](https://maven-badges.herokuapp.com/maven-central/org.scalameta/paradise_2.11.8)

```
resolvers += Resolver.bintrayIvyRepo("scalameta", "maven")
libraryDependencies += "com.michaelpollmeier" %% "scalameta_serialiser" % "0.0.4"
addCompilerPlugin("org.scalameta" % "paradise" % "3.0.0.132" cross CrossVersion.full)
```

# Usage

## @mappable

```scala
import scala.meta.serialiser.mappable
@mappable case class MyCaseClass(i: Int)
```

Annotating any case class with `mappable` will generate typeclass instances `FromMap` and `ToMap` that let you serialise and deserialise that specific case class. 

```scala
trait ToMap[A] {
  def apply(a: A): Map[String, Any]
}

trait FromMap[A] {
  def apply(keyValues: Map[String, Any]): Option[A]
}
```

The typeclass instances will end up in the companion object and will automatically be in scope. 
For details have a look at [MappableTest.scala](examples/src/test/scala/scala/meta/serialiser/MappableTest.scala).

## current limitations (a.k.a. TODOs) 
- no support for multiple constructor parameter lists
- [get maps of specific types](https://github.com/mpollmeier/scalameta-serialiser/issues/1)

# sbt command to compile and test this project
;clean;examples/clean;examples/test
