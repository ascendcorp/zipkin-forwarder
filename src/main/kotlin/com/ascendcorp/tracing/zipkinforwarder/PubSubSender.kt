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

import com.google.api.core.ApiFuture
import com.google.cloud.pubsub.v1.Publisher
import com.google.protobuf.ByteString
import com.google.pubsub.v1.ProjectTopicName
import com.google.pubsub.v1.PubsubMessage
import zipkin2.Call
import zipkin2.Callback
import zipkin2.codec.Encoding
import zipkin2.reporter.BytesMessageEncoder
import java.util.ArrayList



class PubSubSender (
  val project: String?,
  val topic: String?,
  val encoding: Encoding?,
  val messageSizeInBytes: Int?,
  val messageMaxBytes: Int?
    ) {

  data class Builder(
    var project: String? = null,
    var topic: String? = null,
    var encoding: Encoding? = null,
    var messageSizeInBytes: Int? = null,
    var messageMaxBytes: Int? = null
      ){
    fun project(project: String) = apply { this.project = project }
    fun topic(topic: String) = apply { this.topic = topic }
    fun encoding(encoding: Encoding) = apply { this.encoding = encoding }
    fun messageSizeInBytes(messageSizeInBytes: Int) = apply { this.messageSizeInBytes = messageSizeInBytes }
    fun messageMaxBytes(messageMaxBytes: Int) = apply { this.messageMaxBytes = messageMaxBytes }
    fun build() = PubSubSender(project, topic, encoding,
        messageSizeInBytes, messageMaxBytes)
  }

  fun sendSpans(list: List<ByteArray?>): Call<Void> {

    val payload = ByteString.copyFrom(BytesMessageEncoder.forEncoding(encoding).encode(list))
    val pubsubMessage = PubsubMessage.newBuilder()
        .setData(payload)
        .build()
    return PubSubCall(pubsubMessage);

  }

  fun pblisher(): Publisher? {
    val topicName = ProjectTopicName
        .of(project, topic)
    val publisher = Publisher
        .newBuilder(topicName)
        .build()
    return publisher
  }

  fun messageSizeInBytes(p0: MutableList<ByteArray>?): Int? {
    return messageSizeInBytes
  }

  fun encoding(): Encoding? {
    return encoding

  }

  fun messageMaxBytes(): Int? {
    return messageMaxBytes
  }

  inner class PubSubCall() : Call.Base<Void>(){

    lateinit var message: PubsubMessage
    lateinit var future: ApiFuture<String>
    var futures: MutableList<ApiFuture<String>> = ArrayList()

    constructor(message: PubsubMessage) : this() {
      this.message = message
    }

    override fun doExecute(): Void? {
      future = this@PubSubSender.pblisher()!!.publish(message)
      futures.add(future)
      return null
    }

    override fun doEnqueue(callback: Callback<Void>?) {
      future = this@PubSubSender.pblisher()!!.publish(message)
      futures.add(future)
    }

    override fun clone(): Call<Void> {
      return PubSubCall(message)
    }

  }
}