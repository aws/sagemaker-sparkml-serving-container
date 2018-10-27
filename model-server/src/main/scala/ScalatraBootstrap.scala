import com.amazonaws.sagemaker._
import org.scalatra._
import javax.servlet.ServletContext

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext): Unit = {
    context.mount(new InferenceServer, "/*")
  }
}