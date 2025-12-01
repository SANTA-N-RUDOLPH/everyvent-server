package kr.santanrudolph.everyvent.domain.task;

import kr.santanrudolph.everyvent.domain.calendar.CalendarRepository;
import kr.santanrudolph.everyvent.domain.calendar.OfficialCalendarRepository;
import kr.santanrudolph.everyvent.domain.calendar.entity.Calendar;
import kr.santanrudolph.everyvent.domain.calendar.entity.OfficialCalendar;
import kr.santanrudolph.everyvent.domain.task.command.TaskUpdateCommand;
import kr.santanrudolph.everyvent.domain.task.dto.request.TaskCreateRequest;
import kr.santanrudolph.everyvent.domain.task.dto.response.TaskResponse;
import kr.santanrudolph.everyvent.domain.task.dto.response.TaskResponses;
import kr.santanrudolph.everyvent.domain.user.User;
import kr.santanrudolph.everyvent.domain.user.UserRepository;
import kr.santanrudolph.everyvent.domain.user.enums.Role;
import kr.santanrudolph.everyvent.global.exception.ErrorCode;
import kr.santanrudolph.everyvent.global.exception.EveryventException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {

  private final TaskRepository taskRepository;
  private final CalendarRepository calendarRepository;
  private final OfficialCalendarRepository officialCalendarRepository;
  private final UserRepository userRepository;

  @Transactional
  public TaskResponses createTask(Long userId, TaskCreateRequest request) {
    Calendar calendar = getActiveCalendarOrThrow(request.calendarId());

    if (isPastOrSameStartMonth(calendar)) {
      throw new EveryventException(ErrorCode.FORBIDDEN, "해당 캘린더의 월이 시작되면 일반 태스크는 생성할 수 없습니다.");
    }

    User user = getUserOrThrow(userId);
    validateOwnerPermission(calendar, user);

    validateDailyTaskLimit(calendar, request.startDate(), request.endDate());

    List<Task> tasks = createTaskEntities(calendar, request);
    taskRepository.saveAll(tasks);

    return TaskResponses.from(tasks);
  }

  @Transactional
  public TaskResponses createOfficialTask(Long userId, TaskCreateRequest request) {
    Calendar calendar = getActiveCalendarOrThrow(request.calendarId());
    if (!isOfficialCalendar(calendar)) {
      throw new EveryventException(ErrorCode.NOT_FOUND, "해당 캘린더는 공식 캘린더가 아닙니다.");
    }

    if (isPastStartMonth(calendar)) {
      throw new EveryventException(ErrorCode.FORBIDDEN, "해당 캘린더의 월까지만 공식 태스크를 생성할 수 있습니다.");
    }

    User user = getUserOrThrow(userId);
    validateAdminPermission(user);

    validateDailyTaskLimit(calendar, request.startDate(), request.endDate());

    List<Task> tasks = createTaskEntities(calendar, request);

    taskRepository.saveAll(tasks);

    return TaskResponses.from(tasks);
  }

  public TaskResponse getTask(Long userId, Long taskId) {
    Task task = getTaskOrThrow(taskId);

    Calendar calendar = task.getCalendar();
    if (isOfficialCalendar(calendar)) {
      throw new EveryventException(ErrorCode.FORBIDDEN, "공식 캘린더 태스크는 일반 조회 API에서 조회할 수 없습니다.");
    }

    User user = getUserOrThrow(userId);
    validateOwnerPermission(calendar, user);

    return TaskResponse.from(task);
  }

  public TaskResponse getOfficialTask(Long userId, Long taskId) {
    Task task = getTaskOrThrow(taskId);

    if (!isOfficialCalendar(task.getCalendar())) {
      throw new EveryventException(ErrorCode.FORBIDDEN, "일반 캘린더 태스크는 공식 조회 API에서 조회할 수 없습니다.");
    }

    User user = getUserOrThrow(userId);
    validateAdminPermission(user);

    return TaskResponse.from(task);
  }

  @Transactional
  public TaskResponse updateTaskDetails(Long userId, Long taskId, TaskUpdateCommand command) {
    Task task = getTaskOrThrow(taskId);

    if (isOfficialCalendar(task.getCalendar())) {
      throw new EveryventException(ErrorCode.FORBIDDEN, "공식 캘린더 태스크는 일반 수정 API에서 수정할 수 없습니다.");
    }

    if (isPastOrSameStartMonth(task.getCalendar())) {
      throw new EveryventException(ErrorCode.FORBIDDEN, "해당 캘린더의 월이 시작되면 일반 태스크는 수정할 수 없습니다.");
    }

    User user = getUserOrThrow(userId);
    validateOwnerPermission(task.getCalendar(), user);

    updateTaskFields(task, command);

    return TaskResponse.from(task);
  }

  @Transactional
  public TaskResponse updateOfficialTaskDetails(Long userId, Long taskId, TaskUpdateCommand command) {
    Task task = getTaskOrThrow(taskId);

    if (!isOfficialCalendar(task.getCalendar())) {
      throw new EveryventException(ErrorCode.FORBIDDEN, "일반 캘린더 태스크는 공식 수정 API에서 수정할 수 없습니다.");
    }

    if (isPastStartMonth(task.getCalendar())) {
      throw new EveryventException(ErrorCode.FORBIDDEN, "해당 캘린더의 월까지만 공식 태스크를 수정할 수 있습니다.");
    }

    User user = getUserOrThrow(userId);
    validateAdminPermission(user);

    updateTaskFields(task, command);

    return TaskResponse.from(task);
  }

  @Transactional
  public TaskResponse updateTaskCompletion(Long userId, Long taskId, boolean isCompleted) {
    Task task = getTaskOrThrow(taskId);

    User user = getUserOrThrow(userId);
    validateOwnerPermission(task.getCalendar(), user);

    if (isCompleted) {
      task.markCompleted();
    } else {
      task.unmarkCompleted();
    }

    return TaskResponse.from(task);
  }

  @Transactional
  public void deleteTask(Long userId, Long taskId) {
    Task task = getTaskOrThrow(taskId);

    Calendar calendar = task.getCalendar();
    if (isOfficialCalendar(calendar)) {
      throw new EveryventException(ErrorCode.FORBIDDEN, "공식 캘린더 태스크는 일반 삭제 API에서 삭제할 수 없습니다.");
    }

    if (isPastOrSameStartMonth(task.getCalendar())) {
      throw new EveryventException(ErrorCode.FORBIDDEN, "해당 캘린더의 월이 시작되면 일반 태스크는 삭제할 수 없습니다.");
    }

    User user = getUserOrThrow(userId);
    validateOwnerPermission(calendar, user);

    task.softDelete();
  }

  @Transactional
  public void deleteOfficialTask(Long userId, Long taskId) {
    Task task = getTaskOrThrow(taskId);

    if (!isOfficialCalendar(task.getCalendar())) {
      throw new EveryventException(ErrorCode.FORBIDDEN, "일반 캘린더 태스크는 공식 삭제 API에서 삭제할 수 없습니다.");
    }

    User user = getUserOrThrow(userId);
    validateAdminPermission(user);

    task.softDelete();
  }

  // ==================== Private Helper Methods ====================
  // 엔티티/객체 관련
  private User getUserOrThrow(Long userId) {
    return userRepository.findByIdAndDeletedAtIsNull(userId)
            .orElseThrow(() -> new EveryventException(ErrorCode.NOT_FOUND, "해당 사용자를 찾을 수 없습니다."));
  }

  private Calendar getActiveCalendarOrThrow(Long calendarId) {
    Calendar calendar = calendarRepository.findById(calendarId)
            .orElseThrow(() -> new EveryventException(ErrorCode.NOT_FOUND, "해당 캘린더를 찾을 수 없습니다."));

    if (calendar.getDeletedAt() != null) {
      throw new EveryventException(ErrorCode.FORBIDDEN, "이미 삭제된 캘린더입니다.");
    }

    return calendar;
  }

  private Task getTaskOrThrow(Long taskId) {
    return taskRepository.findByIdAndDeletedAtIsNull(taskId)
            .orElseThrow(() -> new EveryventException(ErrorCode.NOT_FOUND, "태스크를 찾을 수 없습니다."));
  }

  // 권한/검증 관련
  private void validateOwnerPermission(Calendar calendar, User user) {
    if (!calendar.getUser().equals(user)) {
      throw new EveryventException(ErrorCode.FORBIDDEN, "해당 캘린더에 접근할 권한이 없습니다.");
    }
  }

  private void validateAdminPermission(User user) {
    if (user.getRole() != Role.ADMIN) {
      throw new EveryventException(ErrorCode.FORBIDDEN, "공식 캘린더에 접근할 권한이 없습니다.");
    }
  }

  private void validateTaskDateRange(Calendar calendar, LocalDate start, LocalDate end) {
    if (end.isBefore(start)) {
      throw new EveryventException(ErrorCode.INVALID_INPUT, "종료일은 시작일과 같거나 이후의 날짜여야 합니다.");
    }

    LocalDate calendarStart = calendar.getStartDate();
    LocalDate calendarEnd = calendar.getEndDate();

    if (start.isBefore(calendarStart) || start.isAfter(calendarEnd)) {
      throw new EveryventException(ErrorCode.INVALID_INPUT, "태스크 시작일은 캘린더 기간 안에 있어야 합니다.");
    }
    if (end.isBefore(calendarStart) || end.isAfter(calendarEnd)) {
      throw new EveryventException(ErrorCode.INVALID_INPUT, "태스크 종료일은 캘린더 기간 안에 있어야 합니다.");
    }
  }

  private void validateDailyTaskLimit(Calendar calendar, LocalDate startDate, LocalDate endDate) {

    int start = startDate.getDayOfMonth();
    int end = endDate.getDayOfMonth();

    for (int day = start; day <= end; day++) {
      int count = taskRepository.countByCalendarIdAndDay(calendar.getId(), day);

      if (count >= 3) {
        throw new EveryventException(ErrorCode.FORBIDDEN, "태스크는 각 일자에 최대 3개까지 생성할 수 있습니다.");
      }
    }
  }

  private boolean isOfficialCalendar(Calendar calendar) {
    OfficialCalendar officialCalendar = officialCalendarRepository
            .findByOriginalCalendarId(calendar.getId())
            .orElse(null);

    if (officialCalendar != null && officialCalendar.getDeletedAt() != null) {
      throw new EveryventException(ErrorCode.FORBIDDEN, "삭제된 원본 캘린더엔 더 이상 접근할 수 없습니다.");
    }

    return officialCalendar != null;
  }

  private boolean isPastOrSameStartMonth(Calendar calendar) {
    YearMonth now = YearMonth.now();
    YearMonth startMonth = YearMonth.from(calendar.getStartDate());
    return !now.isBefore(startMonth);
  }

  private boolean isPastStartMonth(Calendar calendar) {
    YearMonth now = YearMonth.now();
    YearMonth startMonth = YearMonth.from(calendar.getStartDate());
    return now.isAfter(startMonth);
  }

  // 생성/업데이트 관련
  private List<Task> createTaskEntities(Calendar calendar, TaskCreateRequest request) {
    LocalDate start = request.startDate();
    LocalDate end = request.endDate();
    validateTaskDateRange(calendar, start, end);

    List<Task> tasks = new ArrayList<>();

    for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
      int day = date.getDayOfMonth();
      Task task = Task.create(calendar, request.name(), day);
      tasks.add(task);
    }

    return tasks;
  }

  private void updateTaskFields(Task task, TaskUpdateCommand command) {
    if (command.name() != null && !command.name().isBlank()) {
      task.updateName(command.name());
    }
    if (command.day() != null) {
      task.updateDay(command.day());
    }
  }

}
