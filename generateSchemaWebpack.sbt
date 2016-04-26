import play.sbt.PlayImport.PlayKeys._
import sbt.Keys._

lazy val generateSchema = taskKey[Unit]("Generate schema.json file")

fullRunTask(generateSchema, Compile, "GenerateSchema")

lazy val generateSchemaWebpack = taskKey[Unit]("Generate schema.json file and run webpack")

generateSchemaWebpack := {
  generateSchema.value
  webpack.value
}

compile in Runtime <<= compile in Runtime dependsOn generateSchema

playRunHooks <+= baseDirectory.map(Webpack.apply)

lazy val webpack = taskKey[Unit]("Run webpack when packaging the application")

def runWebpack(file: File) = {
  Process("webpack", file) !
}

webpack := {
  if(runWebpack(baseDirectory.value) != 0) throw new Exception("Something goes wrong when running webpack.")
}

dist <<= dist dependsOn webpack

stage <<= stage dependsOn webpack