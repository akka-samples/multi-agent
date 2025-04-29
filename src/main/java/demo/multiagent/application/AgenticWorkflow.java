package demo.multiagent.application;

import akka.Done;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.workflow.Workflow;
import demo.multiagent.application.agents.Planner;
import demo.multiagent.application.agents.Selector;
import demo.multiagent.common.AgentsRegistry;
import demo.multiagent.domain.AgentSelection;
import demo.multiagent.domain.Plan;
import demo.multiagent.domain.PlanStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static demo.multiagent.application.AgenticWorkflow.Status.COMPLETED;
import static demo.multiagent.application.AgenticWorkflow.Status.FAILED;
import static demo.multiagent.application.AgenticWorkflow.Status.STARTED;


@ComponentId("agentic-workflow")
public class AgenticWorkflow extends Workflow<AgenticWorkflow.State> {


  enum Status {
    STARTED,
    COMPLETED,
    FAILED,
  }

  public record State(String userQuery,
                      Plan plan,
                      String answer,
                      Status status) {

    public static State init(String query) {
      return new State(query, new Plan(), "", STARTED);
    }


    public State withAnswer(String answer) {
      return new State(userQuery, plan, this.answer + " " + answer, status);
    }

    public PlanStep pickFirstPlanStep() {
      return plan.steps().getFirst();
    }

    public State removeFirstPlanStep() {
      plan.steps().removeFirst();
      return this;
    }

    public State withPlan(Plan plan) {
      return new State(userQuery, plan, answer, STARTED);
    }

    public State complete() {
      return new State(userQuery, plan, answer, COMPLETED);
    }

    public State failed() {
      return new State(userQuery, plan, answer, FAILED);
    }
  }

  private final Logger logger = LoggerFactory.getLogger(AgenticWorkflow.class);

  private final AgentsRegistry agentsRegistry;
  private final Selector selector;
  private final Planner planner;


  public AgenticWorkflow(AgentsRegistry agentsRegistry, Selector agentSelector, Planner planner) {
    this.agentsRegistry = agentsRegistry;
    this.selector = agentSelector;
    this.planner = planner;
  }

  @Override
  public WorkflowDef<State> definition() {
    return workflow()
      .defaultStepRecoverStrategy(maxRetries(1).failoverTo(INTERRUPT))
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
        logger.debug("Selected agents: {}", selection.agents());
          return effects().transitionTo(CREATE_PLAN_EXECUTION, selection);
        }
      );
  }

  private static final String CREATE_PLAN_EXECUTION = "create-plan-execution";

  private Step planExecution() {
    return step(CREATE_PLAN_EXECUTION)
      .call(AgentSelection.class, agentSelection -> {
        logger.debug(
            "Calling planner with {} / {}",
            currentState().userQuery,
            agentSelection.agents());

          return planner.createPlan(currentState().userQuery, agentSelection);
        }
      )
      .andThen(Plan.class, plan -> {
        logger.debug("Execution plan: {}", plan);
        return effects()
          .updateState(currentState().withPlan(plan))
          .transitionTo(EXECUTE_PLAN);
        }
      );
  }

  private static final String EXECUTE_PLAN = "run-plan";

  private Step runPlan() {
    return step(EXECUTE_PLAN)
      .call(() -> {

        var stepPlan = currentState().pickFirstPlanStep();
        logger.debug("Executing plan step (agent:{}), asking {}", stepPlan.agentId(), stepPlan.query());

        var agent = agentsRegistry.getAgent(stepPlan.agentId());

        var agentResponse = agent.query(commandContext().workflowId(), stepPlan.query());
        if (agentResponse.isValid()) {
          logger.debug("Response from [agent:{}]: '{}'", stepPlan.agentId(), agentResponse);
          return agentResponse.response();
        } else {
          throw new RuntimeException("Agent '" + stepPlan.agentId() + "' responded with error: " + agentResponse.error());
        }

      })
      .andThen(String.class, answer -> {

          logger.debug("Adding answer '{}'", answer);
          var newState = currentState().withAnswer(answer).removeFirstPlanStep();
          if (newState.plan().steps().isEmpty()) {
            logger.debug("No further steps to execute.");
            return effects().updateState(newState.complete()).end();
          } else {
            logger.debug("Still {} steps to execute.", newState.plan().steps().size());
            return effects().updateState(newState).transitionTo(EXECUTE_PLAN);
          }
        }
      );
  }

  private static final String INTERRUPT = "interrupt";

  private Workflow.Step interrupt() {
    return step(INTERRUPT)
      .call(() -> {
        logger.debug("Interrupting workflow");
        return Done.getInstance();
      })
      .andThen(Done.class, __ -> effects().updateState(currentState().failed()).end());
  }
}
