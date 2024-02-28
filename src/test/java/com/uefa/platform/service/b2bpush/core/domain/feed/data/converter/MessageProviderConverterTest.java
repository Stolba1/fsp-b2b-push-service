package com.uefa.platform.service.b2bpush.core.domain.feed.data.converter;

import com.uefa.platform.dto.message.PlatformMessage;
import com.uefa.platform.dto.message.PlatformMessage.Provider;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.entity.MessageArchiveEntity;
import com.uefa.platform.service.b2bpush.core.domain.archive.data.entity.MessageArchiveEntity.MessageProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MessageProviderConverterTest {

    @Test
    void testConvert() {
        MessageArchiveEntity.MessageProvider result = MessageProviderConverter.convertMessageProvider(PlatformMessage.Provider.COMPETITION_STATISTICS_SERVICE);
        Assertions.assertEquals(MessageArchiveEntity.MessageProvider.COMPETITION_STATISTICS_SERVICE, result);
    }

    @Test
    void testConvertMatchStats() {
        MessageArchiveEntity.MessageProvider result = MessageProviderConverter.convertMessageProvider(PlatformMessage.Provider.MATCH_STATISTICS_SERVICE);
        Assertions.assertEquals(MessageArchiveEntity.MessageProvider.MATCH_STATISTICS_SERVICE, result);
    }

    @Test
    void testConvertMatchState() {
        MessageArchiveEntity.MessageProvider result = MessageProviderConverter.convertMessageProvider(PlatformMessage.Provider.MATCH_STATE_PROVIDER);
        Assertions.assertEquals(MessageArchiveEntity.MessageProvider.MATCH_STATE_PROVIDER, result);
    }

    @Test
    void testConvertTranslations() {
        MessageArchiveEntity.MessageProvider result = MessageProviderConverter.convertMessageProvider(Provider.TRANSLATION_SERVICE);
        Assertions.assertEquals(MessageProvider.TRANSLATION_SERVICE, result);
    }


}
