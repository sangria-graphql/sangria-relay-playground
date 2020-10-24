package controllers

import javax.inject.Inject

import akka.actor.ActorSystem
import play.api.libs.json._
import play.api.mvc._
import play.api.Configuration

import sangria.execution.{ErrorWithResolver, QueryAnalysisError, Executor}
import sangria.parser.{SyntaxError, QueryParser}
import sangria.marshalling.playJson._

import models.SchemaDefinition
import models.StarWarsData._
import sangria.renderer.SchemaRenderer

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class Application @Inject()(cc: ControllerComponents, config: Configuration)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  val googleAnalyticsCode = config.getOptional[String]("gaCode")
  val defaultGraphQLUrl = config.getOptional[String]("defaultGraphQLUrl").getOrElse(s"http://localhost:${config.getOptional[Int]("http.port").getOrElse(9000)}/graphql")

  def index = Action {
    Ok(views.html.index(googleAnalyticsCode,defaultGraphQLUrl))
  }

  def playground = Action {
    Ok(views.html.playground(googleAnalyticsCode))
  }

  def starwars = Action {
    Ok(views.html.starwars())
  }

  def graphql(query: String, variables: Option[String], operation: Option[String]) =
    Action.async(executeQuery(query, variables map parseVariables, operation))

  def graphqlBody = Action.async(parse.json) { request =>
    val query = (request.body \ "query").as[String]
    val operation = (request.body \ "operationName").asOpt[String]

    val variables = (request.body \ "variables").toOption.flatMap {
      case JsString(vars) => Some(parseVariables(vars))
      case obj: JsObject => Some(obj)
      case _ => None
    }

    executeQuery(query, variables, operation)
  }

  private def parseVariables(variables: String) =
    if (variables.trim == "") Json.obj() else Json.parse(variables).as[JsObject]

  private def executeQuery(query: String, variables: Option[JsObject], operation: Option[String]) =
    QueryParser.parse(query) match {

      // query parsed successfully, time to execute it!
      case Success(queryAst) =>
        Executor.execute(SchemaDefinition.schema, queryAst, new FactionRepo,
            operationName = operation,
            variables = variables getOrElse Json.obj(),
            maxQueryDepth = Some(10))
          .map(Ok(_))
          .recover {
            case error: QueryAnalysisError => BadRequest(error.resolveError)
            case error: ErrorWithResolver => InternalServerError(error.resolveError)
          }

      // can't parse GraphQL query, return error
      case Failure(error: SyntaxError) =>
        Future.successful(BadRequest(Json.obj(
          "syntaxError" -> error.getMessage,
          "locations" -> Json.arr(Json.obj(
            "line" -> error.originalError.position.line,
            "column" -> error.originalError.position.column)))))

      case Failure(error) =>
        throw error
    }

  def renderSchema = Action {
    Ok(SchemaRenderer.renderSchema(SchemaDefinition.schema))
  }
}
