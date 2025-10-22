package kr.santanrudolph.everyvent.domain.calendar;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import kr.santanrudolph.everyvent.domain.user.User;
import kr.santanrudolph.everyvent.global.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
@Table(name = "calendar")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Calendar extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Instant startDate;

    @Column(nullable = false)
    private Instant endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Visibility visibility;

    private static final String HEX_COLOR_PATTERN = "^#([0-9A-Fa-f]{3}|[0-9A-Fa-f]{6})$";

    @Column(nullable = false)
    @Pattern(
            regexp = HEX_COLOR_PATTERN,
            message = "유효한 HEX 색상 코드여야 합니다 (예: #FFF, #FFFFFF)"
    )
    private String color;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column
    private Instant deletedAt;

    protected Calendar(User user, String title, String description, Instant startDate, Instant endDate, Visibility visibility, String color, Category category) {
        this.user = user;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.visibility = visibility;
        this.color = color;
        this.category = category;
    }

    public abstract boolean isScrapable();

    public void softDelete() {
        this.deletedAt = Instant.now();
    }

    // === 속성 변경 메서드 ===
    public void changeColor(String newColor) {
        validateHexColor(newColor);
        this.color = newColor;
    }

    public void changeVisibility(Visibility newVisibility) {
        validateNotNull(newVisibility, "공개 범위");
        this.visibility = newVisibility;
    }

    public void updateDetails(String title, String description, Instant startDate, Instant endDate, Category category) {
        validateNotNull(title, "제목");
        validateDateRange(startDate, endDate);
        validateNotNull(category, "카테고리");

        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.category = category;
    }

    // === 공통 검증 메서드 ===
    protected void validateHexColor(String color) {
        if (!color.matches(HEX_COLOR_PATTERN)) {
            throw new IllegalArgumentException("유효한 HEX 색상 코드여야 합니다");
        }
    }

    protected void validateNotNull(Object value, String fieldName) {
        if (value == null || (value instanceof String s && s.isBlank())) {
            throw new IllegalArgumentException(fieldName + "은(는) 필수값입니다");
        }
    }

    protected void validateDateRange(Instant startDate, Instant endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("시작일은 종료일보다 이전이어야 합니다");
        }
    }
}
