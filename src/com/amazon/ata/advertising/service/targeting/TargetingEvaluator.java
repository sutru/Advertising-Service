package com.amazon.ata.advertising.service.targeting;

import com.amazon.ata.advertising.service.model.RequestContext;
import com.amazon.ata.advertising.service.targeting.predicate.TargetingPredicate;
import com.amazon.ata.advertising.service.targeting.predicate.TargetingPredicateResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Evaluates TargetingPredicates for a given RequestContext.
 */
public class TargetingEvaluator {
    public static final boolean IMPLEMENTED_STREAMS = true;
    public static final boolean IMPLEMENTED_CONCURRENCY = true;
    private final RequestContext requestContext;

    /**
     * Creates an evaluator for targeting predicates.
     * @param requestContext Context that can be used to evaluate the predicates.
     */
    public TargetingEvaluator(RequestContext requestContext) {
        this.requestContext = requestContext;
    }

    /**
     * Evaluate a TargetingGroup to determine if all of its TargetingPredicates are TRUE or not for the given
     * RequestContext.
     * @param targetingGroup Targeting group for an advertisement, including TargetingPredicates.
     * @return TRUE if all of the TargetingPredicates evaluate to TRUE against the RequestContext, FALSE otherwise.
     */
//    public TargetingPredicateResult evaluate(TargetingGroup targetingGroup) {
//        List<TargetingPredicate> targetingPredicates = targetingGroup.getTargetingPredicates();
//        boolean allTruePredicates = true;
//        for (TargetingPredicate predicate : targetingPredicates) {
//            TargetingPredicateResult predicateResult = predicate.evaluate(requestContext);
//            if (!predicateResult.isTrue()) {
//                allTruePredicates = false;
//                break;
//            }
//        }
//
//        return allTruePredicates ? TargetingPredicateResult.TRUE :
//                                   TargetingPredicateResult.FALSE;
//    }

    public TargetingPredicateResult evaluate(TargetingGroup targetingGroup) {
        List<TargetingPredicate> targetingPredicates = targetingGroup.getTargetingPredicates();
        ExecutorService executor = Executors.newCachedThreadPool();
        List<Future<TargetingPredicateResult>> futures = new ArrayList<>();
        // = targetingPredicates.stream()
//                .map(targetingPredicate ->  {
//                    TargetingPredicateCallable callable =
//                            new TargetingPredicateCallable(requestContext, targetingPredicate);
//                    return executor.submit(callable);
//                })
//                .collect(Collectors.toList());
        for (TargetingPredicate targetingPredicate : targetingPredicates) {
            TargetingPredicateCallable callable =
                            new TargetingPredicateCallable(requestContext, targetingPredicate);
            futures.add(executor.submit(callable));
        }

        List<TargetingPredicateResult> result = futures.stream()
                .map(resultFuture -> {
                    try {
                        return resultFuture.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .filter(targetingPredicateResult -> !targetingPredicateResult.isTrue()
                )
                .collect(Collectors.toList());


//        List<TargetingPredicate> result = targetingPredicates.stream()
//                // is ! not correct or opposite
//                .filter(targetingPredicate ->{
//                    TargetingPredicateCallable callable =
//                            new TargetingPredicateCallable(requestContext, targetingPredicate);
//                    return
//                    !targetingPredicate.evaluate(requestContext).isTrue();
//                })
//                .collect(Collectors.toList());

//        List<TargetingPredicate> result = targetingPredicates.stream()
//                // is ! not correct or opposite
//                .filter(targetingPredicate ->
//                    !targetingPredicate.evaluate(requestContext).isTrue()
//                )
//                .collect(Collectors.toList());

        return result.isEmpty() ? TargetingPredicateResult.TRUE :
                TargetingPredicateResult.FALSE;
    }
}
