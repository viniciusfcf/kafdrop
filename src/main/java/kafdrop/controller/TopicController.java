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

import java.util.Collections;
import java.util.List;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import kafdrop.model.ConsumerVO;
import kafdrop.model.CreateTopicVO;
import kafdrop.model.TopicVO;
import kafdrop.service.KafkaMonitor;
import kafdrop.service.TopicNotFoundException;

/**
 * Handles requests for the topic page.
 */
@Controller
@RequestMapping("/topic")
public final class TopicController {
  private final KafkaMonitor kafkaMonitor;
  private final boolean topicDeleteEnabled;
  private final boolean topicCreateEnabled;

  public TopicController(KafkaMonitor kafkaMonitor,
                         @Value("${topic.deleteEnabled:true}") Boolean topicDeleteEnabled, @Value("${topic.createEnabled:true}") Boolean topicCreateEnabled) {
    this.kafkaMonitor = kafkaMonitor;
    this.topicDeleteEnabled = topicDeleteEnabled;
    this.topicCreateEnabled = topicCreateEnabled;
  }

  @RequestMapping("/{name:.+}")
  public String topicDetails(@PathVariable("name") String topicName, Model model) {
    final var topic = kafkaMonitor.getTopic(topicName)
        .orElseThrow(() -> new TopicNotFoundException(topicName));
    model.addAttribute("topic", topic);
    model.addAttribute("consumers", kafkaMonitor.getConsumers(Collections.singleton(topic)));
    model.addAttribute("topicDeleteEnabled", topicDeleteEnabled);

    return "topic-detail";
  }

  @RequestMapping(value = "/{name:.+}/delete", method = RequestMethod.POST)
  public String deleteTopic(@PathVariable("name") String topicName, Model model) {
    if (!topicDeleteEnabled) {
      model.addAttribute("deleteErrorMessage", "Not configured to be deleted.");
      return topicDetails(topicName, model);
    }

    try {
      kafkaMonitor.deleteTopic(topicName);
      return "redirect:/";
    } catch (Exception ex) {
      model.addAttribute("deleteErrorMessage", ex.getMessage());
      return topicDetails(topicName, model);
    }
  }

  /**
   * Topic create page
   * @param model
   * @return creation page
   */
  @RequestMapping("/create")
  public String createTopicPage(Model model) {
    model.addAttribute("topicCreateEnabled", topicCreateEnabled);
    model.addAttribute("brokersCount", kafkaMonitor.getBrokers().size());
    return "topic-create";
  }

  @Operation(summary = "getTopic", description = "Get details for a topic")
  @APIResponses(value = {
      @APIResponse(responseCode = "200", response = TopicVO.class),
      @APIResponse(responseCode = "404")
  })
  @RequestMapping(path = "/{name:.+}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
  public @ResponseBody TopicVO getTopic(@PathVariable("name") String topicName) {
    return kafkaMonitor.getTopic(topicName)
        .orElseThrow(() -> new TopicNotFoundException(topicName));
  }

  @Operation(summary = "getAllTopics", description = "Get list of all topics")
  @APIResponses(value = {
      @APIResponse(responseCode = "200", response = String.class, responseContainer = "List")
  })
  @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
  public @ResponseBody List<TopicVO> getAllTopics() {
    return kafkaMonitor.getTopics();
  }

  @Operation(summary = "getConsumers", description = "Get consumers for a topic")
  @APIResponses(value = {
      @APIResponse(responseCode = "200", response = String.class, responseContainer = "List"),
      @APIResponse(responseCode = "404")
  })
  @RequestMapping(path = "/{name:.+}/consumers", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
  public @ResponseBody List<ConsumerVO> getConsumers(@PathVariable("name") String topicName) {
    final var topic = kafkaMonitor.getTopic(topicName)
        .orElseThrow(() -> new TopicNotFoundException(topicName));
    return kafkaMonitor.getConsumers(Collections.singleton(topic));
  }

  /**
   * API for topic creation
   * @param createTopicVO request
   */
  @Operation(summary = "createTopic", description = "Create topic")
  @APIResponses(value = {
      @APIResponse(responseCode = "200", response = String.class)
  })
  @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
  public String createTopic(CreateTopicVO createTopicVO, Model model) {
    model.addAttribute("topicCreateEnabled", topicCreateEnabled);
    model.addAttribute("topicName", createTopicVO.getName());
    if (!topicCreateEnabled) {
      model.addAttribute("errorMessage", "Not configured to be created.");
      return createTopicPage(model);
    }
    try {
        kafkaMonitor.createTopic(createTopicVO);
    } catch (Exception ex) {
      model.addAttribute("errorMessage", ex.getMessage());
    }
    model.addAttribute("brokersCount", kafkaMonitor.getBrokers().size());
    return "topic-create";
  }
}
