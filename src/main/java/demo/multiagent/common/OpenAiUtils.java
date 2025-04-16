package demo.multiagent.common;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import demo.multiagent.KeyUtils;

public class OpenAiUtils {

  final private static OpenAiChatModelName chatModelName = OpenAiChatModelName.GPT_4_O_MINI;

  public static OpenAiChatModel chatModel() {
    return OpenAiChatModel.builder()
      .apiKey(KeyUtils.readOpenAiKey())
      .modelName(chatModelName)
      .build();
  }




}
