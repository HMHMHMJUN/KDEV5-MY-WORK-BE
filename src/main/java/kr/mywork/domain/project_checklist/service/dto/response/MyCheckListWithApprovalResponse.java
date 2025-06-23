package kr.mywork.domain.project_checklist.service.dto.response;

import java.util.UUID;

public record MyCheckListWithApprovalResponse(UUID projectId,UUID checkListId,String checkListName, String approval) {
}
