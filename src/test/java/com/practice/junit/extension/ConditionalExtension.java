package com.practice.junit.extension;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

public class ConditionalExtension implements ExecutionCondition {
    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        return System.getProperty("skip") != null
                ? ConditionEvaluationResult.disabled("Test is skipped")
                : ConditionEvaluationResult.enabled("Test is enabled by default");
    }
}
