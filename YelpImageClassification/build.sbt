name := "YelpImageClassification"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "1.6.0" % "provided",
  "org.nd4j" % "nd4j-x86" % "0.4-rc3.8",
  "org.nd4j" % "nd4s_2.11" % "0.4-rc3.8",
  "org.deeplearning4j" % "deeplearning4j-core" % "0.4-rc3.8",
  "org.imgscalr" % "imgscalr-lib" % "4.2",
  "com.sksamuel.scrimage" %% "scrimage-core" % "2.1.0",
  "com.sksamuel.scrimage" %% "scrimage-io-extra" % "2.1.0",
  "com.sksamuel.scrimage" %% "scrimage-filters" % "2.1.0"
)