/*
 * Copyright 2017 Kafdrop contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package kafdrop.controller;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import kafdrop.model.BrokerVO;
import kafdrop.service.BrokerNotFoundException;
import kafdrop.service.KafkaMonitor;

@Controller
public final class BrokerController {
  private final KafkaMonitor kafkaMonitor;

  public BrokerController(KafkaMonitor kafkaMonitor) {
    this.kafkaMonitor = kafkaMonitor;
  }

  @RequestMapping("/broker/{id}")
  public String brokerDetails(@PathVariable("id") int brokerId, Model model) {
    model.addAttribute("broker", kafkaMonitor.getBroker(brokerId)
        .orElseThrow(() -> new BrokerNotFoundException("No such broker " + brokerId)));
    model.addAttribute("topics", kafkaMonitor.getTopics());
    return "broker-detail";
  }

  @Operation(summary = "getBroker", description = "Get details for a specific Kafka broker")
  @APIResponses(value = {
      @APIResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = BrokerVO.class))),
      @APIResponse(responseCode = "404")
  })
  @RequestMapping(path = "/broker/{id}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
  public @ResponseBody BrokerVO brokerDetailsJson(@PathVariable("id") int brokerId) {
    return kafkaMonitor.getBroker(brokerId).orElseThrow(() -> new BrokerNotFoundException("No such broker " + brokerId));
  }

  @Operation(summary = "getAllBrokers", description = "Get details for all known Kafka brokers")
  @APIResponses(value = {
      @APIResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = BrokerVO.class)))
  })
  @RequestMapping(path = "/broker", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
  public @ResponseBody List<BrokerVO> brokerDetailsJson() {
    return kafkaMonitor.getBrokers();
  }
}
