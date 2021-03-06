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

import scala.concurrent.Future
import scala.util.{Failure, Success}

class Application @Inject() (system: ActorSystem, config: Configuration) extends InjectedController {
  import system.dispatcher
  
  val googleAnalyticsCode: Option[String] = config.getOptional[String]("gaCode")
  val defaultGraphQLUrl: String = config.getOptional[String]("defaultGraphQLUrl").getOrElse(s"http://localhost:${config.getOptional[Int]("http.port").getOrElse(9000)}/graphql")

  def index: Action[AnyContent] = Action {
    Ok(views.html.playground(googleAnalyticsCode))
  }

  def playground: Action[AnyContent] = Action {
    Ok(views.html.playground(googleAnalyticsCode))
  }

  def graphql(query: String, variables: Option[String], operation: Option[String]): Action[AnyContent] =
    Action.async(executeQuery(query, variables map parseVariables, operation))

  def graphqlBody: Action[JsValue] = Action.async(parse.json) { request =>
    val query = (request.body \ "query").as[String]
    val operation = (request.body \ "operationName").asOpt[String]

    val variables = (request.body \ "variables").toOption.flatMap {
      case JsString(vars) => Some(parseVariables(vars))
      case obj: JsObject => Some(obj)
      case _ => None
    }

    executeQuery(query, variables, operation)
  }

  private def parseVariables(variables: String): JsObject =
    if (variables.trim == "") Json.obj() else Json.parse(variables).as[JsObject]

  private def executeQuery(query: String, variables: Option[JsObject], operation: Option[String]): Future[Result] =
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

  def renderSchema: Action[AnyContent] = Action {
    Ok(SchemaRenderer.renderSchema(SchemaDefinition.schema))
  }
}
