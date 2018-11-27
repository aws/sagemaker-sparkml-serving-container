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

package com.amazonaws.sagemaker.configuration;

import com.amazonaws.sagemaker.configuration.ContextLoaderTest.TestConfig;
import java.io.File;
import ml.combust.mleap.runtime.javadsl.LeapFrameBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
public class ContextLoaderTest {

    @Autowired
    ApplicationContext context;

    @Configuration
    @Import(BeanConfiguration.class) // the actual configuration
    public static class TestConfig {

        @Bean
        public File provideModelFile() {
            return new File(this.getClass().getResource("model").getFile());
        }
    }

    @Test
    public void testApplicationContextSetup() {
        //Checks ApplicationContext is initialized and a random Bean is instantiated properly
        Assert.assertNotNull(context.getBean(LeapFrameBuilder.class));
    }


}
