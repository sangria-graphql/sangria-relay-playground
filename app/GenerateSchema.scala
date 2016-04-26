import java.io.PrintWriter

import models.SchemaDefinition
import models.StarWarsData.FactionRepo
import play.api.libs.json.Json
import sangria.execution.Executor
import sangria.introspection._
import sangria.marshalling.playJson._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}


object GenerateSchema {
  def main(args: Array[String]) {
    val futureOfSchemaJson = Executor.execute(SchemaDefinition.schema, introspectionQuery, userContext = new FactionRepo)

    futureOfSchemaJson onComplete {
      case Success(schemaJson) => {
        new PrintWriter("schema.json"){
          write(Json.prettyPrint(schemaJson))
          close()
        }
      }
      case Failure(t) => println("Could not generate schema.json : " + t.getMessage)
    }
  }
}
