# java-agent-learning

Spring Boot Maven project for learning Java agents with Spring AI and Ollama.

## Requirements

- JDK 17
- Maven 3.8+
- Ollama running locally on `http://localhost:11434`

## Run

```bash
mvn spring-boot:run
```

The default Ollama model is `qwen2.5:7b`. Override it with:

```bash
OLLAMA_MODEL=llama3.1 mvn spring-boot:run
```
