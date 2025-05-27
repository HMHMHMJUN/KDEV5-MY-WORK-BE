package kr.mywork.domain.company.service.dto.request;

import java.util.UUID;
import kr.mywork.domain.company.model.Company;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CompanyUpdateRequest {
	@Getter
	private final UUID id;
	private final String name;
	private final String detail;
	private final String businessNumber;
	private final String address;
	private final String type;
	private final String contactPhoneNumber;
	private final String contactEmail;
	private final String logoImagePath;
}
