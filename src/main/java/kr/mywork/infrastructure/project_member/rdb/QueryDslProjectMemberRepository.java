package kr.mywork.infrastructure.project_member.rdb;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.mywork.domain.project.model.ProjectMember;
import kr.mywork.domain.project_member.repository.ProjectMemberRepository;
import kr.mywork.domain.project_member.service.dto.response.CompanyMemberInProjectResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static kr.mywork.domain.member.model.QMember.member;
import static kr.mywork.domain.project.model.QProjectMember.projectMember;

@Repository
@RequiredArgsConstructor
public class QueryDslProjectMemberRepository implements ProjectMemberRepository {

	final JpaProjectMemberRepository jpaProjectMemberRepository;
	private final JPAQueryFactory queryFactory;

	@Override
	public ProjectMember save(ProjectMember projectMember) {
		return jpaProjectMemberRepository.save(projectMember);
	}

	@Override
	public List<CompanyMemberInProjectResponse> findCompanyMembersInProject(UUID projectId, UUID companyId) {
		return queryFactory
			.select(Projections.constructor(
				CompanyMemberInProjectResponse.class,
				member.id,
				member.name,
				member.email
			))
			.from(projectMember)
			.join(member).on(projectMember.memberId.eq(member.id))
			.where(
				projectMember.projectId.eq(projectId),
				projectMember.deleted.eq(false),
				member.companyId.eq(companyId),
				member.deleted.eq(false)
			)
			.fetch();
	}

	@Override
	public Optional<ProjectMember> findByMemberIdAndProjectId(UUID memberId,UUID projectId) {
		return jpaProjectMemberRepository.findByMemberIdAndProjectId(memberId,projectId);
	}

	@Override
	public boolean existsByMemberIdAndProjectIdAndDeleted(final UUID memberId, final UUID projectId,
		final boolean deleted) {
		return queryFactory.select(projectMember.id)
			.from(projectMember)
			.where(projectMember.deleted.isTrue()
				.and(projectMember.memberId.eq(memberId))
				.and(projectMember.projectId.eq(projectId)))
			.limit(1)
			.fetchFirst() != null;
	}
  
  @Override
	public List<ProjectMember> findAllByMemberId(UUID memberId) {
		return queryFactory
			.selectFrom(projectMember)
			.where(projectMember.memberId.eq(memberId)
				.and(projectMember.deleted.isFalse()))
			.fetch();
	}

  @Override
	public List<UUID> findProjectIdsByMemberId(UUID memberId) {
		return queryFactory
			.select(projectMember.projectId)
			.from(projectMember)
			.where(projectMember.memberId.eq(memberId))
			.fetch();
	}

}
