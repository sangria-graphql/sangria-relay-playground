import java.io.PrintWriter

import models.SchemaDefinition
import models.StarWarsData.FactionRepo
import play.api.libs.json.Json
import sangria.execution.Executor
import sangria.introspection._
import sangria.marshalling.playJson._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}


object GenerateSchema {
  def main(args: Array[String]): Unit = {
    val futureOfSchemaJson = Executor.execute(SchemaDefinition.schema, introspectionQuery, userContext = new FactionRepo)

    val schemaJson = Await.ready(futureOfSchemaJson, 5.second).value.get

    schemaJson match {
      case Success(t) => {
        new PrintWriter("schema.json"){
          write(Json.prettyPrint(schemaJson.get))
          close()
        }
      }
      case Failure(t) => println("Could not generate schema.json : " + t.getMessage)
    }
  }
}
