package kr.mywork.domain.company.errors;

public class CompanyNotFoundException extends CompanyException {
	public CompanyNotFoundException(final CompanyErrorType errorType) {
		super(errorType);
	}
}
