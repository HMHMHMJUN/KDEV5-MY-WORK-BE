package kr.mywork.domain.project.service.dto.response;

import java.util.UUID;

public record DashboardMostPostProjectResponse(UUID projectId,long postCount) {
}
