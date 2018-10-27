package com.amazonaws.sagemaker

import ml.combust.mleap.runtime.frame.Row

case class SageMakerFormat(columnName: Seq[String], columnDataType: Seq[String], columnVal: Seq[Row], outputColumnName: String, outputColumnDataType: String)

object SageMakerFormat {
  val json = "com.amazonaws.sagemaker"
}