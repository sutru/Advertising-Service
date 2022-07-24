package com.amazon.ata.advertising.service.targeting;

import com.amazon.ata.advertising.service.model.RequestContext;
import com.amazon.ata.advertising.service.targeting.predicate.TargetingPredicate;
import com.amazon.ata.advertising.service.targeting.predicate.TargetingPredicateResult;

import java.util.concurrent.Callable;

public class TargetingPredicateCallable implements Callable {

    public RequestContext requestContext;
    public TargetingPredicate predicate;

    public TargetingPredicateCallable(RequestContext requestContext, TargetingPredicate targetingPredicate) {
        this.requestContext = requestContext;
        this.predicate = targetingPredicate;
    }

    @Override
    public TargetingPredicateResult call() {
        return predicate.evaluate(requestContext);
    }
}
