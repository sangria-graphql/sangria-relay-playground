import java.net.InetSocketAddress

import play.sbt.PlayRunHook
import sbt._
import scala.sys.process._


object Webpack {
  def apply(base: File): PlayRunHook = {
    object WebpackHook extends PlayRunHook {
      private lazy val ext: String = sys.props.get("os.name").filter(_.toLowerCase.contains("windows")).map(_ => ".cmd").getOrElse("")
      private lazy val webpackCmd: String = s"node_modules/.bin/webpack${ext}"
      
      var process: Option[Process] = None

      override def beforeStarted() = {
        process = Option(
          Process(webpackCmd, base).run()
        )
      }

      override def afterStarted() = {
        process = Option(
          Process(webpackCmd + " --watch", base).run()
        )
      }

      override def afterStopped() = {
        process.foreach(_.destroy())
        process = None
      }

    }

    WebpackHook
  }
}