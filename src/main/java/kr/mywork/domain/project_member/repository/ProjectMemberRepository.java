package kr.mywork.domain.project_member.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import kr.mywork.domain.project.model.ProjectMember;
import kr.mywork.domain.project_member.service.dto.response.CompanyMemberInProjectResponse;

public interface ProjectMemberRepository {
	ProjectMember save(ProjectMember projectMember);
	List<CompanyMemberInProjectResponse> findCompanyMembersInProject(UUID projectId,UUID companyId);
	Optional<ProjectMember> findByMemberIdAndProjectId(UUID memberId,UUID projectId);
	boolean existsByMemberIdAndProjectIdAndDeleted(UUID memberId, UUID projectId, boolean deleted);
	List<ProjectMember> findAllByMemberId(UUID memberId);
	List<UUID> findProjectIdsByMemberId(UUID memberId);
}
