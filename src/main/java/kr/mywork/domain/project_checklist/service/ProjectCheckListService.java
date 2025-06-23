package kr.mywork.domain.project_checklist.service;

import kr.mywork.common.auth.components.dto.LoginMemberDetail;
import kr.mywork.domain.member.errors.MemberErrorType;
import kr.mywork.domain.member.errors.MemberTypeNotFoundException;
import kr.mywork.domain.member.model.MemberRole;
import kr.mywork.domain.project.errors.ProjectErrorType;
import kr.mywork.domain.project.errors.ProjectNotFoundException;
import kr.mywork.domain.project.model.Project;
import kr.mywork.domain.project.repository.ProjectAssignRepository;
import kr.mywork.domain.project.repository.ProjectRepository;
import kr.mywork.domain.project_checklist.errors.ProjectCheckListErrorType;
import kr.mywork.domain.project_checklist.errors.ProjectCheckListNotFoundException;
import kr.mywork.domain.project_checklist.model.ProjectCheckList;
import kr.mywork.domain.project_checklist.repository.ProjectCheckListRepository;
import kr.mywork.domain.project_checklist.service.dto.request.ProjectCheckListApprovalRequest;
import kr.mywork.domain.project_checklist.service.dto.request.ProjectCheckListCreateRequest;
import kr.mywork.domain.project_checklist.service.dto.request.ProjectCheckListUpdateRequest;
import kr.mywork.domain.project_checklist.service.dto.response.*;
import kr.mywork.domain.project_member.repository.ProjectMemberRepository;
import kr.mywork.domain.project_step.model.ProjectStep;
import kr.mywork.domain.project_step.repository.ProjectStepRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectCheckListService {

	private final ProjectCheckListRepository projectCheckListRepository;
	private final ProjectStepRepository projectStepRepository;
	private final ProjectRepository projectRepository;
	private final ProjectAssignRepository projectAssignRepository;
	private final ProjectMemberRepository projectMemberRepository;

	@Value("${dashboard.page.size}")
	private int projectChecklistPageSize;

	@Transactional
	public ProjectCheckListCreateResponse createProjectCheckList(
		ProjectCheckListCreateRequest projectCheckListRequest) {

		//TODO 프로젝트 단계 존재하는지 체크해야함.

		ProjectCheckList projectCheckList = projectCheckListRepository.save(projectCheckListRequest);

		return ProjectCheckListCreateResponse.from(projectCheckList);

	}

	@Transactional(readOnly = true)
	public ProjectCheckListDetailResponse getProjectCheckList(UUID checkListId) {
		ProjectCheckList projectCheckList = projectCheckListRepository.findById(checkListId)
			.orElseThrow(
				() -> new ProjectCheckListNotFoundException(ProjectCheckListErrorType.PROJECT_CHECK_LIST_NOT_FOUND));

		return new ProjectCheckListDetailResponse(projectCheckList);
	}

	@Transactional
	public ProjectCheckListUpdateResponse updateProjectCheckList(
		ProjectCheckListUpdateRequest projectCheckListUpdateRequest) {
		ProjectCheckList projectCheckList = projectCheckListRepository.findById(projectCheckListUpdateRequest.getId())
			.orElseThrow(
				() -> new ProjectCheckListNotFoundException(ProjectCheckListErrorType.PROJECT_CHECK_LIST_NOT_FOUND));

		projectCheckList.update(projectCheckListUpdateRequest);
		return ProjectCheckListUpdateResponse.from(projectCheckList);
	}

	@Transactional
	public UUID deleteProjectCheckList(UUID checkListId) {
		ProjectCheckList projectCheckList = projectCheckListRepository.findById(checkListId)
			.orElseThrow(
				() -> new ProjectCheckListNotFoundException(ProjectCheckListErrorType.PROJECT_CHECK_LIST_NOT_FOUND));

		projectCheckList.softDelete();
		return projectCheckList.getId();
	}

	@Transactional
	public ProjectCheckListApprovalResponse approvalProjectCheckList(
		ProjectCheckListApprovalRequest projectCheckListApprovalRequest) {
		ProjectCheckList projectCheckList = projectCheckListRepository.findById(projectCheckListApprovalRequest.getId())
			.orElseThrow(
				() -> new ProjectCheckListNotFoundException(ProjectCheckListErrorType.PROJECT_CHECK_LIST_NOT_FOUND));

		projectCheckList.changeApproval(projectCheckListApprovalRequest);
		return ProjectCheckListApprovalResponse.from(projectCheckList);
	}

	@Transactional
	public List<CheckListProjectStepProgressResponse> getCheckListProgress(final UUID projectId,
		final String approval) {

		final List<ProjectStep> projectSteps = projectStepRepository.findAllByProjectId(projectId);
		final Map<UUID, ProjectStep> projectStepMap = projectSteps.stream()
			.collect(Collectors.toMap(ProjectStep::getId, projectStep -> projectStep));

		final List<ProjectStepCheckListCountResponse> projectStepTotalCountResponses =
			projectCheckListRepository.findProgressCountGroupByProjectStepIdAndApproval(projectStepMap.keySet(), null);
		final List<ProjectStepCheckListCountResponse> projectStepApprovalCountResponses =
			projectCheckListRepository.findProgressCountGroupByProjectStepIdAndApproval(projectStepMap.keySet(),
				approval);

		return transformProjectStepProgress(projectStepTotalCountResponses, projectStepApprovalCountResponses,
			projectStepMap);
	}

	private List<CheckListProjectStepProgressResponse> transformProjectStepProgress(
		final List<ProjectStepCheckListCountResponse> projectStepTotalCountResponses,
		final List<ProjectStepCheckListCountResponse> projectStepApprovalCountResponses,
		final Map<UUID, ProjectStep> projectStepMap) {

		// findProgressCountGroupByProjectStepIdAndApproval 에서 순서를 보장해주므로 해당 코드 정상 동작
		List<CheckListProjectStepProgressResponse> checkListProjectStepProgressResponses = new ArrayList<>();
		for (int i = 0; i < projectStepTotalCountResponses.size(); i++) {
			final ProjectStepCheckListCountResponse projectStepTotalCountResponse =
				projectStepTotalCountResponses.get(i);
			final ProjectStepCheckListCountResponse projectStepApprovalCountResponse =
				projectStepApprovalCountResponses.get(i);

			final UUID projectStepId = projectStepTotalCountResponse.projectStepId();
			final String projectStepName = projectStepMap.get(projectStepId).getTitle();

			checkListProjectStepProgressResponses.add(new CheckListProjectStepProgressResponse(
				projectStepId,
				projectStepName,
				projectStepTotalCountResponse.count(),
				projectStepApprovalCountResponse.count()));
		}

		return checkListProjectStepProgressResponses;
	}

	@Transactional
	public List<ProjectCheckListSelectResponse> findAllByProjectIdAndProjectStepId(final UUID projectId,
		final UUID projectStepId) {
		final Project project = projectRepository.findById(projectId)
			.orElseThrow(() -> new ProjectNotFoundException(ProjectErrorType.PROJECT_NOT_FOUND));

		return projectCheckListRepository.findAllByProjectIdAndStepId(project.getId(), projectStepId);
	}

	@Transactional
	public List<MyCheckListWithApprovalResponse> getMyCheckListWithInFiveDays(
			final int page, final String approval, final LoginMemberDetail memberDetail, final LocalDateTime fiveDaysAgo){

		//로그인한 유저의 타입별로 프로젝트 Ids를 반환
		final List<UUID> projectIds =  getProjectIdsByRoleName(memberDetail);

		// 프로젝트 ID로 projectStep 정보가져오기(projectId,stepId)
		final List<ProjectStep> myProjectSteps = projectStepRepository.findAllByIds(projectIds);

		List<UUID> projectStepIds = myProjectSteps.stream()
				.map(ProjectStep::getId)
				.toList();

		// projectStepId로 checkList 정보 조회 (checkListId,checkListName,approval)
		final List<ProjectCheckList> myCheckList = projectCheckListRepository.findAllByProjectStepIds(projectStepIds,approval,page,projectChecklistPageSize,fiveDaysAgo);

		// 리턴 : projectId , checkListId, checkListName, approval
		Map<UUID,UUID> stepIdToProjectId = myProjectSteps.stream()
				.collect(Collectors.toMap(ProjectStep::getId,ProjectStep::getProjectId));

		return myCheckList.stream()
				.map(checkList -> new MyCheckListWithApprovalResponse(
						stepIdToProjectId.get(checkList.getProjectStepId()),
						checkList.getId(),
						checkList.getTitle(),
						checkList.getApproval()
				))
				.toList();

	}

	private List<UUID> getProjectIdsByRoleName(LoginMemberDetail memberDetail) {
		String roleName = memberDetail.roleName();
		if (MemberRole.CLIENT_ADMIN.isSameRoleName(roleName) || MemberRole.DEV_ADMIN.isSameRoleName(roleName)) {
			return projectAssignRepository.findCompanyProjectsByCompanyId(memberDetail.companyId(), roleName);
		} else if (MemberRole.USER.isSameRoleName(roleName)) {
			return projectMemberRepository.findProjectIdsByMemberId(memberDetail.memberId());
		} else {
			throw new MemberTypeNotFoundException(MemberErrorType.TYPE_NOT_FOUND);
		}
	}
}
