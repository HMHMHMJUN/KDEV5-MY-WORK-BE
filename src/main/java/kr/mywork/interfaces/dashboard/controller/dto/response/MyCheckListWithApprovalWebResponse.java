package kr.mywork.interfaces.dashboard.controller.dto.response;

import kr.mywork.domain.project_checklist.service.dto.response.MyCheckListFiveDaysAgoResponse;

import java.util.List;

public record MyCheckListWithApprovalWebResponse(List<MyCheckListFiveDaysAgoResponse> checkList) {
}
