package com.example.demo.support;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.DockerClientFactory;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@ExtendWith(RequiresDocker.RequiresDockerCondition.class)
public @interface RequiresDocker {

    class RequiresDockerCondition implements ExecutionCondition {
        @Override
        public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
            try {
                boolean available = DockerClientFactory.instance().isDockerAvailable();
                if (available) {
                    return ConditionEvaluationResult.enabled("Docker is available");
                } else {
                    return ConditionEvaluationResult.disabled("Docker is not available - skipping integration test");
                }
            } catch (Throwable t) {
                return ConditionEvaluationResult.disabled("Docker check failed: " + t.getMessage() + " - skipping integration test");
            }
        }
    }
}
