package v1.order

import java.util.UUID

import javax.inject.{Inject, Provider}
import play.api.MarkerContext
import play.api.libs.json._

import scala.concurrent.{ExecutionContext, Future}

/**
  * DTO for displaying order information.
  */
case class OrderResource(orderId: String, memberId: String, totalAmount: Int,
                         platformId: String, link: String, created_at: String)

object OrderResource {
  /**
    * Mapping to read/write a OrderResource out as a JSON value.
    */
  implicit val format: Format[OrderResource] = Json.format
}


/**
  * Controls access to the backend data, returning [[OrderResource]]
  */
class OrderResourceHandler @Inject()(
    routerProvider: Provider[OrderRouter],
    orderRepository: OrderRepository)(implicit ec: ExecutionContext) {

  def create(orderInput: OrderFormInput)(
    implicit mc: MarkerContext): Future[OrderResource] = {
    val data = OrderData(memberId = orderInput.memberId, totalAmount = orderInput.totalAmount,
      OTP = 234, platformId = orderInput.platformId)
    // We don't actually create the order, so return what we have
    orderRepository.create(data).map { id =>
      createOrderResource(data)
    }
  }

  def lookup(id: String)(
    implicit mc: MarkerContext): Future[Option[OrderResource]] = {
    val orderFuture = orderRepository.get(id)
    orderFuture.map { maybeOrderData =>
      maybeOrderData.map { orderData =>
        createOrderResource(orderData)
      }
    }
  }

  def find(platformId: Option[String], start: Option[Long], end: Option[Long])(
    implicit mc: MarkerContext): Future[Iterable[OrderResource]] = {
    orderRepository.list(platformId, start, end).map { orderDataList =>
      orderDataList.map(orderData => createOrderResource(orderData))
    }
  }

  private def createOrderResource(p: OrderData): OrderResource = {
    OrderResource(orderId = p.orderId, memberId = p.memberId,
      totalAmount = p.totalAmount, platformId = p.platformId,
      link = routerProvider.get().link(p.orderId),
      created_at = p.created_at.toString
    )
  }

}
