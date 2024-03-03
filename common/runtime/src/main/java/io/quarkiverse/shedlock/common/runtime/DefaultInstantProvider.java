package io.quarkiverse.shedlock.common.runtime;

import java.time.Instant;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.arc.DefaultBean;

@ApplicationScoped
@DefaultBean
public class DefaultInstantProvider implements InstantProvider {
    @Override
    public Instant now() {
        return Instant.now();
    }
}
