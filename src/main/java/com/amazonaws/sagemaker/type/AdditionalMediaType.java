/*
 *  Copyright 2010-2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License").
 *  You may not use this file except in compliance with the License.
 *  A copy of the License is located at
 *
 *      http://aws.amazon.com/apache2.0
 *
 *  or in the "license" file accompanying this file. This file is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 *
 */

package com.amazonaws.sagemaker.type;

/**
 * This class contains MIME types which are not part of Spring officially provided MIME types
 */
public final class AdditionalMediaType {

    public static final String TEXT_CSV_VALUE = "text/csv";
    public static final String APPLICATION_JSONLINES_VALUE = "application/jsonlines";
    public static final String APPLICATION_JSONLINES_VALUE_MULTIPLE ="application/jsonlines;data=multiline";
    public static final String APPLICATION_JSONLINES_TEXT_VALUE = "application/jsonlines;data=text";

}
