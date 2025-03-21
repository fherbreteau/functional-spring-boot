package io.github.fherbreteau.functional.domain.access;

import static java.util.Optional.ofNullable;

import java.util.function.BinaryOperator;

import io.github.fherbreteau.functional.domain.entities.AccessRight;

public class AccessContext {
    private BinaryOperator<AccessRight> accessRightMergeFunction;
    private String step;

    public AccessRight applyMergeFunction(AccessRight accessRight, AccessRight itemAccess) {
        return ofNullable(accessRightMergeFunction)
                .map(f -> f.apply(accessRight, itemAccess))
                .orElse(null);
    }

    public void setAccessRightMergeFunction(BinaryOperator<AccessRight> accessRightMergeFunction) {
        this.accessRightMergeFunction = accessRightMergeFunction;
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }
}
