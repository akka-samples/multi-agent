package demo.multiagent.application.agents;

import akka.javasdk.agent.Agent;
import akka.javasdk.annotations.AgentDescription;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.http.HttpClientProvider;
import demo.multiagent.domain.AgentResponse;

@ComponentId("weather-agent")
@AgentDescription(
    name = "Weather Agent",
    description = """
      An agent that provides weather information. It can provide current weather, forecasts, and other
      related information.
    """,
    role = "doer"
)
public class WeatherAgent extends Agent {

  private static final String SYSTEM_MESSAGE = """
      You are a weather agent.
      Your job is to provide weather information.
      You provide current weather, forecasts, and other related information.

      The responses from the weather services are in json format. You need to digest it into human language. Be aware that
      Celsius temperature is in temp_c field. Fahrenheit temperature is in temp_f field.
    """ + AgentResponse.FORMAT_INSTRUCTIONS;

  private final HttpClientProvider httpClientProvider;

  public WeatherAgent(HttpClientProvider httpClientProvider) {
    this.httpClientProvider = httpClientProvider;
  }

  public Agent.Effect<AgentResponse> query(String message) {
    return effects()
        .reply(new AgentResponse("It's always sunny", null));

    // FIXME tool WeatherService
//    return effects()
//        .systemMessage(SYSTEM_MESSAGE)
//        .userMessage(message)
//        .responseAs(AgentResponse.class)
//        .thenReply();
  }

}
