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

import com.ascendcorp.tracing.zipkinforwarder.config.ForwarderConfiguration
import com.ascendcorp.tracing.zipkinforwarder.config.GCPConfiguration
import org.assertj.core.api.Assertions
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.support.AnnotationConfigContextLoader

@RunWith(SpringRunner::class)
@SpringBootTest(properties = ["destination.type=gcp-stackdriver"])
@ContextConfiguration(
    classes = [ForwarderConfiguration::class, GCPConfiguration::class, SpanTransporter::class],
    loader = AnnotationConfigContextLoader::class)
class StackDriverSenderTests {
  @Autowired
  lateinit var context: ApplicationContext

  @Autowired
  lateinit var forwarder: SpanTransporter

  @Test
  fun `GCPConfiguration is loaded when required property is set`() {
    Assertions.assertThat(context.containsBean("stackDriverSender")).isTrue()
  }

}

