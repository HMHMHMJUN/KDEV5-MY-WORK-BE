package kr.mywork.domain.project.repository;

import kr.mywork.domain.member.service.dto.response.MemberProjectInfoResponse;
import kr.mywork.domain.project.model.Project;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository {

	Project save(Project project);

	Optional<Project> findById(UUID projectId);

	Long countTotalProjectIdsAndStep(Collection<UUID> projectIds, String step);

	List<MemberProjectInfoResponse> findeMemberProjectList(UUID memberId);

	List<Project> findAllByStepAndNameWithPaging(String step, String title, Integer page, Integer size);

	List<Project> findAllByIdsAndStep(Collection<UUID> projectIds, String step, Integer page, Integer projectPageSize);

	Long countTotalProjectsByNameAndStep(String keyword, String step);

	List<Project> findAllNearDeadlineProjects(int page, int pageSize);

	List<Project> findAllNearDeadlineProjectsByProjectIds(Collection<UUID> projectIds, int page, int pageSize);

	Long countNearDeadlineProjects();

	Long countNearDeadlineProjectsByProjectIds(Collection<UUID> projectIds);

	List<Project> findProjectsNameById(List<UUID> mostPostProjectIds);

}
