package modules

import java.io.PrintWriter

import com.google.inject.AbstractModule
import models.SchemaDefinition
import models.StarWarsData.FactionRepo
import play.api.Logger
import play.api.libs.json.Json
import sangria.execution.Executor
import sangria.introspection._
import sangria.marshalling.playJson._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.util.{Failure, Success}

trait GenerateSchemaJson {}

class MyGenerateSchemaJsonClass extends GenerateSchemaJson {
  initialize()

  // running initialization in constructor
  def initialize() = {
    val futureOfSchemaJson = Executor.execute(SchemaDefinition.schema, introspectionQuery, userContext = new FactionRepo)
    futureOfSchemaJson onComplete {
      case Success(schemaJson) => {
        Logger.info("Generation of schema.json")
        new PrintWriter("schema.json") {
          write(Json.prettyPrint(schemaJson));
          close()
        }
      }
      case Failure(t) => Logger.warn("Could not generate schema.json : " + t.getMessage)
    }
  }
}

class GenerateSchemaJsonModule extends AbstractModule {
  def configure() = {
    bind(classOf[GenerateSchemaJson])
      .to(classOf[MyGenerateSchemaJsonClass]).asEagerSingleton
  }
}