package demo.multiagent.application.agents;

import demo.multiagent.application.SessionMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;

// TODO: make this agent interact with a real weather API
@AgentCard(
    id = "weather-agent",
    name = "Weather Agent",
    description = """
      An agent that provides weather information. It can provide current weather, forecasts, and other
      related information.
    """
)
public class WeatherAgent extends Agent {


  private final String sysMessage = """
      You are a fake weather agent.
      Your job is to provide made up weather information. It can provide current weather,
      forecasts, and other related information.
    """;

  public WeatherAgent(SessionMemory sessionMemory, ChatLanguageModel chatLanguageModel) {
    super(sessionMemory, chatLanguageModel);
  }


  @Override
  public String agentSpecificSystemMessage() {
    return sysMessage;
  }


}
