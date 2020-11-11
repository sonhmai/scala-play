package v1.order

import javax.inject.Inject
import play.api.Logger
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

case class OrderFormInput(memberId: String, totalAmount: Int, platformId: String)

/**
  * Takes HTTP requests and produces JSON.
  */
class OrderController @Inject()(cc: OrderControllerComponents)(
    implicit ec: ExecutionContext)
    extends OrderBaseController(cc) {

  private val logger = Logger(getClass)

  private val form: Form[OrderFormInput] = {
    import play.api.data.Forms._

    Form(
      mapping(
        "memberId" -> nonEmptyText,
        "totalAmount" -> number,
        "platformId" -> nonEmptyText
      )(OrderFormInput.apply)(OrderFormInput.unapply)
    )
  }

  def index: Action[AnyContent] = OrderAction.async { implicit request =>
    logger.trace("index: ")
    val platformId = request.getQueryString("platformId")
    val start = request.getQueryString("start").map(s => s.toLong)
    val end = request.getQueryString("end").map(s => s.toLong)
    orderResourceHandler.find(platformId, start, end).map { orders =>
      Ok(Json.toJson(orders))
    }
  }

  def process: Action[AnyContent] = OrderAction.async { implicit request =>
    logger.trace("process: ")
    processJsonOrder()
  }

  def show(id: String): Action[AnyContent] = OrderAction.async {
    implicit request =>
      logger.trace(s"show: id = $id")
      orderResourceHandler.lookup(id).map { order =>
        Ok(Json.toJson(order))
      }
  }

  private def processJsonOrder[A]()(
      implicit request: OrderRequest[A]): Future[Result] = {
    def failure(badForm: Form[OrderFormInput]) = {
      Future.successful(BadRequest(badForm.errorsAsJson))
    }

    def success(input: OrderFormInput) = {
      orderResourceHandler.create(input).map { order =>
        Created(Json.toJson(order)).withHeaders(LOCATION -> order.link)
      }
    }

    form.bindFromRequest().fold(failure, success)
  }
}
