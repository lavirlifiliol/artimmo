libraryDependencies += "dev.zio" %% "zio" % "2.1.15"
libraryDependencies += "dev.zio" %% "zio-json" % "0.7.29"
libraryDependencies += "io.github.harvardpl" % "AbcDatalog" % "0.7.2"
libraryDependencies += "dev.zio" %% "zio-http" % "3.0.1"

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.6.3"

lazy val root = (project in file("."))
  .settings(
    name := "ArtiMMOClient"
  )
