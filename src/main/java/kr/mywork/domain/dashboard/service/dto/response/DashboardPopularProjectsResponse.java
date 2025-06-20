package kr.mywork.domain.dashboard.service.dto.response;

import java.util.UUID;

public record DashboardPopularProjectsResponse(UUID projectId, String projectName,long postCount) {
}
