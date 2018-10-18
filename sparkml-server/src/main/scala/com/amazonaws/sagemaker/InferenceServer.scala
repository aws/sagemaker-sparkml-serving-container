package com.amazonaws.sagemaker

import java.io.File

import ml.combust.bundle.BundleFile
import ml.combust.mleap.runtime.MleapSupport._
import ml.combust.mleap.runtime.frame.Transformer
import ml.combust.mleap.runtime.serialization.{BuiltinFormats, FrameReader}
import org.json4s.{DefaultFormats, Formats}
import org.scalatra._
import org.scalatra.json._
import resource._

class InferenceServer extends ScalatraServlet with JacksonJsonSupport {
  protected implicit val jsonFormats: Formats = DefaultFormats

  before() {
    contentType = formats("json")
  }

  get("/ping") {
    Ok()
  }

  post("/invocations") {
    Ok(predict(request.body), Map("content-type" -> "text/csv"))
  }


  error {
    case _: java.nio.file.NoSuchFileException => "Model not found."
    case e: spray.json.JsonParser.ParsingException => "JSON Parsing Exception : " + e.toString
    case e => "Unexpected Exception : " + e.toString
  }

  val model_path = "/opt/ml/model"


  def init_model(): Transformer = {
    val mleapTransformer = (for (bf <- managed(BundleFile(new File(model_path)))) yield {
      bf.loadMleapBundle().get.root
    }).tried.get

    mleapTransformer
  }

  def predict(input: String): String = {
    val frame = FrameReader(BuiltinFormats.json).fromBytes(input.getBytes).get
    val frame2 = mleapTransformer.transform(frame).get
    val frame3 = frame2.select("features").get
    //println(frame3.dataset.head)
    frame3.dataset.head.getTensor(0).toArray.mkString(",")
    //frame3.dataset.head.get(0).toString
  }

  lazy val mleapTransformer: Transformer = init_model()

}