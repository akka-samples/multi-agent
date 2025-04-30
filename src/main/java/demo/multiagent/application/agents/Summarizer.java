package demo.multiagent.application.agents;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;


public class Summarizer {

  private final ChatLanguageModel chatLanguageModel;

  public Summarizer(ChatLanguageModel chatLanguageModel) {
    this.chatLanguageModel = chatLanguageModel;
  }

  interface Assistant {
    String chat(String message);
  }

  private String buildSystemMessage(String userQuery) {
    return  """
        You will receive the original query and a message generate by different other agents.
      
        Your task is to build a new message using the message provided by the other agents.
        You are not allowed to add any new information, you should only re-phrase it to make
        them part of coherent message.
      
        The message to summarize will be provided between single quotes.
      
        ORIGINAL USER QUERY:
        %s
      """.formatted(userQuery);
  }


  public String summarize(String originalQuery, String agentsResponses) {

    var assistant = AiServices.builder(Assistant.class)
      .chatLanguageModel(chatLanguageModel)
      .systemMessageProvider(__ -> buildSystemMessage(originalQuery))
      .build();

    return assistant.chat("Summarize the following message: '" + agentsResponses + "'");
  }

}
