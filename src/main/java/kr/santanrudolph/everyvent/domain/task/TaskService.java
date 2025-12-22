package kr.santanrudolph.everyvent.domain.task;


import kr.santanrudolph.everyvent.domain.calendar.Calendar;
import kr.santanrudolph.everyvent.domain.calendar.service.CalendarPolicy;
import kr.santanrudolph.everyvent.domain.calendar.service.CalendarService;
import kr.santanrudolph.everyvent.domain.task.dto.TaskCreateRequest;
import kr.santanrudolph.everyvent.domain.task.dto.TaskResponse;
import kr.santanrudolph.everyvent.domain.task.dto.TaskUpdateRequest;
import kr.santanrudolph.everyvent.domain.user.User;
import kr.santanrudolph.everyvent.domain.user.UserService;
import kr.santanrudolph.everyvent.global.exception.ErrorCode;
import kr.santanrudolph.everyvent.global.exception.EveryventException;
import kr.santanrudolph.everyvent.global.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {

  private static final int MAX_TASKS_PER_DAY = 3;
  private static final boolean UNLOCKED = false;

  private final TaskRepository taskRepository;
  private final CalendarService calendarService;
  private final UserService userService;
  private final CalendarPolicy calendarPolicy;

  @Transactional
  public List<TaskResponse> createTasks(Long calendarId, List<TaskCreateRequest> requests) {
    User currentUser = userService.getCurrentUser();
    Calendar calendar = calendarService.findCalendarByIdAndDeletedAtIsNull(calendarId);

    try {
      calendarPolicy.validateCanUpdate(currentUser, calendar);
    } catch (EveryventException e) {
      throw new EveryventException(e.getErrorCode(), "태스크를 생성할 수 없습니다. " + e.getMessage());
    }

    List<Task> tasks = requestsToTasks(calendar, requests);
    List<Task> results = taskRepository.saveAll(tasks);

    return results.stream()
        .map(task -> TaskResponse.from(task, UNLOCKED))
        .toList();
  }

  public List<TaskResponse> getTasksAllInformation(Long calendarId) {
    User currentUser = userService.getCurrentUser();
    Calendar calendar = calendarService.findCalendarByIdAndDeletedAtIsNull(calendarId);

    try {
      calendarPolicy.validateCanView(currentUser, calendar);
    } catch (EveryventException e) {
      throw new EveryventException(e.getErrorCode(), "태스크를 조회할 수 없습니다. " + e.getMessage());
    }

    List<Task> tasks = taskRepository.findAllByCalendarId(calendarId);

    return tasks.stream()
        .map(task -> TaskResponse.from(task, UNLOCKED))
        .toList();
  }


  public List<TaskResponse> getTasks(Long calendarId) {
    User currentUser = userService.getCurrentUser();
    Calendar calendar = calendarService.findCalendarByIdAndDeletedAtIsNull(calendarId);

    try {
      calendarPolicy.validateCanView(currentUser, calendar);
    } catch (EveryventException e) {
      throw new EveryventException(e.getErrorCode(), "태스크를 조회할 수 없습니다. " + e.getMessage());
    }

    List<Task> tasks = taskRepository.findAllByCalendarId(calendarId);

    return tasks.stream()
        .map(task -> TaskResponse.from(task, isLocked(calendar, task)))
        .toList();
  }

  @Transactional
  public TaskResponse updateTask(Long calendarId, Long taskId, TaskUpdateRequest request) {
    User currentUser = userService.getCurrentUser();
    Calendar calendar = calendarService.findCalendarByIdAndDeletedAtIsNull(calendarId);
    Task task = findTaskById(taskId);

    try {
      calendarPolicy.validateCanUpdate(currentUser, calendar);
      validateTaskBelongsToCalendar(task, calendarId);
    } catch (EveryventException e) {
      throw new EveryventException(e.getErrorCode(), "태스크를 수정할 수 없습니다. " + e.getMessage());
    }

    if (request.content() != null) {
      task.updateContent(request.content());
    }

    return TaskResponse.from(task, UNLOCKED);
  }

  @Transactional
  public void deleteTask(Long calendarId, Long taskId) {
    User currentUser = userService.getCurrentUser();
    Calendar calendar = calendarService.findCalendarByIdAndDeletedAtIsNull(calendarId);
    Task task = findTaskById(taskId);

    try {
      calendarPolicy.validateCanUpdate(currentUser, calendar);
      validateTaskBelongsToCalendar(task, calendarId);
    } catch (EveryventException e) {
      throw new EveryventException(e.getErrorCode(), "태스크를 삭제할 수 없습니다. " + e.getMessage());
    }

    taskRepository.delete(task);
  }

  @Transactional
  public TaskResponse updateTaskCompletion(Long calendarId, Long taskId, Boolean completed) {
    User currentUser = userService.getCurrentUser();
    Calendar calendar = calendarService.findCalendarByIdAndDeletedAtIsNull(calendarId);
    Task task = findTaskById(taskId);

    validateCanComplete(currentUser, calendar, task);

    if (completed) {
      task.complete();
    } else {
      task.uncomplete();
    }

    return TaskResponse.from(task, false);
  }

  private Task findTaskById(Long taskId) {
    return taskRepository.findById(taskId)
        .orElseThrow(() -> new EveryventException(ErrorCode.NOT_FOUND, "태스크를 찾을 수 없습니다."));
  }

  private List<Task> requestsToTasks(Calendar calendar, List<TaskCreateRequest> requests) {
    Map<Integer, Long> requestedTaskCountByDay = requests.stream()
        .collect(Collectors.groupingBy(TaskCreateRequest::day, Collectors.counting()));

    return requests.stream()
        .map(request -> {
          LocalDate taskDay = parseTaskDay(calendar, request);

          validateDateWithinCalendarRange(calendar, taskDay);
          validateTaskLimit(calendar, taskDay, requestedTaskCountByDay.get(request.day()));

          return Task.createTask(calendar, taskDay, request.content());
        })
        .toList();
  }

  private LocalDate parseTaskDay(Calendar calendar, TaskCreateRequest request) {
    LocalDate startDay = calendar.getStartDate();
    try {
      return startDay.withDayOfMonth(request.day());
    } catch (DateTimeException e) {
      throw new EveryventException(
          ErrorCode.INVALID_INPUT,
          String.format("%d월에는 %d일이 존재하지 않습니다.", startDay.getMonthValue(), request.day())
      );
    }
  }

  private void validateTaskLimit(Calendar calendar, LocalDate day, long requestedTaskCountByDay) {
    long calendarId = calendar.getId();
    long count = taskRepository.countByCalendarIdAndDay(calendarId, day);

    if (count + requestedTaskCountByDay > MAX_TASKS_PER_DAY) {
      throw new EveryventException(
          ErrorCode.INVALID_INPUT,
          String.format("하루에 최대 %d개의 태스크만 생성할 수 있습니다.", MAX_TASKS_PER_DAY)
      );
    }
  }

  private void validateDateWithinCalendarRange(Calendar calendar, LocalDate day) {
    if (day.isBefore(calendar.getStartDate()) || day.isAfter(calendar.getEndDate())) {
      throw new EveryventException(
          ErrorCode.INVALID_INPUT, "태스크 날짜는 캘린더 기간 내에 있어야 합니다."
      );
    }
  }

  private void validateTaskBelongsToCalendar(Task task, Long calendarId) {
    if (!task.getCalendar().getId().equals(calendarId)) {
      throw new EveryventException(ErrorCode.INVALID_INPUT, "해당 태스크는 이 캘린더에 속하지 않습니다.");
    }
  }

  private void validateCanComplete(User currentUser, Calendar calendar, Task task) {
    try {
      calendarPolicy.validateCanView(currentUser, calendar);
      validateTaskBelongsToCalendar(task, calendar.getId());
    } catch (EveryventException e) {
      throw new EveryventException(e.getErrorCode(), "태스크 완료 상태를 변경할 수 없습니다. " + e.getMessage());
    }

    if (isFutureTask(task)) {
      throw new EveryventException(ErrorCode.FORBIDDEN, "태스크 완료 상태를 변경할 수 없습니다. 미래의 태스크입니다.");
    }
  }

  private boolean isLocked(Calendar calendar, Task task) {
    LocalDate now = TimeUtil.today();

    LocalDate previewStartDay = calendar.getPreviewStartDay();
    LocalDate previewEndDay = calendar.getPreviewEndDay();

    // 미리보기 범위 안에 있으면 잠겨있지 않음
    if (previewStartDay != null && previewEndDay != null) {
      if (TimeUtil.isInRange(previewStartDay, previewEndDay, task.getDay())) {
        return false;
      }
    }

    // 해당 날짜가 미래면 잠겨있음
    return task.getDay().isAfter(now);
  }

  private boolean isFutureTask(Task task) {
    return TimeUtil.isAfter(task.getDay(), TimeUtil.today());
  }
}
