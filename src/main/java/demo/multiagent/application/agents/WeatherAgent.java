package demo.multiagent.application.agents;

import demo.multiagent.application.SessionMemory;

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

  public WeatherAgent(SessionMemory sessionMemory) {
    super(sessionMemory);
  }


  @Override
  public String agentSpecificSystemMessage() {
    return sysMessage;
  }


}
