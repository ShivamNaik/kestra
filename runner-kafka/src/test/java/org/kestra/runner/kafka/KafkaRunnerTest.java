package org.kestra.runner.kafka;

import com.google.common.collect.ImmutableMap;
import org.apache.kafka.common.errors.RecordTooLargeException;
import org.junit.jupiter.api.Test;
import org.kestra.core.models.executions.Execution;
import org.kestra.core.models.flows.State;
import org.kestra.core.queues.QueueException;
import org.kestra.core.runners.InputsTest;
import org.kestra.core.runners.ListenersTest;
import org.kestra.core.runners.RunnerCaseTest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javax.inject.Inject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KafkaRunnerTest extends AbstractKafkaRunnerTest {
    @Inject
    private RunnerCaseTest runnerCaseTest;

    @Test
    void full() throws TimeoutException, QueueException {
        Execution execution = runnerUtils.runOne("org.kestra.tests", "full");

        assertThat(execution.getTaskRunList(), hasSize(13));
    }

    @Test
    void logs() throws TimeoutException {
        Execution execution = runnerUtils.runOne("org.kestra.tests", "logs");

        assertThat(execution.getTaskRunList(), hasSize(3));
    }

    @Test
    void errors() throws TimeoutException, QueueException {
        Execution execution = runnerUtils.runOne("org.kestra.tests", "errors");

        assertThat(execution.getTaskRunList(), hasSize(7));
    }

    @Test
    void sequential() throws TimeoutException, QueueException {
        Execution execution = runnerUtils.runOne("org.kestra.tests", "sequential");

        assertThat(execution.getTaskRunList(), hasSize(11));
    }

    @Test
    void parallel() throws TimeoutException, QueueException {
        Execution execution = runnerUtils.runOne("org.kestra.tests", "parallel", null, null, Duration.ofSeconds(120));

        assertThat(execution.getTaskRunList(), hasSize(8));
    }

    @Test
    void parallelNested() throws TimeoutException, QueueException {
        Execution execution = runnerUtils.runOne("org.kestra.tests", "parallel-nested");

        assertThat(execution.getTaskRunList(), hasSize(11));
    }

    @Test
    void listeners() throws TimeoutException, QueueException, IOException, URISyntaxException {
        repositoryLoader.load(Objects.requireNonNull(ListenersTest.class.getClassLoader().getResource("flows/tests")));

        Execution execution = runnerUtils.runOne(
            "org.kestra.tests",
            "listeners",
            null,
            (f, e) -> ImmutableMap.of("string", "OK")
        );

        assertThat(execution.getTaskRunList().get(1).getTaskId(), is("ok"));
        assertThat(execution.getTaskRunList().size(), is(3));
        assertThat(execution.getTaskRunList().get(2).getTaskId(), is("execution-success-listener"));
    }

    @Test
    void recordTooLarge() {
        char[] chars = new char[2000000];
        Arrays.fill(chars, 'a');

        HashMap<String, String> inputs = new HashMap<>(InputsTest.inputs);
        inputs.put("string", new String(chars));

        RuntimeException e = assertThrows(RuntimeException.class, () -> {
            runnerUtils.runOne(
                "org.kestra.tests",
                "inputs",
                null,
                (flow, execution1) -> runnerUtils.typedInputs(flow, execution1, inputs)
            );
        });

        assertThat(e.getCause().getClass(), is(ExecutionException.class));
        assertThat(e.getCause().getCause().getClass(), is(RecordTooLargeException.class));
    }

    @Test
    void workerRecordTooLarge() throws TimeoutException {
        char[] chars = new char[600000];
        Arrays.fill(chars, 'a');

        HashMap<String, String> inputs = new HashMap<>(InputsTest.inputs);
        inputs.put("string", new String(chars));

        Execution execution = runnerUtils.runOne(
            "org.kestra.tests",
            "inputs",
            null,
            (flow, execution1) -> runnerUtils.typedInputs(flow, execution1, inputs)
        );

        assertThat(execution.getState().getCurrent(), is(State.Type.FAILED));
    }

    @Test
    void invalidVars() throws TimeoutException {
        Execution execution = runnerUtils.runOne("org.kestra.tests", "variables-invalid");

        assertThat(execution.getTaskRunList(), hasSize(2));
        assertThat(execution.getTaskRunList().get(1).getState().getCurrent(), is(State.Type.FAILED));
        assertThat(execution.getTaskRunList().get(1).getAttempts().get(0).getLogs().get(0).getMessage(), containsString("Missing variable: inputs.invalid"));
        assertThat(execution.getState().getCurrent(), is(State.Type.FAILED));
    }

    @Test
    void restart() throws Exception {
        runnerCaseTest.restart();
    }
}
