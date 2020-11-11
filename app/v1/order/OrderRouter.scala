package v1.order

import javax.inject.Inject
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

/**
  * Routes and URLs to the OrderResource controller.
  */
class OrderRouter @Inject()(controller: OrderController) extends SimpleRouter {
  val prefix = "/v1/orders"

  def link(id: String): String = {
    import io.lemonlabs.uri.dsl._
    val url = prefix / id.toString
    url.toString()
  }

  override def routes: Routes = {
    case GET(p"/") =>
      controller.index

    case POST(p"/") =>
      controller.process

    case POST(p"/verify") =>
      controller.verify

    case GET(p"/$id") =>
      controller.show(id)
  }

}
