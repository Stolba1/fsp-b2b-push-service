package com.uefa.platform.service.b2bpush.core.domain.feed.data.entity;


public enum LiveFeedDataType {
    MATCH_PLAYER_STATISTICS,
    MATCH_TEAM_STATISTICS,
    COMPETITION_PLAYER_STATISTICS,
    COMPETITION_TEAM_STATISTICS,
    MATCH_STATE,
    TRANSLATIONS,
    PRE_MATCH;

    public static String getRoutingKey(LiveFeedDataType liveFeedDataType) {
        return switch (liveFeedDataType) {
            case MATCH_PLAYER_STATISTICS, MATCH_TEAM_STATISTICS -> "MATCH-STATISTICS";
            case COMPETITION_PLAYER_STATISTICS, COMPETITION_TEAM_STATISTICS -> "COMPETITION-STATISTICS";
            case MATCH_STATE -> "MATCH-STATE-PROVIDER";
            default -> throw new IllegalStateException("The following liveFeedDataType is not handled: " + liveFeedDataType);
        };
    }
}
