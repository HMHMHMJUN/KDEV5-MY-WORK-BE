package kr.mywork.interfaces.dashboard.controller;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import kr.mywork.common.api.support.response.ApiResponse;
import kr.mywork.common.auth.components.annotation.LoginMember;
import kr.mywork.common.auth.components.dto.LoginMemberDetail;
import kr.mywork.domain.dashboard.service.DashboardService;
import kr.mywork.domain.dashboard.service.dto.response.DashboardPopularProjectsResponse;
import kr.mywork.domain.project.service.ProjectService;
import kr.mywork.domain.project.service.dto.request.NearDeadlineProjectRequest;
import kr.mywork.domain.project.service.dto.response.NearDeadlineProjectResponse;
import kr.mywork.domain.project_checklist.service.ProjectCheckListService;
import kr.mywork.domain.project_checklist.service.dto.response.MyCheckListFiveDaysAgoResponse;
import kr.mywork.domain.project_checklist.service.dto.response.MyCheckListWithApprovalResponse;
import kr.mywork.interfaces.dashboard.controller.dto.response.*;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard")
@Validated
public class DashboardController {

	private static final String APPROVAL_TYPE = "^(PENDING|APPROVED|REJECTED)$";
	private final DashboardService dashboardService;
	private final ProjectService projectService;
	private final ProjectCheckListService projectCheckListService;

	@GetMapping("/totalSummery")
	public ApiResponse<DashboardCountSummaryWebResponse> getDashboardTotalCount(
			@LoginMember LoginMemberDetail loginMemberDetail) {

		final DashboardCountSummaryWebResponse dashboardCountSummaryWebResponse = dashboardService.getSummaryTotalCount(
				loginMemberDetail);

		return ApiResponse.success(dashboardCountSummaryWebResponse);

	}
	@GetMapping("/popular-projects")
	public    ApiResponse<DashboardPopularProjectListWebResponse> getMostPostProjectsTopFive(
		@LoginMember final LoginMemberDetail memberDetail
	){
		final List<DashboardPopularProjectsResponse> popularProjects = projectService.getMostPostProjectsTopFive(memberDetail);

		final List<DashboardPopularProjectWebResponse> webResponse = popularProjects.stream()
			.map(DashboardPopularProjectWebResponse::from)
			.toList();

		final DashboardPopularProjectListWebResponse response = new DashboardPopularProjectListWebResponse(webResponse);

		return ApiResponse.success(response);

	}

	@GetMapping("/projects/near-deadline")
	public ApiResponse<NearDeadlineProjectListWebResponse> getNearDeadlineProjects(
		@RequestParam(name = "page") @Min(value = 1, message = "{invalid.page-size}") final int page,
		@LoginMember final LoginMemberDetail loginMemberDetail
	) {

		LocalDate today = LocalDate.now();

		NearDeadlineProjectRequest nearDeadlineProjectWebRequest = new NearDeadlineProjectRequest(
			loginMemberDetail.memberId(),
			loginMemberDetail.companyId(),
			loginMemberDetail.roleName()
		);

		final List<NearDeadlineProjectResponse> NearDeadlineProjectResponse =
			projectService.findNearDeadlineProjectsByLoginMember(page, nearDeadlineProjectWebRequest, today);

		final long totalCount =
			projectService.countNearDeadlineProjectsByLoginMember(nearDeadlineProjectWebRequest, today);

		final List<NearDeadlineProjectWebResponse> nearDeadlineProjectWebResponses = NearDeadlineProjectResponse.stream()
			.map(NearDeadlineProjectWebResponse::fromServiceResponse)
			.toList();

		final NearDeadlineProjectListWebResponse response =
			new NearDeadlineProjectListWebResponse(nearDeadlineProjectWebResponses, totalCount);

		return ApiResponse.success(response);
	}

	@GetMapping("/check-list")	// 회사 전체 요청/승인/거절한/체크리스
	public ApiResponse<MyCheckListWithApprovalWebResponse> getDashboardMyCheckList(
			@RequestParam(name = "page") @Min(value = 1, message = "{invalid.page-size}") final int page,
			@RequestParam(name="approval") @Pattern(regexp = APPROVAL_TYPE,message = "{invalid.approval-type}") final String approval,
			@LoginMember LoginMemberDetail loginMemberDetail){

		LocalDateTime fiveDaysAgo = LocalDateTime.now().minusDays(5);

		final List<MyCheckListWithApprovalResponse> getMyCheckListWithInFiveDays = projectCheckListService.getMyCheckListWithInFiveDays(page,approval,loginMemberDetail,fiveDaysAgo);

		final List<MyCheckListFiveDaysAgoResponse> checklistsWithinFiveDays =getMyCheckListWithInFiveDays.stream()
				.map(MyCheckListFiveDaysAgoResponse::from)
				.toList();

		return ApiResponse.success(new MyCheckListWithApprovalWebResponse(checklistsWithinFiveDays));
	}
}
