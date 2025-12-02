package kr.santanrudolph.everyvent.domain.task.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record TaskCreateRequest(
        @NotNull
        Long calendarId,

        @NotBlank
        String name,

        @NotNull
        LocalDate startDate,

        @NotNull
        LocalDate endDate
) {}
