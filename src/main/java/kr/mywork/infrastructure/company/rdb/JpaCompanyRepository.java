package kr.mywork.infrastructure.company.rdb;

import java.util.UUID;
import kr.mywork.domain.company.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaCompanyRepository extends JpaRepository<Company, UUID> {
}
