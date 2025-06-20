package kr.mywork.infrastructure.project.rdb;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.mywork.domain.member.service.dto.response.MemberProjectInfoResponse;
import kr.mywork.domain.project.model.Project;
import kr.mywork.domain.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;

import static kr.mywork.domain.project.model.QProject.project;
import static kr.mywork.domain.project.model.QProjectMember.projectMember;

@Repository
@RequiredArgsConstructor
@Transactional
public class QueryDslProjectRepository implements ProjectRepository {

	private final JpaProjectRepository projectRepository;
	private final JPAQueryFactory queryFactory;

	@Override
	public Project save(final Project project) {
		return projectRepository.save(project);
	}

	@Override
	public Optional<Project> findById(UUID projectId) {
		return projectRepository.findById(projectId);
	}

	@Override
	public Long countTotalProjectIdsAndStep(final Collection<UUID> projectIds, final String step) {
		return queryFactory
			.select(project.id.count())
			.from(project)
			.where(eqDeleted(false), inProjectIds(projectIds), eqProjectStep(step))
			.fetchOne();
	}

	private BooleanExpression inProjectIds(final Collection<UUID> projectIds) {
		if (projectIds == null) {
			return null;
		}

		return project.id.in(projectIds);
	}

	@Override
	public List<MemberProjectInfoResponse> findeMemberProjectList(UUID memberId) {
		return queryFactory.select(Projections.constructor(
				MemberProjectInfoResponse.class,
				projectMember.projectId,
				project.name
			))
			.from(projectMember)
			.leftJoin(project).on(projectMember.projectId.eq(project.id))
			.where(
				eqMember(memberId),
				eqDeleted(false)
			)
			.fetch();
	}

	@Override
	public List<Project> findAllByStepAndNameWithPaging(final String step, final String projectName,
		final Integer page, final Integer size) {

		final int offset = (page - 1) * size;

		return queryFactory.selectFrom(project)
			.where(eqDeleted(false), eqProjectStep(step), containsProjectName(projectName))
			.limit(size)
			.offset(offset)
			.fetch();
	}

	private BooleanExpression containsProjectName(final String projectName) {
		if (projectName == null) {
			return null;
		}

		return project.name.contains(projectName);
	}

	@Override
	public List<Project> findAllByIdsAndStep(final Collection<UUID> projectIds, final String step, final Integer page,
		final Integer size) {

		final int offset = (page - 1) * size;

		return queryFactory.selectFrom(project)
			.where(eqDeleted(false), inProjectIds(projectIds), eqProjectStep(step))
			.offset(offset)
			.limit(size)
			.fetch();
	}

	@Override
	public Long countTotalProjectsByNameAndStep(final String name, final String step) {
		return queryFactory.select(project.id.count())
			.from(project)
			.where(eqDeleted(false), containsProjectName(name), eqProjectStep(step))
			.fetchOne();
	}

	public List<Project> findAllNearDeadlineProjects(final int page, final int size) {
		final int offset = (page - 1) * size;
		final LocalDate today = LocalDate.now();

		return queryFactory
			.selectFrom(project)
			.where(
				project.deleted.isFalse(),
				project.endAt.between(today.atStartOfDay(), today.plusDays(5).atTime(23, 59, 59))
			)
			.orderBy(project.endAt.asc())
			.offset(offset)
			.limit(size)
			.fetch();
	}

	@Override
	public List<Project> findAllNearDeadlineProjectsByProjectIds(Collection<UUID> projectIds, int page, int pageSize) {
		final int offset = (page - 1) * pageSize;
		return queryFactory
			.selectFrom(project)
			.where(
				project.id.in(projectIds)
					.and(project.deleted.isFalse())
					.and(project.endAt.goe(LocalDateTime.now()))
			)
			.orderBy(project.endAt.asc())
			.offset(offset)
			.limit(pageSize)
			.fetch();
	}

	@Override
	public Long countNearDeadlineProjects() {
		final LocalDate today = LocalDate.now();

		return queryFactory
			.select(project.count())
			.from(project)
			.where(
				project.deleted.isFalse(),
				project.endAt.between(today.atStartOfDay(), today.plusDays(5).atTime(23, 59, 59))
			)
			.fetchOne();
	}


	@Override
	public Long countNearDeadlineProjectsByProjectIds(Collection<UUID> projectIds) {
		if (projectIds == null || projectIds.isEmpty()) {
			return 0L;
		}

		final LocalDate today = LocalDate.now();

		return queryFactory
			.select(project.count())
			.from(project)
			.where(
				project.id.in(projectIds),
				project.deleted.isFalse(),
				project.endAt.between(today.atStartOfDay(), today.plusDays(5).atTime(23, 59, 59))
			)
			.fetchOne();
	}
	public List<Project> findProjectsNameById(List<UUID> mostPostProjectIds) {
		return queryFactory
			.selectFrom(project)
			.where(project.id.in(mostPostProjectIds))
			.fetch();
	}
	private BooleanExpression eqProjectStep(final String step) {
		if (step == null) {
			return null;
		}

		return project.step.eq(step);
	}

	private BooleanExpression eqMember(UUID memberId) {
		if (memberId == null) {
			return null;
		}

		return projectMember.memberId.eq(memberId)
			.and(projectMember.deleted.isFalse());
	}

	private BooleanExpression eqName(String keyword) {
		if (keyword == null || keyword.isEmpty()) {
			return null;
		}
		return project.name.containsIgnoreCase(keyword);
	}

	private BooleanExpression eqDeleted(Boolean deleted) {
		if (deleted == null) {
			return null;
		}
		return project.deleted.eq(deleted);
	}
}
