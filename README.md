Sagemaker SparkML Serving Container
===================================

SageMaker SparkML Serving Container lets you deploy an Apache Spark ML Pipeline in [Amazon SageMaker](https://aws.amazon.com/sagemaker/) for real-time, batch prediction and inference pipeline use-cases. The container can be used to deploy a Spark ML Pipeline outside of SageMaker as well. It is powered by open-source [MLeap](https://github.com/combust/mleap) library.

Build Status
============

| Image Build Status                         | PR Build Status                         |
|--------------------------------------------|-----------------------------------------|
| ![CodeBuild](https://tinyurl.com/y8wdoq3h) | ![Travis](https://tinyurl.com/y79gh9of) |

Overview
========

[Apache Spark](http://spark.apache.org/) is a unified analytics engine for large scale data processing. Apache Spark comes with a Machine Learning library called [MLlib](http://spark.apache.org/mllib/) which lets you build ML pipelines using most of the standard feature transformers & algorithms. Apache Spark is well suited for batch processing use-cases and is not the preferred solution for low latency online inference scenarios. In order to perform low latency online prediction, SageMaker SparkML Serving Container leverages an open source library called MLeap.

MLeap is focussed towards deploying Apache Spark based ML pipelines to production for low latency online inference use-cases. It provides a serialization format for exporting a Spark ML pipeline and a runtime engine to execute prediction against the serialized pipeline. SageMaker SparkML Serving provides a RESTful web service using [Spring Boot](https://spring.io/projects/spring-boot) which internally calls MLeap runtime engine for execution.

SageMaker SparkML Serving Container is primarily built on the underlying Spring Boot based web service and it provides a layer to build a SageMaker compatible Docker image. In addition to using it in SageMaker, you can build the Dockerfile or download SageMaker provided Docker images to perform inference against an MLeap serialized Spark ML Pipeline locally or outside of SageMaker.  

Supported Spark/MLeap version
=============================

Currently SageMaker SparkML Serving is powered by MLeap 0.9.6 and it is tested with Spark major version - 2.2.

Table of Contents
=================

* How to use
* Using the Docker image for performing inference with SageMaker
* Using the Docker image for performing inference locally

How to use
==========

SageMaker SparkML Serving Container takes a code-free approach for performing inference. You need to pass a schema specifying the structure of input columns and output column. The web server will return you the contents of the output column in a specific format depending on `content-type` and `Accept`.

Procedure to pass the schema
----------------------------

There are two ways to pass the input schema to the serving container. You can either pass it as an environment variable or pass the schema with every request. In case there is a schema passed via the environment variable as well as it is passed via request, the one in the request will be considered. This functionality is provided to enable you the capability to override the default schema passed through an environment variable for certain specific requests.

In order to pass the schema via an environment variable, it should be passed with the key : `SAGEMAKER_SPARKML_SCHEMA`.

### Format of the schema

The schema should be passed in the following format:

```
{
  "input": [
    {
      "name": "name_1",
      "type": "int"
    },
    {
      "name": "name_2",
      "type": "string"
    },
    {
      "name": "name_3",
      "type": "double"
    }
  ],
  "output": {
    "name": "prediction",
    "type": "double"
  }
}
```

The `input` field takes a list of mappings and `output` field is a single mapping. Each mapping in the `input` field corresponds to one column in the `Dataframe` that was serialized with MLeap as part of the Spark job. `output` is required for you to specify the output column that you want in response after the `Dataframe` is transformed. If you have built an ML pipeline with a training algorithm at the end (e.g. Random Forest), most likely you'd be interested in the column `prediction`. The column name passed here (via the key `name`) should be exactly same as the name of the columns in the `Dataframe`. You can query for any field that was present in the `Dataframe` which was serialized with MLeap via the `output` field.

Supported data types and data structures
----------------------------------------
SageMaker SparkML Serving Container supports most of the primitive data types to be the `type` field in `input` and `output`. `type` can be: `boolean`, `byte`, `short`, `int`, `long`, `double`, `float` and `string`. 

Each column can have data structures of three types: a single value (`basic`), a Spark `DenseVector` (`vector`) and a Spark `Array` (`array`). This means each column can be a single `int` (or any of the aforementioned data types) or an `Array` of `int` or a `DenseVector` of `int`.
If a column is of type `basic`, then you do not need to pass any additional information. Otherwise, if one or more columns in `input` or `output` is of the type `vector` or `array`, then you need to pass the information with a new key `struct` like this:

```
{
  "input": [
    {
      "name": "name_1",
      "type": "int",
      "struct": "vector"
    },
    {
      "name": "name_2",
      "type": "string",
      "type": "basic"  # This line is optional
    },
    {
      "name": "name_3",
      "type": "double",
      "struct": "array"
    }
  ],
  "output": {
    "name": "features",
    "type": "double",
    "struct": "vector"
  }
}
```


Request Structure
-----------------
SageMaker SparkML Serving Container can parse requests in both `text/csv` and `application/json` format. In case the schema is passed via an environment variable, the request should just contain the payload unless you want to override the schema for a specific request.

### CSV
For CSV, the request should be passed with `content-type` as `text/csv` and schema should be passed via environment variable. In case of CSV input, each input column is treated as `basic` type because you can not have nested data structures in CSV. If your input payload contains one or more columns with `struct` as `vector` or `array`, you have to pass the request payload using JSON.

Sample CSV request:

```
feature_1,feature_2,feature_3
```

String values do not need to be passed with quotes around it. There should not be any space around the comma and the order of the field should match one-to-one with the `input` field of the schema.

### JSON
For JSON, the request should be passed with `content-type` as `application/json`. The schema can be passed either via an environment variable or as part of the payload.

#### Schema is passed via environment variable
If schema is passed via an environment variable, then the input should be formatted like this:

```

# If individual columns are basic type
"data": [feature_1, "feature_2", feature_3]

# If one or more individual columns is vector or array
"data": [[feature_11, feature_12], "feature_2", [feature_31, feature_32]]
```

As with standard JSON, string input values has to be encoded with quotes.

#### Schema is passed as part of the request

For JSON input, the schema can be passed as part of the input payload as well. All the other rules apply for this as well i.e. if a column is `basic`, then you do not need to pass the `struct` field in the mapping for that column. For this, a sample request would look like the following:

```
{
  "schema": {
    "input": [
      {
        "name": "name_1",
        "type": "int",
        "struct": "vector"
      },
      {
        "name": "name_2",
        "type": "string"
      },
      {
        "name": "name_3",
        "type": "double",
        "struct": "array"
      }
    ],
    "output": {
      "name": "features",
      "type": "double",
      "struct": "vector"
    }
  },
  "data": [[feature_11, feature_12, feature_13], "feature_2", [feature_31, feature_32]]
}
```

Output structure
----------------
SageMaker SparkML Serving Container can return output in three formats: CSV (`Accept` should be `text/csv`), JSON (`Accept` should be `application/jsonlines`) and JSON for text data (`Accept` should be `application/jsonlines;data=text`). 
Default output format is CSV (in case there is no `Accept` parameter passed in the HTTP request).

### Sample output

#### CSV

```
out_1,out_2,out_3
```

#### JSON

```
{"features": [out_1, out_2, "out_3"]}
```

#### JSON for text data
This format is expected to be used for output which is text (e.g. `Tokenizer`). The `struct` in output in this case will most likely an `array` or `vector` and it is concatenated with space instead of comma.

```
{"source": "sagemaker sparkml serving"}

or

{"source": "feature_1 feature_2 feature_3"}
```

This container is expected to be used in conjunction with other [SageMaker built-in algorithms](https://docs.aws.amazon.com/sagemaker/latest/dg/algos.html) for inference pipeline and the output formats resemble the structure those algorithms can work seamlessly with.

Example Notebooks
-----------------

You can find examples of how to use this in an end-to-end fashion here: [1](https://github.com/awslabs/amazon-sagemaker-examples/tree/master/sagemaker-python-sdk/sparkml_serving_emr_mleap_abalone), [2](https://github.com/awslabs/amazon-sagemaker-examples/tree/master/advanced_functionality/inference_pipeline_sparkml_xgboost_abalone) and [3](https://github.com/awslabs/amazon-sagemaker-examples/tree/master/advanced_functionality/inference_pipeline_sparkml_blazingtext_dbpedia).

Using the Docker image for performing inference with SageMaker
==============================================================

SageMaker SparkML Serving Container is built to work seamlessly with SageMaker for real time inference, batch transformation and inference pipeline use-cases. 

With AWS SDK
------------
If you are using AWS Java SDK or Boto to call SageMaker APIs, then you can pass the SageMaker provided Docker images for this container in all region as part of the [`CreateModel`](https://docs.aws.amazon.com/sagemaker/latest/dg/API_CreateModel.html) API call in the `PrimaryContainer` or `Containers` field. The schema should be passed using the `Environment` field of the API. As the schema has quotes, it should be encoded properly so that the JSON parser in the server can parse it during inference. For example, if you are using Boto, you can use Python's `json` library to do a `json.dumps` on the `dict` that holds the schema before passing it via Boto.

Calling `CreateModel` is required for creating a `Model` in SageMaker with this Docker container and the serialized pipeline artifacts which is the stepping stone for all the use cases mentioned above.

SageMaker works with Docker images stored in [Amazon ECR](https://aws.amazon.com/ecr/). SageMaker team has prepared and uploaded the Docker images for SageMaker SparkML Serving Container in all regions where SageMaker operates. 
Region to ECR container URL mapping can be found below. For a mapping from Region to Region Name, please see [here](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/Concepts.RegionsAndAvailabilityZones.html).

* us-west-1 = 746614075791.dkr.ecr.us-west-1.amazonaws.com/sagemaker-sparkml-serving:2.2
* us-west-2 = 246618743249.dkr.ecr.us-west-2.amazonaws.com/sagemaker-sparkml-serving:2.2
* us-east-1 = 683313688378.dkr.ecr.us-east-1.amazonaws.com/sagemaker-sparkml-serving:2.2
* us-east-2 = 257758044811.dkr.ecr.us-east-2.amazonaws.com/sagemaker-sparkml-serving:2.2
* ap-northeast-1 = 354813040037.dkr.ecr.ap-northeast-1.amazonaws.com/sagemaker-sparkml-serving:2.2
* ap-northeast-2 = 366743142698.dkr.ecr.ap-northeast-2.amazonaws.com/sagemaker-sparkml-serving:2.2
* ap-southeast-1 = 121021644041.dkr.ecr.ap-southeast-1.amazonaws.com/sagemaker-sparkml-serving:2.2
* ap-southeast-2 = 783357654285.dkr.ecr.ap-southeast-2.amazonaws.com/sagemaker-sparkml-serving:2.2
* ap-south-1 = 720646828776.dkr.ecr.ap-south-1.amazonaws.com/sagemaker-sparkml-serving:2.2
* eu-west-1 = 141502667606.dkr.ecr.eu-west-1.amazonaws.com/sagemaker-sparkml-serving:2.2
* eu-west-2 = 764974769150.dkr.ecr.eu-west-2.amazonaws.com/sagemaker-sparkml-serving:2.2
* eu-central-1 = 492215442770.dkr.ecr.eu-central-1.amazonaws.com/sagemaker-sparkml-serving:2.2
* ca-central-1 = 341280168497.dkr.ecr.ca-central-1.amazonaws.com/sagemaker-sparkml-serving:2.2
* us-gov-west-1 = 414596584902.dkr.ecr.us-gov-west-1.amazonaws.com/sagemaker-sparkml-serving:2.2

With [SageMaker Python SDK](https://github.com/aws/sagemaker-python-sdk)
------------------------------------------------------------------------

If you are using SageMaker Python SDK, you can create an instance of `SparkMLModel` class with only the serialized pipeline artifacts and call `deploy()` method on it to create an inference endpoint or use the model created as part of the method for a batch transformation job.

Using it in an inference pipeline
---------------------------------

For using it as one of the containers in an inference pipeline, you need to pass the container as one of the containers in the `Containers` field if you are using AWS SDK. If you are using SageMaker Python SDK, then you need to pass an instance of `SparkMLModel` as one of the models in the `PipelineModel` instance that you will create. For more information on this, please see the documentation on SageMaker Python SDK.


Using the Docker image for performing inference locally
=======================================================

You can also build and test this container locally or deploy it outside of SageMaker to perform predictions against an MLeap serialized Spark ML Pipeline.

### Installing Docker

First you need to ensure that have installed [Docker](https://www.docker.com/) on your development environment and you have it running with `docker start`.

#### Building the image locally

In order to build the Docker image, you need to run a single Docker command:

```
docker build -t sagemaker-sparkml-serving:2.2 .
```

#### Running the image locally

In order to run the Docker image, you need to run the following command. Please make sure that the serialized model artifact is present in `/tmp/model` or change the location to where it is stored in the following command. 
The command will start the server on port 8080 and will also pass the schema as an environment variable to the Docker container. Alternatively, you can edit the `Dockerfile` to add `ENV SAGEMAKER_SPARKML_SCHEMA=schema` as well before building the Docker image.

```
docker run -p 8080:8080 -e SAGEMAKER_SPARKML_SCHEMA=schema -v /tmp/model:/opt/ml/model sagemaker-sparkml-serving:2.2 serve
```

#### Invoking with a payload

Once the container starts to run, you can invoke it with a payload like this. Remember from our last schema definition that `feature_2` is a string. Note the difference in input for that.

```
curl -i -H "content-type:text/csv" -d "feature_1,feature_2,feature_3" http://localhost:8080/invocations

or 

curl -i -H "content-type:application/json" -d "{\"data\":[feature_1,\"feature_2\",feature_3]}" http://localhost:8080/invocations
```

The `Dockerfile` can be found at the root directory of the package. SageMaker SparkML Serving Container tags the Docker images using the Spark major version it is compatible with. Right now, it only supports Spark 2.2 and as a result, the Docker image is tagged with 2.2. 

In order to save the effort of building the Docker image everytime you are making a code change, you can also install [Maven](http://maven.apache.org/) and run `mvn clean package` at your project root to verify if the code is compiling fine and unit tests are running without any issue. 


Publicly available Docker images from SageMaker
===============================================

If you are not making any changes to the underlying code that powers this Docker container, you can also download one of the already built Docker images from SageMaker provided [Amazon ECR](https://aws.amazon.com/ecr/) repositories.

In order to download the image from the repository in `us-west-2` (US West - Oregon) region:

* Make sure you have [Docker](https://www.docker.com/) installed in your development environment. Start the Docker client.
* Install [AWS CLI](https://aws.amazon.com/cli/).
* Authenticate your Docker client with `aws ecr get-login` with the following command:

```
aws ecr get-login --region us-west-2 --registry-ids 246618743249 --no-include-email
```

* Download the Docker image with the following command:

```
docker pull 246618743249.dkr.ecr.us-west-2.amazonaws.com/sagemaker-sparkml-serving:2.2
```

For running the Docker image, please see the Running the image locally section from above.

For other regions, please see the region to ECR repository mapping provided above and download the image based on the region you are operating.

License
=======
This library is licensed under the Apache 2.0 License.

