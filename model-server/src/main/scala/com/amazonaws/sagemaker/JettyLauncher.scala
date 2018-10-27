package com.amazonaws.sagemaker

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener

object JettyLauncher {
  def main(args: Array[String]): Unit = {
    val port = if (System.getenv("SAGEMAKER_BIND_TO_PORT") != null) System.getenv("SAGEMAKER_BIND_TO_PORT").toInt else 8080
    println(port)

    val server = new Server(port)
    val context = new WebAppContext()
    context.setContextPath("/")
    context.setResourceBase("src/main/webapp")
    context.addEventListener(new ScalatraListener)
    context.addServlet(classOf[DefaultServlet], "/")

    server.setHandler(context)

    server.start()
    server.join()
  }
}