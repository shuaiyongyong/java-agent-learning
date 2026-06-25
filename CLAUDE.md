# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

A learning project (`java-agent-learning`) exploring LLM integration in Java/Spring Boot. It is a single Spring Boot app that deliberately demonstrates **three different ways** to call a local Ollama model, so expect parallel/overlapping implementations rather than one canonical path. Code comments and prompts are in Chinese.

## Commands

```bash
# Run the app (requires Ollama running at http://localhost:11434)
mvn spring-boot:run

# Override the Ollama model (default qwen2.5:7b) or base URL via env vars
OLLAMA_MODEL=llama3.1 mvn spring-boot:run
OLLAMA_BASE_URL=http://host:11434 mvn spring-boot:run

# Build / package
mvn clean package

# Run all tests
mvn test

# Run a single test class / method
mvn test -Dtest=JavaAgentLearningApplicationTests
mvn test -Dtest=JavaAgentLearningApplicationTests#methodName
```

Requirements: JDK 17, Maven 3.8+, a running local Ollama. `spring.ai.ollama.init.pull-model-strategy=always` means Spring AI will attempt to pull the configured model on startup, so the model name must be valid/available to Ollama.

## Architecture

Three independent LLM-access mechanisms coexist. When adding or modifying a feature, identify which mechanism the endpoint belongs to first.

1. **Spring AI `ChatClient`** (the primary, most-developed path)
   - `config/ChatClientConfig.java` defines named `ChatClient` beans (`defaultClient`, `translatorClient`) — the translator bean bakes in a system prompt. **This is the key wiring file**: services inject a specific client by name via `@Qualifier`.
   - Flow: `controller/*Controller` → `service/*Service` (constructor-injected `@Qualifier` ChatClient) → Ollama.
   - Endpoints: `/default/chat`, `/translation/chat`, `/output/person` & `/output/personConverter`.
   - Structured output (`StructuredOutputService`) shows two techniques side by side: high-level `ChatClient.entity(Class)` vs. low-level `BeanOutputConverter` with manual `{format}` prompt injection. Target type is the `record/Person` record.

2. **LangChain4j `@AiService` interfaces** (`service/OllamaAssistant`, `service/OpenAiAssistant`)
   - Declarative AI services using `@AiService(wiringMode = EXPLICIT, chatModel = "...")`. No implementation — LangChain4j generates the proxy and wires it to a named chat model bean (`ollamaChatModel` / `openAiChatModel`).
   - Exposed at `/assistant/chat`. Note `OpenAiAssistant` references `openAiChatModel`, configured under the separate `langchain4j.open-ai.*` properties.

3. **Raw HTTP demo** (`demo/` package)
   - `OllamaClient` calls the Ollama `/api/chat` endpoint directly with `java.net.http.HttpClient`, handling both streaming and non-streaming modes manually. `OllamaChatExample` has a `main()` and is a standalone runnable example, **not** part of the Spring web app. `ChatBody` is its Lombok request DTO.

## Configuration notes

- `src/main/resources/application.properties` holds **two separate config namespaces**: `spring.ai.ollama.*` (mechanism 1) and `langchain4j.ollama.*` / `langchain4j.open-ai.*` (mechanism 2). They configure different models independently — changing one does not affect the other.
- UTF-8 is forced across Tomcat/servlet encoding (for Chinese I/O). Some comment lines in `application.properties` render as `?` due to encoding.
