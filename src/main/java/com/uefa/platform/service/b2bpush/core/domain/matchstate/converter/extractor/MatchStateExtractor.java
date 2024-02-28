package com.uefa.platform.service.b2bpush.core.domain.matchstate.converter.extractor;

import com.uefa.platform.dto.message.matchstate.MatchStateData;
import reactor.core.publisher.Mono;

public interface MatchStateExtractor<T> {
    Mono<T> extract(MatchStateData data);
}
