package v1.order

import java.time._
import java.util.UUID

import akka.actor.ActorSystem
import javax.inject.{Inject, Singleton}
import play.api.libs.concurrent.CustomExecutionContext
import play.api.{Logger, MarkerContext}

import scala.collection.mutable.ListBuffer
import scala.concurrent.Future

final case class OrderData(orderId: String = UUID.randomUUID().toString, memberId: String,
                           OTP: Int, totalAmount: Int, platformId: String,
                           created_at: Long = Instant.now().getEpochSecond)

class OrderId private (val underlying: Int) extends AnyVal {
  override def toString: String = underlying.toString
}

object OrderId {
  def apply(raw: String): OrderId = {
    require(raw != null)
    new OrderId(Integer.parseInt(raw))
  }
}

class OrderExecutionContext @Inject()(actorSystem: ActorSystem)
    extends CustomExecutionContext(actorSystem, "repository.dispatcher")

/**
  * A pure non-blocking interface for the OrderRepository.
  */
trait OrderRepository {
  def create(data: OrderData)(implicit mc: MarkerContext): Future[String]

  def list(platformId: Option[String], start: Option[Long], end: Option[Long])(
    implicit mc: MarkerContext): Future[Iterable[OrderData]]

  def get(id: String)(implicit mc: MarkerContext): Future[Option[OrderData]]
}

/**
  * A simple implementation for the Order Repository.
  *
  * A custom execution context is used here to establish that blocking operations should be
  * executed in a different thread than Play's ExecutionContext, which is used for CPU bound tasks
  * such as rendering.
  */
@Singleton
class OrderRepositoryImpl @Inject()()(implicit ec: OrderExecutionContext)
    extends OrderRepository {

  private val logger = Logger(this.getClass)

  private val orderList = ListBuffer(
    OrderData(memberId = "member1", OTP = 123, totalAmount = 10000, platformId = "web"),
    OrderData(memberId = "member2", OTP = 345, totalAmount = 10000, platformId = "ios"),
    OrderData(memberId = "member1", OTP = 456, totalAmount = 10000, platformId = "android"),
    OrderData(memberId = "member3", OTP = 789, totalAmount = 10000, platformId = "pos"),
    OrderData(memberId = "member2", OTP = 234, totalAmount = 10000, platformId = "web")
  )

  override def list(platformId: Option[String], start: Option[Long], end: Option[Long])(
      implicit mc: MarkerContext): Future[Iterable[OrderData]] = {
    Future {
      logger.trace(s"list: ")
      orderList.filter(order => platformId.forall(order.platformId == _))
        .filter(order => start.forall(order.created_at >= _))
        .filter(order => end.forall(order.created_at <= _))
    }
  }

  override def get(id: String)(
      implicit mc: MarkerContext): Future[Option[OrderData]] = {
    Future {
      logger.trace(s"get: id = $id")
      orderList.find(order => order.orderId == id)
    }
  }

  def create(data: OrderData)(implicit mc: MarkerContext): Future[String] = {
    Future {
      logger.trace(s"create: data = $data")
      data.orderId
    }
  }

}
