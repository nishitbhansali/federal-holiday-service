package com.rbc.holidays;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;

import static org.assertj.core.api.Assertions.assertThat;

class FederalHolidaysApplicationTest {

    @Test
    void constructorShouldCreateApplicationInstance() {
        FederalHolidaysApplication application = new FederalHolidaysApplication();

        assertThat(application).isNotNull();
    }

    @Test
    void mainShouldDelegateToSpringApplicationRun() {
        String[] args = {"--spring.profiles.active=local"};

        try (MockedStatic<SpringApplication> springApplication = Mockito.mockStatic(SpringApplication.class)) {
            FederalHolidaysApplication.main(args);

            springApplication.verify(() -> SpringApplication.run(FederalHolidaysApplication.class, args));
        }
    }
}