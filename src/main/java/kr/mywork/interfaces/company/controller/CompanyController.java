package kr.mywork.interfaces.company.controller;

import java.util.UUID;
import kr.mywork.common.api.support.response.ApiResponse;
import kr.mywork.domain.company.service.CompanyService;
import kr.mywork.domain.company.service.dto.request.CompanyCreateRequest;
import kr.mywork.domain.company.service.dto.request.CompanyUpdateRequest;
import kr.mywork.interfaces.company.controller.dto.request.CompanyCreateWebRequest;
import kr.mywork.interfaces.company.controller.dto.request.CompanyDeleteWebRequest;
import kr.mywork.interfaces.company.controller.dto.request.CompanyUpdateWebRequest;
import kr.mywork.interfaces.company.controller.dto.response.CompanyCreateWebResponse;
import kr.mywork.interfaces.company.controller.dto.response.CompanyDeleteWebResponse;
import kr.mywork.interfaces.company.controller.dto.response.CompanyIdCreateWebResponse;
import kr.mywork.interfaces.company.controller.dto.response.CompanyUpdateWebResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/company")
public class CompanyController {

	private final CompanyService companyService;

	@PostMapping("/id/generate")
	public ApiResponse<CompanyIdCreateWebResponse> createCompanyId() {
		final UUID companyId = companyService.createCompanyId();
		return ApiResponse.success(new CompanyIdCreateWebResponse(companyId));
	}

	@PostMapping
	public ApiResponse<CompanyCreateWebResponse> createCompany(
		@RequestBody final CompanyCreateWebRequest companyCreateWebRequest) {

		final CompanyCreateRequest companyCreateRequest = companyCreateWebRequest.toServiceDto();

		final UUID createdCompanyId = companyService.createCompany(companyCreateRequest);

		final CompanyCreateWebResponse companyCreateWebResponse = new CompanyCreateWebResponse(createdCompanyId);

		return ApiResponse.success(companyCreateWebResponse);
	}

	@PutMapping()
	public ApiResponse<CompanyUpdateWebResponse> updateCompany(
			@RequestBody final CompanyUpdateWebRequest companyUpdateWebRequest) {

		final CompanyUpdateRequest companyUpdateRequest = companyUpdateWebRequest.toServiceDto();

		final UUID companyId = companyService.updateCompany(companyUpdateRequest);

		final CompanyUpdateWebResponse companyUpdateWebResponse = new CompanyUpdateWebResponse(companyId);

		return ApiResponse.success(companyUpdateWebResponse);
	}

	@DeleteMapping
	public ApiResponse<CompanyDeleteWebResponse> deleteCompany(
			@RequestBody final CompanyDeleteWebRequest companyDeleteWebRequest
			){
		final UUID deleteCompanyId = companyService.deleteCompany(companyDeleteWebRequest.id());

		final CompanyDeleteWebResponse companyDeleteWebResponse = new CompanyDeleteWebResponse(deleteCompanyId);

		return ApiResponse.success(companyDeleteWebResponse);
	}
}
