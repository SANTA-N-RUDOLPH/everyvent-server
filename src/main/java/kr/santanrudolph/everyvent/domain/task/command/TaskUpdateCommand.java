package kr.santanrudolph.everyvent.domain.task.command;

public record TaskUpdateCommand(
    String name,
    Integer day
) {}
