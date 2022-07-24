package com.amazon.ata.advertising.service.businesslogic;

import com.amazon.ata.advertising.service.dao.ReadableDao;
import com.amazon.ata.advertising.service.model.AdvertisementContent;
import com.amazon.ata.advertising.service.model.EmptyGeneratedAdvertisement;
import com.amazon.ata.advertising.service.model.GeneratedAdvertisement;
import com.amazon.ata.advertising.service.model.RequestContext;
import com.amazon.ata.advertising.service.targeting.TargetingEvaluator;
import com.amazon.ata.advertising.service.targeting.TargetingGroup;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;
import javax.inject.Inject;

/**
 * This class is responsible for picking the advertisement to be rendered.
 */
public class AdvertisementSelectionLogic {

    private static final Logger LOG = LogManager.getLogger(AdvertisementSelectionLogic.class);

    private final ReadableDao<String, List<AdvertisementContent>> contentDao;
    private final ReadableDao<String, List<TargetingGroup>> targetingGroupDao;
    private Random random = new Random();

    /**
     * Constructor for AdvertisementSelectionLogic.
     * @param contentDao Source of advertising content.
     * @param targetingGroupDao Source of targeting groups for each advertising content.
     */
    @Inject
    public AdvertisementSelectionLogic(ReadableDao<String, List<AdvertisementContent>> contentDao,
                                       ReadableDao<String, List<TargetingGroup>> targetingGroupDao) {
        this.contentDao = contentDao;
        this.targetingGroupDao = targetingGroupDao;
    }

    /**
     * Setter for Random class.
     * @param random generates random number used to select advertisements.
     */
    public void setRandom(Random random) {
        this.random = random;
    }

    /**
     * Gets all of the content and metadata for the marketplace and determines which content can be shown.  Returns the
     * eligible content with the highest click through rate.  If no advertisement is available or eligible, returns an
     * EmptyGeneratedAdvertisement.
     *
     * @param customerId - the customer to generate a custom advertisement for
     * @param marketplaceId - the id of the marketplace the advertisement will be rendered on
     * @return an advertisement customized for the customer id provided, or an empty advertisement if one could
     *     not be generated.
     */

//    public GeneratedAdvertisement selectAdvertisement(String customerId, String marketplaceId) {
//        GeneratedAdvertisement generatedAdvertisement = new EmptyGeneratedAdvertisement();
//        if (StringUtils.isEmpty(marketplaceId)) {
//            LOG.warn("MarketplaceId cannot be null or empty. Returning empty ad.");
//        } else {
//            List<AdvertisementContent> contents = contentDao.get(marketplaceId);
//            // start new code
//            TargetingEvaluator evaluator = new TargetingEvaluator(new RequestContext(customerId, marketplaceId));
//            Optional<AdvertisementContent> first = contents.stream()
//                    .filter(advertisementContent -> {
//                        List<TargetingGroup> groups = targetingGroupDao.get(advertisementContent.getContentId());
//                        for (TargetingGroup group : groups) {
//                            if (evaluator.evaluate(group).isTrue()) return true;
//                        }
//                        return false;
//                    })
//                    .findFirst();
//            // end new code
//            if (first.isPresent()) {
//                AdvertisementContent randomAdvertisementContent = first.get();
//                generatedAdvertisement = new GeneratedAdvertisement(randomAdvertisementContent);
//            }
//
//        }
//
//        return generatedAdvertisement;
//    }

    public GeneratedAdvertisement selectAdvertisement(String customerId, String marketplaceId) {
        GeneratedAdvertisement generatedAdvertisement = new EmptyGeneratedAdvertisement();
        if (StringUtils.isEmpty(marketplaceId)) {
            LOG.warn("MarketplaceId cannot be null or empty. Returning empty ad.");
        } else {
            List<AdvertisementContent> contents = contentDao.get(marketplaceId);
            SortedMap<TargetingGroup, AdvertisementContent> targetMap = new TreeMap<>(new TargetingGroupComparator());
            TargetingEvaluator evaluator = new TargetingEvaluator(new RequestContext(customerId, marketplaceId));
            contents.forEach(advertisementContent -> {
                List<TargetingGroup> groups = targetingGroupDao.get(advertisementContent.getContentId());
                        TargetingGroup highest = new TargetingGroup();
                        highest.setClickThroughRate(-1.0);
                        for (TargetingGroup group : groups) {
                            if (evaluator.evaluate(group).isTrue() &&
                                    group.getClickThroughRate() > highest.getClickThroughRate()) {
                                highest = group;
                            }
                        }
                if (highest.getContentId() != null && !highest.getContentId().isEmpty()) {
                    targetMap.put(highest, advertisementContent);
                }
            });
            if (!targetMap.isEmpty()) {
                TargetingGroup bestTarget = targetMap.lastKey();
                AdvertisementContent bestAd = targetMap.get(bestTarget);
                generatedAdvertisement = new GeneratedAdvertisement(bestAd);
            }

        }

        return generatedAdvertisement;
    }
    private class TargetingGroupComparator implements Comparator<TargetingGroup> {

        @Override
        public int compare(TargetingGroup o1, TargetingGroup o2) {
            double result = o1.getClickThroughRate() - o2.getClickThroughRate();
            if (result > 0) {
                return 1;
            } else if(result < 0) {
                return -1;
            }
            return 0;
        }
    }
}

