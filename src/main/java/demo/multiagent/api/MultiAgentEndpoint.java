package demo.multiagent.api;

import akka.http.javadsl.model.HttpResponse;
import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.Get;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.javasdk.annotations.http.Post;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.http.HttpResponses;
import demo.multiagent.application.AgenticWorkflow;
import demo.multiagent.application.agents.Selector;


@Acl(allow = @Acl.Matcher(principal = Acl.Principal.INTERNET))
@HttpEndpoint("/chat")
public class MultiAgentEndpoint {

  public record Request(String message) {
  }

  private final ComponentClient componentClient;

  public MultiAgentEndpoint(ComponentClient componentClient) {
    this.componentClient = componentClient;
  }

  @Post("/{chatId}")
  public HttpResponse handleRequest(String chatId, Request request) {
    var res =
      componentClient
      .forWorkflow(chatId)
        .method(AgenticWorkflow::start)
        .invoke(request.message);

    return HttpResponses.created(res, "/chat/" + chatId);
  }

  @Get("/{chatId}")
  public HttpResponse getAnswer(String chatId) {
    var res =
      componentClient
        .forWorkflow(chatId)
        .method(AgenticWorkflow::getAnswer)
        .invoke();

    return HttpResponses.ok(res);
  }
}
