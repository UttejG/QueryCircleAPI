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

  def requestBuilder(url: String) =
    new ning.NingWSClient(new com.ning.http.client.AsyncHttpClientConfig.Builder().build())
      .url(url)
      .withHeaders("Accept" -> "application/json")
      .withQueryString("circle-token" -> s"$circleToken")

  def QueryAPI[T](url: String, queryString: Option[(String, String)])(successBlock: T => Unit)(implicit reads: Reads[T]) = {
    val response = queryString match {
      case Some(queryStringParameter) => requestBuilder(url).withQueryString(queryStringParameter)
      case None => requestBuilder(url)
    }

    response.get() onComplete {
      case Success(successResponse) =>
        Json.fromJson[T](successResponse.json) match {
          case JsSuccess(successResponse, _) => successBlock(successResponse)
          case jsError: JsError => println(jsError)
        }
      case Failure(error) =>
        println("An error occurred with message : " + error.getMessage)
    }
  }

  if(circleToken.isEmpty || projectRecentBuildsUrl == baseUrl || singleBuildUrl == baseUrl) {
    println("Program exited for one of the following reasons :- ")
    println("  1) No valid Circle auth token!")
    println("  2) No api endpoint paths provided!")
    System.exit(1)
  }

  QueryAPI[Seq[RecentBuildNumbers]] (projectRecentBuildsUrl, Some("filter" -> "failed")) {
    (buildNumbers: Seq[RecentBuildNumbers]) => buildNumbers.foreach(buildNumber =>
      QueryAPI[SingleBuildInfo](singleBuildUrl + buildNumber.build_num, None) {
        (buildInfo: SingleBuildInfo) => {
          val buildNumberPrintString = s"Build Number : ${buildNumber.build_num}"
          val failedSteps: Seq[String] = buildInfo.steps
            .flatMap(_.actions.filter(_.status != "success"))
            .map(step => s"Failure in container ${step.index} @ step -> ${step.name}")

          // Printing the info onto console
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
