package kr.mywork.domain.company.errors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CompanyErrorType {
	ID_NOT_FOUND(CompanyErrorCode.ERROR_COMPANY01, "회사 아이디를 찾을 수 없습니다."),
	TYPE_NOT_FOUND(CompanyErrorCode.ERROR_COMPANY02, "회사 타입을 찾을 수 없습니다."),
	COMPANY_NOT_FOUND(CompanyErrorCode.ERROR_COMPANY03, "회사가 존재 하지 않습니다.");

	private final CompanyErrorCode errorCode;
	private final String message;
}
