package com.uefa.platform.service.b2bpush.core.domain.feed.data.entity;

/**
 * An event package is a definition for a group of events that need to be sent to a certain client.
 */
public enum EventPackage {
    /**
     * Includes everything but the passes.
     */
    BASIC,
    /**
     * Includes all events.
     */
    EXTENDED
}
