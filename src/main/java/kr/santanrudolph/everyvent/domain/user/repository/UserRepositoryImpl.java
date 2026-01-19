package kr.santanrudolph.everyvent.domain.user.repository;


import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.santanrudolph.everyvent.domain.follow.QFollow;
import kr.santanrudolph.everyvent.domain.user.QUser;
import kr.santanrudolph.everyvent.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static kr.santanrudolph.everyvent.domain.user.QUser.user;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {

  private final JPAQueryFactory queryFactory;


  // 나랑 관계 있는 사람 검색 ->
  @Override
  public List<User> searchRelatedUsers(Long currentUserId, String keyword, Pageable pageable) {
    QFollow f1 = new QFollow("f1");  // 내가 팔로우한 사람
    QFollow f2 = new QFollow("f2");  // 나를 팔로우한 사람

    String lowerKeyword = keyword.toLowerCase();

    // CASE 문으로 정렬 순서 계산
    NumberExpression<Integer> relationOrder = new CaseBuilder()
        .when(f1.id.isNotNull().and(f2.id.isNotNull())).then(1)  // 맞팔
        .when(f1.id.isNotNull()).then(2)                          // 내가 팔로우
        .otherwise(4);

    return queryFactory
        .select(user)
        .from(user)
        .leftJoin(f1).on(f1.follower.id.eq(currentUserId).and(f1.target.id.eq(user.id)))
        .leftJoin(f2).on(f2.follower.id.eq(user.id).and(f2.target.id.eq(currentUserId)))
        .where(
            user.deletedAt.isNull(),
            user.id.ne(currentUserId),
            f1.id.isNotNull().or(f2.id.isNotNull()),  // 팔로우 관계 존재
            keywordMatch(lowerKeyword)
        )
        .orderBy(relationOrder.asc(), user.nickname.asc())
        .limit(pageable.getPageSize())
        .offset(pageable.getOffset())
        .fetch();
  }

  @Override
  public List<User> searchUnrelatedUsers(Long currentUserId, String keyword, Pageable pageable) {
    String lowerKeyword = keyword.toLowerCase();

    return queryFactory
        .selectFrom(user)
        .where(
            user.deletedAt.isNull(),
            user.id.ne(currentUserId),
            notFollowingOrFollower(currentUserId),
            keywordMatch(lowerKeyword)
        )
        .orderBy(user.nickname.asc())
        .limit(pageable.getPageSize())
        .offset(pageable.getOffset())
        .fetch();
  }

  // findCommonFollowersBatch는 UserRepository의 Native Query 버전 사용 (Window Function으로 LIMIT 2 적용)

  @Override
  public List<CommonFollowerCountProjection> countCommonFollowersBatch(Long currentUserId, List<Long> targetUserIds) {
    QUser targetUser = new QUser("targetUser");
    QUser commonFollower = new QUser("commonFollower");
    QFollow f1 = new QFollow("f1");
    QFollow f2 = new QFollow("f2");

    return queryFactory
        .select(Projections.fields(
            CommonFollowerCountProjection.class,
            targetUser.id.as("targetUserId"),
            commonFollower.id.countDistinct().as("count")
        ))
        .from(targetUser)
        .join(f1).on(f1.target.id.eq(targetUser.id))
        .join(commonFollower).on(commonFollower.id.eq(f1.follower.id))
        .where(
            targetUser.id.in(targetUserIds),
            commonFollower.id.in(
                JPAExpressions
                    .select(f2.target.id)
                    .from(f2)
                    .where(
                        f2.follower.id.eq(currentUserId),
                        f2.target.deletedAt.isNull()
                    )
            ),
            commonFollower.deletedAt.isNull()
        )
        .groupBy(targetUser.id)
        .fetch();
  }

  private BooleanExpression keywordMatch(String lowerKeyword) {
    return user.nickname.lower().contains(lowerKeyword);
  }

  private BooleanExpression notFollowingOrFollower(Long currentUserId) {
    QFollow subFollow = new QFollow("subFollow");

    return JPAExpressions
        .selectOne()
        .from(subFollow)
        .where(
            subFollow.follower.id.eq(currentUserId).and(subFollow.target.id.eq(user.id))
                .or(subFollow.follower.id.eq(user.id).and(subFollow.target.id.eq(currentUserId)))
        )
        .notExists();
  }

}
