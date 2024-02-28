package com.uefa.platform.service.b2bpush.core.domain.feed.data.entity;

import org.springframework.data.mongodb.core.query.Update;

public class BaseUpdateBuilder {

    protected final Update update;

    protected BaseUpdateBuilder() {
        update = new Update();
    }

    public Update build() {
        return update;
    }

    protected BaseUpdateBuilder setField(String fieldName, Object data) {
        if (data != null) {
            update.set(fieldName, data);
        } else {
            update.unset(fieldName);
        }
        return this;
    }
}
