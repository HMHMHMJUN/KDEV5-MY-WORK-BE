package kr.mywork.domain.project_checklist.service.dto.response;

import java.util.UUID;

public record MyCheckListFiveDaysAgoResponse (UUID projectId, UUID checkListId, String checkListName, String approval) {
    public static MyCheckListFiveDaysAgoResponse from(MyCheckListWithApprovalResponse response){
        return new MyCheckListFiveDaysAgoResponse(
                response.projectId(),
                response.checkListId(),
                response.checkListName(),
                response.approval()
        );
    }
}
