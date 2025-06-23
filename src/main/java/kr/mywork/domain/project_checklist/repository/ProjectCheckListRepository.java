package kr.mywork.domain.project_checklist.repository;

import kr.mywork.domain.project_checklist.model.ProjectCheckList;
import kr.mywork.domain.project_checklist.service.dto.request.ProjectCheckListCreateRequest;
import kr.mywork.domain.project_checklist.service.dto.response.ProjectCheckListSelectResponse;
import kr.mywork.domain.project_checklist.service.dto.response.ProjectStepCheckListCountResponse;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectCheckListRepository {
	ProjectCheckList save(ProjectCheckListCreateRequest projectCheckListRequest);

	Optional<ProjectCheckList> findById(UUID checkListId);

	List<ProjectStepCheckListCountResponse> findProgressCountGroupByProjectStepIdAndApproval(
		Collection<UUID> projectStepIds, String approval);

	List<ProjectCheckListSelectResponse> findAllByProjectIdAndStepId(UUID projectId, UUID projectStepId);

	List<ProjectCheckList> findAllByProjectStepIds(List<UUID> projectStepIds, String approval, int page, int projectChecklistPageSize, LocalDateTime fiveDaysAgo);
}
