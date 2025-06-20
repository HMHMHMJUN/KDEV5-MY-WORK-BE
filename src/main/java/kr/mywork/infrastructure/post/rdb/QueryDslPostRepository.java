package kr.mywork.infrastructure.post.rdb;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.mywork.domain.post.model.Post;
import kr.mywork.domain.post.repository.PostRepository;
import kr.mywork.domain.post.service.dto.request.PostCreateRequest;
import kr.mywork.domain.post.service.dto.response.PostSelectResponse;
import kr.mywork.domain.project.service.dto.response.DashboardMostPostProjectResponse;
import kr.mywork.domain.project_step.model.ProjectStep;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static kr.mywork.domain.post.model.QPost.post;
import static kr.mywork.domain.project_step.model.QProjectStep.projectStep;

@Repository
@RequiredArgsConstructor
public class QueryDslPostRepository implements PostRepository {

	private final JpaPostRepository postRepository;
	private final JPAQueryFactory jpaQueryFactory;
	private final JPAQueryFactory queryFactory;

	@Override
	public Post save(final PostCreateRequest postCreateRequest) {
		return postRepository.save(postCreateRequest.toEntity());
	}

	@Override
	public Optional<Post> findById(UUID postId) {
		return postRepository.findById(postId);
	}

	public List<PostSelectResponse> findPostsByProjectStepSearchConditionWithPaging(int page, int postPageSize,
		UUID projectStepId, String projectStepTitle,
		String keyword, Boolean deleted, UUID projectId, String keywordType, String approval) {

		final int offset = (page - 1) * postPageSize;

		// post 테이블에서 projectStepId에 해당하는 모든 게시글 조회
		List<PostSelectResponse> posts = jpaQueryFactory
			.select(Projections.constructor(PostSelectResponse.class,
				post.id,
				post.createdAt,
				post.authorName,
				post.title,
				post.approval
			))
			.from(post)
			.where(
				post.projectStepId.eq(projectStepId),
				eqDeleted(deleted),
				containsKeyword(keywordType, keyword),
				eqApproval(approval)
			)
			.orderBy(post.createdAt.desc())
			.offset(offset)
			.limit(postPageSize)
			.fetch();

		// 조회 결과 각 객체에 프로젝트 단계명 추가
		posts.forEach(postResponse -> postResponse.assignProjectStepName(projectStepTitle));

		return posts;
	}

	// 프로젝트의 전체 게시물 조회
	public List<PostSelectResponse> findPostsByProjectSearchConditionWithPaging(int page, int postPageSize,
		String keyword,
		Boolean deleted, UUID projectId, String keywordType, String approval) {

		final int offset = (page - 1) * postPageSize;

		// post와 project_step을 JOIN하여 한 번의 쿼리로 게시글과 단계명을 함께 조회
		return jpaQueryFactory
			.select(Projections.constructor(PostSelectResponse.class,
				post.id,
				post.createdAt,
				post.authorName,
				post.title,
				post.approval,
				projectStep.title.as("projectStepTitle") // 프로젝트 단계명 추가
			))
			.from(post)
			.join(projectStep).on(post.projectStepId.eq(projectStep.id))
			.where(
				projectStep.projectId.eq(projectId), // 특정 프로젝트의 단계들만 필터링
				eqDeleted(deleted),
				containsKeyword(keywordType, keyword),
				eqApproval(approval)
			)
			.orderBy(post.createdAt.desc())
			.offset(offset)
			.limit(postPageSize)
			.fetch();
	}

	private BooleanExpression containsKeyword(final String searchType, final String keyword) {
		if (searchType == null) {
			return null;
		}

		if (keyword == null || keyword.isEmpty()) {
			return null;
		}

		return switch (searchType) {
			case "AUTHORNAME" -> post.authorName.containsIgnoreCase(keyword);
			case "TITLE" -> post.title.containsIgnoreCase(keyword);
			default -> null;
		};
	}

	@Override
	public Long countTotalPostsByProjectStepCondition(UUID projectStepId, String keyword, Boolean deleted,
		UUID projectId,
		String keywordType, String approval) {
		return jpaQueryFactory.select(post.id.count())
			.from(post)
			.where(
				eqProjectStepId(projectStepId),
				eqDeleted(deleted),
				containsKeyword(keywordType, keyword),
				eqApproval(approval)
			)
			.fetchOne();
	}

	@Override
	public List<DashboardMostPostProjectResponse> findMostPostProjectTopFive(@Nullable List<UUID> limitedProjectIds) {

		return queryFactory
			.select(Projections.constructor(DashboardMostPostProjectResponse.class,
				projectStep.projectId,
				post.id.count()
				))
			.from(post)
			.join(projectStep).on(post.projectStepId.eq(projectStep.id))
			.where(
					post.deleted.eq(false),
					isInLimitedProjects(limitedProjectIds)
			)
			.groupBy(projectStep.projectId)
			.orderBy(post.id.count().desc())
			.limit(5)
			.fetch();
	}

	private BooleanExpression isInLimitedProjects(@Nullable List<UUID> limitedProjectIds) {
		return (limitedProjectIds != null && !limitedProjectIds.isEmpty()) ? projectStep.projectId.in(limitedProjectIds) : null;
	}

	@Override
	public Long countTotalPostsByProjectCondition(List<ProjectStep> projectSteps, String keyword, Boolean deleted,
		UUID projectId,
		String keywordType, String approval) {
		return jpaQueryFactory.select(post.id.count())
			.from(post)
			.where(
				inProjectStepIds(projectSteps),  // 추가
				eqDeleted(deleted),
				containsKeyword(keywordType, keyword),
				eqApproval(approval)
			)
			.fetchOne();
	}

	// 동적 조건 메서드 추가
	private BooleanExpression inProjectStepIds(List<ProjectStep> projectSteps) {
		if (projectSteps == null || projectSteps.isEmpty()) {
			return null;
		}

		List<UUID> projectStepIds = projectSteps.stream()
			.map(ProjectStep::getId)
			.toList();

		return post.projectStepId.in(projectStepIds);
	}

	private BooleanExpression eqProjectStepId(UUID projectStepId) {
		if (projectStepId == null) {
			return null;
		}

		return post.projectStepId.eq(projectStepId);
	}

	private BooleanExpression eqKeyword(String keyword) {
		if (keyword == null) {
			return null;
		}

		return post.title.like(keyword + "%");
	}

	private BooleanExpression eqDeleted(Boolean deleted) {
		if (deleted == null) {
			return null;
		}

		return post.deleted.eq(deleted);
	}

	private BooleanExpression eqApproval(String approval) {
		if (approval == null) {
			return null;
		}

		return post.approval.eq(approval);
	}
}
