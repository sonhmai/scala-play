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
                         platformId: String, link: String, created_at: String,
                         verified: Boolean)

object OrderResource {
  /**
    * Mapping to read/write a OrderResource out as a JSON value.
    */
  implicit val format: Format[OrderResource] = Json.format
}

/**
  * DTO for displaying order info after placing successfully
  */
case class OrderPlacedInfo(orderId: String, OTP: Int, link: String)

object OrderPlacedInfo {
  /**
    * Mapping to read/write as a JSON value.
    */
  implicit val format: Format[OrderPlacedInfo] = Json.format
}

/**
  * Controls access to the backend data, returning [[OrderResource]]
  */
class OrderResourceHandler @Inject()(
                                      routerProvider: Provider[OrderRouter],
                                      orderRepository: OrderRepository)(implicit ec: ExecutionContext) {

  def create(orderInput: OrderFormInput)(
    implicit mc: MarkerContext): Future[OrderPlacedInfo] = {
    val data = OrderData(memberId = orderInput.memberId, totalAmount = orderInput.totalAmount,
      OTP = 234, platformId = orderInput.platformId)
    // We don't actually create the order, so return what we have
    orderRepository.create(data).map { id =>
      createOrderPlacedInfo(data)
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

  def verify(orderVerification: OrderVerificationInput)(
    implicit mc: MarkerContext): Future[Option[OrderResource]] = {
    val orderFuture = orderRepository.get(orderVerification.orderId)
    orderFuture.map { maybeOrderData =>
      maybeOrderData.map { orderData =>
        createOrderResource(orderData)
      }
    }
  }

  private def createOrderPlacedInfo(orderData: OrderData): OrderPlacedInfo = {
    OrderPlacedInfo(orderData.orderId, orderData.OTP, routerProvider.get().link(orderData.orderId))
  }

  private def createOrderResource(orderData: OrderData): OrderResource = {
    OrderResource(orderId = orderData.orderId, memberId = orderData.memberId,
      totalAmount = orderData.totalAmount, platformId = orderData.platformId,
      link = routerProvider.get().link(orderData.orderId),
      created_at = orderData.created_at.toString,
      verified = orderData.verified
    )
  }

}
