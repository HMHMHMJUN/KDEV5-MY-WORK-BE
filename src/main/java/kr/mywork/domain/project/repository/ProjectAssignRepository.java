package kr.mywork.domain.project.repository;

import kr.mywork.domain.project.model.ProjectAssign;
import kr.mywork.domain.project.service.dto.response.ProjectAssignResponse;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectAssignRepository {

	ProjectAssign save(ProjectAssign projectAssign);

	Optional<ProjectAssign> findByProjectId(UUID projectId);

	Optional<ProjectAssignResponse> findDtoByProjectId(UUID projectId);

	List<ProjectAssign> findAllByProjectIds(List<UUID> projectIds);

	List<ProjectAssign> findAllByCompanyIdsAndType(final Collection<UUID> companyIds, final String companyType);

	List<ProjectAssign> findAllByCompanyId(UUID companyId);

	List<UUID> findCompanyProjectsByCompanyId(final UUID companyId ,String memberRole);

}
