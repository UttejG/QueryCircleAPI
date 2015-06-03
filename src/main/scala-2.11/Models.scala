import play.api.libs.json._

object Models {

  implicit val recentBuildNumbersReads: Reads[RecentBuildNumbers] = Json.reads[RecentBuildNumbers]
  implicit val actionReads: Reads[Action] = Json.reads[Action]
  implicit val stepsReads: Reads[Step] = Json.reads[Step]
  implicit val singleBuildReads: Reads[SingleBuildInfo] = Json.reads[SingleBuildInfo]

  case class RecentBuildNumbers(build_num : BigDecimal)

  case class SingleBuildInfo(build_num: Int, steps: Seq[Step])

  case class Step(name : String, actions : Seq[Action])

  case class Action
  (
    name : String,
    index : Int,
    status : String
    )

}
