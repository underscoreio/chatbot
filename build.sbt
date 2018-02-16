val Http4sVersion = "0.18.0"
val LogbackVersion = "1.2.3"
val MinitestVersion = "2.0.0"

lazy val root = (project in file("."))
  .settings(
    organization := "io.underscore",
    name := "chatbot",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.12.4",
    libraryDependencies ++= Seq(
      "org.http4s"      %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s"      %% "http4s-circe"        % Http4sVersion,
      "org.http4s"      %% "http4s-dsl"          % Http4sVersion,
      "ch.qos.logback"  %  "logback-classic"     % LogbackVersion,
      "io.monix"        %% "minitest"            % MinitestVersion % "test",
      "io.monix"        %% "minitest-laws"       % MinitestVersion % "test"
    ),
    fork in run := true
  )

