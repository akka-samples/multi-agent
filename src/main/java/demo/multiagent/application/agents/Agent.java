package demo.multiagent.application.agents;

import demo.multiagent.domain.AgentResponse;

public interface Agent {
  AgentResponse query(String message);
}
