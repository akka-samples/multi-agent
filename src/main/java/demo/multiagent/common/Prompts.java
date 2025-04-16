package demo.multiagent.common;

public class Prompts {

  public static final String agentResponseSpec = """
      IMPORTANT:
      Output should be in json format with the following fields:
        {
          "response": "string",
          "error": "string",
        }
      
      When you can generate a response, the error field should be empty.
        For example:
        {
          "response": "The weather is sunny.",
          "error": ""
        }
      
      When you can't generate a response, then it should be empty and the error
      field should contain a message explaining why you couldn't generate a response.
        For example:
        {
            "response": "",
            "error": "I cannot provide a response for this question."
        }
  
      You return an error if the asked question is outside your domain of expertise,
       if it's invalid or if you cannot provide a response for any other reason.

      Do not include any explanations or text outside of the JSON structure.
      """;
}
