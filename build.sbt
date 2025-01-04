lazy val root = (project in file("."))
  .settings(
    name := "hands-on-scala",
    scalaVersion := "3.3.4",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.2.15" % "test",
      "com.github.pureconfig" %% "pureconfig-core" % "0.17.4"
    )
  )
