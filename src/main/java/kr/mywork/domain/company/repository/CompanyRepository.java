package kr.mywork.domain.company.repository;

import java.util.Optional;
import java.util.UUID;
import kr.mywork.domain.company.model.Company;
import kr.mywork.domain.company.service.dto.request.CompanyCreateRequest;

public interface CompanyRepository {
	Company save(CompanyCreateRequest companyCreateRequest);
	Optional<Company> findById(UUID companyId);
}
