package com.rbc.holidays.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RequestContextHolderTest {

    @AfterEach
    void tearDown() {
        RequestContextHolder.clear();
    }

    @Test
    void setGetAndClearShouldManageThreadLocalContext() {
        RequestContext context = RequestContext.of("123e4567-e89b-12d3-a456-426614174000", "qa-user", "/api/v1/holidays");

        RequestContextHolder.set(context);

        assertThat(RequestContextHolder.get()).isEqualTo(context);
        assertThat(RequestContextHolder.getCorrelationIdOrDefault()).isEqualTo("123e4567-e89b-12d3-a456-426614174000");

        RequestContextHolder.clear();

        assertThat(RequestContextHolder.get()).isNull();
        assertThat(RequestContextHolder.getCorrelationIdOrDefault()).isEqualTo("UNKNOWN");
    }

    @Test
    void setShouldRejectNullContext() {
        assertThatThrownBy(() -> RequestContextHolder.set(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("RequestContext cannot be null");
    }

    @Test
    void constructorShouldPreventInstantiation() throws Exception {
        Constructor<RequestContextHolder> constructor = RequestContextHolder.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        assertThatThrownBy(constructor::newInstance)
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(UnsupportedOperationException.class)
                .hasRootCauseMessage("Utility class - do not instantiate");
    }
}