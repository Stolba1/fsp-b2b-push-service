package com.uefa.platform.service.b2bpush.core.domain.feed.data.converter;

import com.uefa.platform.dto.message.PlatformMessage;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.entity.MessageArchiveEntity;

public class MessageProviderConverter {

    private MessageProviderConverter() {
        //hide from instantiation
    }

    public static MessageArchiveEntity.MessageProvider convertMessageProvider(PlatformMessage.Provider messageProvider) {

        return switch (messageProvider) {
            case MATCH_STATISTICS_SERVICE -> MessageArchiveEntity.MessageProvider.MATCH_STATISTICS_SERVICE;
            case COMPETITION_STATISTICS_SERVICE -> MessageArchiveEntity.MessageProvider.COMPETITION_STATISTICS_SERVICE;
            case MATCH_STATE_PROVIDER -> MessageArchiveEntity.MessageProvider.MATCH_STATE_PROVIDER;
            case TRANSLATION_SERVICE -> MessageArchiveEntity.MessageProvider.TRANSLATION_SERVICE;
            default -> null;
        };
    }
}
