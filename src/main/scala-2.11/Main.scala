import Models._
import scala.util.{Failure, Success}
import play.api.libs.json._
import play.api.libs.ws._
import scala.concurrent.ExecutionContext.Implicits.global

object Main extends App {

  val baseUrl = "https://circleci.com/api/v1/project/"
  // Add a valid token
  val circleToken = ""
  // This looks something like baseUrl + ":username/:project/tree/:branch"
  val projectRecentBuildsUrl = baseUrl + ""
  // This looks something like baseUrl + ":username/:project/"
  val singleBuildUrl = baseUrl + ""

  def responseBuilder(url: String) =
    new ning.NingWSClient(new com.ning.http.client.AsyncHttpClientConfig.Builder().build())
      .url(url)
      .withHeaders("Accept" -> "application/json")
      .withQueryString("circle-token" -> s"$circleToken")

  def QueryAPI[T](url: String, queryString: Option[(String, String)])(SuccessBlock: T => Unit)(implicit reads: Reads[T]) = {
    val response = queryString match {
      case Some(queryStringParameter) => responseBuilder(url).withQueryString(queryStringParameter)
      case None => responseBuilder(url)
    }

    response.get() onComplete {
      case Success(successResponse) =>
        Json.fromJson[T](successResponse.json) match {
          case JsSuccess(successResponse, _) => SuccessBlock(successResponse)
          case jsError: JsError => println(jsError)
        }
      case Failure(error) =>
        println("An error occurred with message : " + error.getMessage)
    }
  }

  QueryAPI[Seq[RecentBuildNumbers]] (projectRecentBuildsUrl, Some("filter" -> "failed")) {
    (buildNumbers: Seq[RecentBuildNumbers]) => buildNumbers.foreach(buildNumber =>
      QueryAPI[SingleBuildInfo](singleBuildUrl + buildNumber.build_num, None) {
        (buildInfo: SingleBuildInfo) => {
          val buildNumberPrintString = s"Build Number : ${buildNumber.build_num}"
          val failedSteps: Seq[String] = buildInfo.steps
            .flatMap(_.actions.filter(_.status != "success"))
            .map(step => s"Failure in container ${step.index} @ step -> ${step.name}")

          println(buildNumberPrintString)
          for (step <- failedSteps) {
            println(step)
          }
          println("")
        }
      }
    )
  }
}