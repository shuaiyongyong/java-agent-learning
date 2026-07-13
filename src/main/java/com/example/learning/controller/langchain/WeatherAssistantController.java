package com.example.learning.controller.langchain;

import com.example.learning.assistant.WeatherAssistant;
import com.example.learning.assistant.weather.*;
import dev.langchain4j.service.TokenStream;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/weather/assistant")
public class WeatherAssistantController {

    @Resource
    WeatherAssistant weatherAssistant;

    @Resource
    WeatherAssistantV0 weatherAssistantV0;

    @Resource
    WeatherAssistantV1 weatherAssistantV1;

    @Resource
    WeatherAssistantV2 weatherAssistantV2;

    @Resource
    WeatherAssistantV3 weatherAssistantV3;

    @Resource
    WeatherAssistantV4 weatherAssistantV4;


    /**
     * 天气查询
     */
    @GetMapping("/getWeather")
    public String weather(@RequestParam String message) {
        return weatherAssistant.chat(message);
    }

    /**
     * 天气查询流式聊天接口（SSE），基于 LangChain4j TokenStream。
     */
    @GetMapping(value = "/getWeather/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter weatherStream(@RequestParam String message) {
        SseEmitter emitter = new SseEmitter(60_000L);
        TokenStream tokenStream = weatherAssistant.chatStream(message);
        tokenStream
                .onPartialResponse(chunk -> {
                    try {
                        emitter.send(SseEmitter.event().data(chunk));
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                })
                .onCompleteResponse(resp -> emitter.complete())
                .onError(emitter::completeWithError)
                .start();

        return emitter;
    }

    /**
     * 测试  5 个天气助手变体（V0~V4）。
     */
    @GetMapping("/getWeatherV0")
    public String getWeatherV0(@RequestParam String message) {
        return weatherAssistantV0.chat(message);
    }

    @GetMapping("/getWeatherV1")
    public String getWeatherV1(@RequestParam String message) {
        return weatherAssistantV1.chat(message);
    }

    @GetMapping("/getWeatherV2")
    public String getWeatherV2(@RequestParam String message) {
        return weatherAssistantV2.chat(message);
    }

    @GetMapping("/getWeatherV3")
    public String getWeatherV3(@RequestParam String message) {
        return weatherAssistantV3.chat(message);
    }

    @GetMapping("/getWeatherV4")
    public String getWeatherV4(@RequestParam String message) {
        return weatherAssistantV4.chat(message);
    }

}
