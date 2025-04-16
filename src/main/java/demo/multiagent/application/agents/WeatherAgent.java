package demo.multiagent.application.agents;

import demo.multiagent.domain.AgentResponse;
import dev.langchain4j.service.AiServices;
import demo.multiagent.common.OpenAiUtils;
import demo.multiagent.common.Prompts;

// TODO: make this agent interact with a real weather API
@AgentCard(
    id = "weather-agent",
    name = "Weather Agent",
    description = """
      An agent that provides weather information. It can provide current weather, forecasts, and other
      related information.
    """
)
public class WeatherAgent implements Agent {

  private final String sysMessage = """
      You are a fake weather agent.
      Your job is to provide made up weather information. It can provide current weather,
      forecasts, and other related information.
    
      %s
    """.formatted(Prompts.agentResponseSpec);

  interface Assistant {
    String chat(String message);
  }


  @Override
  public AgentResponse query(String message) {

    var assistant = AiServices.builder(Assistant.class)
      .chatLanguageModel(OpenAiUtils.chatModel())
      .systemMessageProvider(__ -> sysMessage)
      .build();

    return AgentResponse.fromJson(assistant.chat(message));
  }
}
