package demo.multiagent.application;

import akka.Done;
import akka.javasdk.JsonSupport;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.workflow.Workflow;
import com.fasterxml.jackson.core.JsonProcessingException;
import demo.multiagent.application.agents.Planner;
import demo.multiagent.application.agents.Selector;
import demo.multiagent.domain.AgentSelection;
import demo.multiagent.domain.Plan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static demo.multiagent.application.AgenticWorkflow.Status.COMPLETED;
import static demo.multiagent.application.AgenticWorkflow.Status.FAILED;
import static demo.multiagent.application.AgenticWorkflow.Status.STARTED;


@ComponentId("agentic-workflow")
public class AgenticWorkflow extends Workflow<AgenticWorkflow.State> {

  private final Logger logger = LoggerFactory.getLogger(AgenticWorkflow.class);

  enum Status {
    STARTED,
    COMPLETED,
    FAILED,
  }

  public record State(String userQuery, String answer, Status status) {

    public static State init(String query) {
      return new State(query, "", STARTED);
    }

    public State withAnswer(String answer) {
      return new State(userQuery, answer, COMPLETED);
    }

    public State failed() {
      return new State(userQuery, answer, FAILED);
    }
  }

  private final Selector selector;
  private final Planner planner;


  public AgenticWorkflow(Selector agentSelector, Planner planner) {
    this.selector = agentSelector;
    this.planner = planner;
  }

  @Override
  public WorkflowDef<State> definition() {
    return workflow()
      .defaultStepRecoverStrategy(maxRetries(5).failoverTo(INTERRUPT))
      .addStep(selectAgent())
      .addStep(planExecution())
      .addStep(runPlan())
      .addStep(interrupt());
  }


  public Effect<Done> start(String query) {
    if (currentState() == null) {
      return effects()
        .updateState(State.init(query))
        .transitionTo(SELECT_AGENTS)
        .thenReply(Done.getInstance());
    } else {
      return effects().error("Workflow '" + commandContext().workflowId() + "' already started");
    }
  }

  public Effect<String> getAnswer() {
    if (currentState() == null) {
      return effects().error("Workflow '" + commandContext().workflowId() + "' not started");
    } else {
      return effects().reply(currentState().answer());
    }
  }

  private static final String SELECT_AGENTS = "select-agents";

  private Step selectAgent() {
    return step(SELECT_AGENTS)
      .call(() -> selector.selectAgents(currentState().userQuery))
      .andThen(AgentSelection.class, selection -> {
          logger.info("Selected agents: {}", selection.agents());
          return effects().transitionTo(CREATE_PLAN_EXECUTION, selection);
        }
      );
  }

  private static final String CREATE_PLAN_EXECUTION = "create-plan-execution";

  private Step planExecution() {
    return step(CREATE_PLAN_EXECUTION)
      .call(AgentSelection.class, agentSelection -> {
          logger.info(
            "Calling planner with {} / {}",
            currentState().userQuery,
            agentSelection.agents());

          return planner.createPlan(currentState().userQuery, agentSelection);
        }
      )
      .andThen(Plan.class, plan -> {
          logger.info("Execution plan: {}", plan);
          return effects().transitionTo(EXECUTE_PLAN, plan);
        }
      );
  }

  private static final String EXECUTE_PLAN = "run-plan";

  private Step runPlan() {
    return step(EXECUTE_PLAN)
      .call(Plan.class, plan -> {
        try {
          // TODO: execute each PlanStep
          // for now, just returning the plan as json for debugging
          return JsonSupport.getObjectMapper().writeValueAsString(plan);
        } catch (JsonProcessingException e) {
          throw new RuntimeException(e);
        }
      })
      .andThen(String.class, answer ->
        effects().updateState(currentState().withAnswer(answer)).end()
      );
  }

  private static final String INTERRUPT = "interrupt";

  private Workflow.Step interrupt() {
    return step(INTERRUPT)
      .call(() -> {
        logger.info("Interrupting workflow");
        return Done.getInstance();
      })
      .andThen(Done.class, __ -> effects().updateState(currentState().failed()).end());
  }
}
