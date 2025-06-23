package kr.mywork.infrastructure.project_checklist.rdb;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.mywork.domain.project_checklist.model.ProjectCheckList;
import kr.mywork.domain.project_checklist.repository.ProjectCheckListRepository;
import kr.mywork.domain.project_checklist.service.dto.request.ProjectCheckListCreateRequest;
import kr.mywork.domain.project_checklist.service.dto.response.ProjectCheckListSelectResponse;
import kr.mywork.domain.project_checklist.service.dto.response.ProjectStepCheckListCountResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.querydsl.core.types.ExpressionUtils.count;
import static kr.mywork.domain.project_checklist.model.QProjectCheckList.projectCheckList;
import static kr.mywork.domain.project_step.model.QProjectStep.projectStep;

@Repository
@RequiredArgsConstructor
public class QueryDslProjectCheckListRepository implements ProjectCheckListRepository {

	private final JpaProjectCheckListRepository projectCheckListRepository;
	private final JPAQueryFactory queryFactory;

	@Override
	public ProjectCheckList save(ProjectCheckListCreateRequest projectCheckListCreateRequest) {

		return projectCheckListRepository.save(projectCheckListCreateRequest.toEntity());
	}

	@Override
	public Optional<ProjectCheckList> findById(UUID checkListId) {
		return projectCheckListRepository.findById(checkListId);
	}

	@Override
	public List<ProjectStepCheckListCountResponse> findProgressCountGroupByProjectStepIdAndApproval(
		final Collection<UUID> projectStepIds, final String approval) {
		return queryFactory.select(Projections.constructor(ProjectStepCheckListCountResponse.class,
				projectCheckList.projectStepId,
				count(projectCheckList.id),
				projectStep.orderNum))
			.from(projectCheckList)
			.join(projectStep).on(projectCheckList.projectStepId.eq(projectStep.id))
			.where(
				projectCheckList.deleted.isFalse(),
				eqApproval(approval),
				projectCheckList.projectStepId.in(projectStepIds))
			.groupBy(projectCheckList.projectStepId, projectStep.orderNum)
			.orderBy(projectStep.orderNum.asc())
			.fetch();
	}

	@Override
	public List<ProjectCheckListSelectResponse> findAllByProjectIdAndStepId(final UUID projectId, final UUID projectStepId) {
		return queryFactory.select(Projections.constructor(ProjectCheckListSelectResponse.class,
				projectCheckList.title,
				projectCheckList.approval,
				projectStep.title,
				projectCheckList.createdAt))
			.from(projectCheckList)
			.join(projectStep).on(projectCheckList.projectStepId.eq(projectStep.id))
			.where(projectStep.projectId.eq(projectId), eqProjectStepId(projectStepId))
			.fetch();
	}

	@Override
	public List<ProjectCheckList> findAllByProjectStepIds(List<UUID> projectStepIds, String approval, int page, int pageSize, LocalDateTime fiveDaysAgo) {

		final int offset = (page - 1) * pageSize;

		return queryFactory
				.selectFrom(projectCheckList)
				.where(
						projectCheckList.projectStepId.in(projectStepIds),
						projectCheckList.approval.eq(approval),
						projectCheckList.createdAt.goe(fiveDaysAgo)
				)
				.offset(offset)
				.limit(pageSize)
				.fetch();
	}

	private BooleanExpression eqProjectStepId(final UUID projectStepId) {
		if (projectStepId == null) {
			return null;
		}

		return projectStep.id.eq(projectStepId);
	}

	private BooleanExpression eqApproval(final String approval) {
		if (approval == null) {
			return null;
		}
		return projectCheckList.approval.eq(approval);
	}
}
