scalaVersion := "2.11.12"

enablePlugins(ScalaNativePlugin)
nativeLinkStubs := true

libraryDependencies ++= Seq(
  "com.github.scopt" %%% "scopt" % "3.7.0",
  "com.lihaoyi" %%% "fastparse" % "1.0.0",
  "com.lihaoyi" %%% "utest" % "0.6.3" % "test"
)

testFrameworks += new TestFramework("utest.runner.Framework")
