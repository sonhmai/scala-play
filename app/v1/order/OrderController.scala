package v1.order

import javax.inject.Inject
import play.api.Logger
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

case class OrderFormInput(memberId: String, totalAmount: Int, platformId: String)

case class OrderVerificationInput(orderId: String, OTP: Int)

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

  private val verificationForm: Form[OrderVerificationInput] = {
    import play.api.data.Forms._

    Form(
      mapping(
        "orderId" -> nonEmptyText,
        "OTP" -> number
      )(OrderVerificationInput.apply)(OrderVerificationInput.unapply)
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

  def verify: Action[AnyContent] = OrderAction.async { implicit request =>
    logger.trace("verify: ")
    verifyOrder()
  }

  private def processJsonOrder[A]()(
      implicit request: OrderRequest[A]): Future[Result] = {
    def failure(badForm: Form[OrderFormInput]) = {
      Future.successful(BadRequest(badForm.errorsAsJson))
    }

    def success(input: OrderFormInput) = {
      orderResourceHandler.create(input).map { orderPlacedInfo =>
        Created(Json.toJson(orderPlacedInfo)).withHeaders(LOCATION -> orderPlacedInfo.link)
      }
    }

    form.bindFromRequest().fold(failure, success)
  }

  private def verifyOrder[A]()(
    implicit request: OrderRequest[A]): Future[Result] = {
    def failure(badForm: Form[OrderVerificationInput]) = {
      Future.successful(BadRequest(badForm.errorsAsJson))
    }

    def success(input: OrderVerificationInput) = {
      orderResourceHandler.verify(input).map { orderResource =>
        Created(Json.toJson(orderResource))
      }
    }

    verificationForm.bindFromRequest().fold(failure, success)
  }
}
