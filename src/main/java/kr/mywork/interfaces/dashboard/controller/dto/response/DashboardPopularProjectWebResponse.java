package kr.mywork.interfaces.dashboard.controller.dto.response;

import java.util.UUID;

import kr.mywork.domain.dashboard.service.dto.response.DashboardPopularProjectsResponse;

public record DashboardPopularProjectWebResponse (UUID projectId,String projectName){

	public static DashboardPopularProjectWebResponse from(DashboardPopularProjectsResponse serviceResponse) {
		return new DashboardPopularProjectWebResponse(
			serviceResponse.projectId(),
			serviceResponse.projectName()
		);
	}
}
