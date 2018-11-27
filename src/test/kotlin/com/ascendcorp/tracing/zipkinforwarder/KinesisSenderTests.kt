/*
 *   Copyright 2018 Ascend Corporation (https://www.ascendcorp.com/)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software distributed under the License
 *   is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *   or implied. See the License for the specific language governing permissions and limitations under
 *   the License.
 */

package com.ascendcorp.tracing.zipkinforwarder

import com.ascendcorp.tracing.zipkinforwarder.config.AWSConfiguration
import com.ascendcorp.tracing.zipkinforwarder.config.ForwarderConfiguration
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.support.AnnotationConfigContextLoader

@RunWith(SpringRunner::class)
@SpringBootTest(properties = ["destination.type=aws-kinesis"])
@ContextConfiguration(
    classes = [ForwarderConfiguration::class, AWSConfiguration::class, SpanTransporter::class],
    loader = AnnotationConfigContextLoader::class)
class KinesisSenderTests {

  @Autowired
  lateinit var context: ApplicationContext

  @Autowired
  lateinit var forwarder: SpanTransporter

  @Test
  fun `AWSConfiguration is loaded when required property is set`() {
    assertThat(context.containsBean("kinesisSender")).isTrue()
  }

  @Test
  fun `Sends trace to Kinesis with no enpoints`() {
    forwarder.forward(TestObjects.TRACE)
    assertThat(forwarder.kinesisSender.check().ok()).isFalse()
  }

}