package kr.mywork.infrastructure.project.rdb;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.mywork.domain.company.model.CompanyType;
import kr.mywork.domain.member.errors.MemberErrorType;
import kr.mywork.domain.member.errors.MemberTypeNotFoundException;
import kr.mywork.domain.member.model.MemberRole;
import kr.mywork.domain.project.model.ProjectAssign;
import kr.mywork.domain.project.repository.ProjectAssignRepository;
import kr.mywork.domain.project.service.dto.response.ProjectAssignResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static kr.mywork.domain.project.model.QProjectAssign.projectAssign;

@Repository
@RequiredArgsConstructor
@Transactional
public class QueryDslProjectAssignRepository implements ProjectAssignRepository {

	private final JPAQueryFactory queryFactory;
	private final JpaProjectAssignRepository jpaProjectAssignRepository;

	@Override
	public ProjectAssign save(ProjectAssign projectAssign) {
		return jpaProjectAssignRepository.save(projectAssign);
	}


	@Override
	public Optional<ProjectAssign> findByProjectId(UUID projectId) {
		return jpaProjectAssignRepository.findByProjectId(projectId);
	}

	@Override
	public Optional<ProjectAssignResponse> findDtoByProjectId(UUID projectId) {
		return Optional.ofNullable(
			queryFactory
				.select(Projections.constructor(
					ProjectAssignResponse.class,
					projectAssign.projectId,
					projectAssign.devCompanyId,
					projectAssign.clientCompanyId
				))
				.from(projectAssign)
				.where(projectAssign.projectId.eq(projectId))
				.fetchOne()
		);
	}

	@Override
	public List<ProjectAssign> findAllByProjectIds(final List<UUID> projectIds) {
		return queryFactory.selectFrom(projectAssign)
			.where(projectAssign.projectId.in(projectIds))
			.fetch();
	}

	@Override
	public List<ProjectAssign> findAllByCompanyIdsAndType(final Collection<UUID> companyIds, final String companyType) {
		return queryFactory.selectFrom(projectAssign)
			.where(inCompanyTypeAndCompanyIds(companyIds, companyType))
			.fetch();
	}

	@Override
	public List<UUID> findCompanyProjectsByCompanyId(UUID companyId,String memberRole) {
		return queryFactory
			.select(projectAssign.projectId)
			.from(projectAssign)
			.where(companyFilter(companyId,memberRole))
			.fetch();
	}
	private BooleanExpression companyFilter(UUID companyId, String memberRole) {
		if(MemberRole.CLIENT_ADMIN.isSameRoleName(memberRole)){
			return projectAssign.clientCompanyId.eq(companyId);
		}else if(MemberRole.DEV_ADMIN.isSameRoleName(memberRole)){
			return projectAssign.devCompanyId.eq(companyId);
		}
		throw new MemberTypeNotFoundException(MemberErrorType.TYPE_NOT_FOUND);
	}

	private BooleanExpression inCompanyTypeAndCompanyIds(final Collection<UUID> companyIds, final String companyType) {
		if (CompanyType.DEV.name().equals(companyType)) {
			return projectAssign.devCompanyId.in(companyIds);
		}

		if (CompanyType.CLIENT.name().equals(companyType)) {
			return projectAssign.clientCompanyId.in(companyIds);
		}

		return null;
	}

	@Override
	public List<ProjectAssign> findAllByCompanyId(UUID companyId) {
		return queryFactory
			.selectFrom(projectAssign)
			.where(
				projectAssign.devCompanyId.eq(companyId)
					.or(projectAssign.clientCompanyId.eq(companyId))
			)
			.fetch();
	}
}
