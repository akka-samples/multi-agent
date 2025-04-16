package demo.multiagent.common;

import demo.multiagent.application.agents.ActivityAgent;
import demo.multiagent.application.agents.Agent;
import demo.multiagent.application.agents.AgentCard;
import demo.multiagent.application.agents.WeatherAgent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Agents {

  private final static List<Class<? extends Agent>> agents =
    List.of(
      ActivityAgent.class,
      WeatherAgent.class
    );


  private final static Map<String, Class<? extends Agent>> agentMap =
    agents.stream()
      .filter(agentClass -> agentClass.isAnnotationPresent(AgentCard.class))
      .collect(Collectors.toMap(
        agentClass -> agentClass.getAnnotation(AgentCard.class).id(),
        agentClass -> agentClass
      ));


  public static String allAgentsInJson() {
    return genListOfAgents(agents);
  }

  public static String agentSelectionInJson(List<String> agentsIds) {
    List<Class<? extends Agent>> selected = new ArrayList<>();
    for (String agentId : agentsIds) {
      selected.add(agentMap.get(agentId));
    }
    return genListOfAgents(selected);
  }

  private static String genListOfAgents(List<Class<? extends Agent>> agents) {
    return agents.stream()
      .filter(agentClass -> agentClass.isAnnotationPresent(AgentCard.class))
      .map(agentClass -> {
        AgentCard annotation = agentClass.getAnnotation(AgentCard.class);
        return String.format("""
            {"id": "%s", "name": "%s", "description": "%s"}""",
          annotation.id(),
          annotation.name(),
          annotation.description());
      })
      .collect(Collectors.joining(", ", "[", "]"));
  }
  public static Class<? extends Agent> getAgent(String agentId) {
    return agentMap.get(agentId);
  }
}
