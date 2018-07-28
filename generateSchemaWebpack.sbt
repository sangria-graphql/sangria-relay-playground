import play.sbt.PlayImport.PlayKeys._
import scala.sys.process._

lazy val generateSchema = taskKey[Unit]("Generate schema.json file")

fullRunTask(generateSchema, Compile, "GenerateSchema")

lazy val generateSchemaWebpack = taskKey[Unit]("Generate schema.json file and run webpack")

generateSchemaWebpack := {
  generateSchema.value
  webpack.value
}

compile in Runtime := (compile in Runtime dependsOn generateSchema).value

playRunHooks += { baseDirectory.map(Webpack.apply).value }

lazy val ext = sys.props.get("os.name").filter(_.toLowerCase.contains("windows")).map(_ => ".cmd").getOrElse("")
lazy val webpackCmd = "node_modules/.bin/webpack" + ext

lazy val webpack = taskKey[Unit]("Run webpack when packaging the application")

def runWebpack(file: File) = {
  Process(webpackCmd, file) !
}

webpack := {
  if(runWebpack(baseDirectory.value) != 0) throw new Exception("Something goes wrong when running webpack.")
}

dist := (dist dependsOn webpack).value

stage := (stage dependsOn webpack).value
