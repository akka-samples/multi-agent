package demo.multiagent.application.agents;

import demo.multiagent.domain.AgentResponse;
import dev.langchain4j.service.AiServices;
import demo.multiagent.common.OpenAiUtils;
import demo.multiagent.common.Prompts;

@AgentCard(
  id = "activity-agent",
  name = "Activity Agent",
  description = """
      An agent that suggests activities in the real world. Like for example, a team building activity, sports,
      an indoor or outdoor game, board games, a city trip, etc.
    """
)
public class ActivityAgent implements Agent {

  private final String sysMessage = """
      You are an activity agent. Your job is to suggest activities in the real world. Like for example, a team
      building activity, sports, an indoor or outdoor game, board games, a city trip, etc.
    
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
