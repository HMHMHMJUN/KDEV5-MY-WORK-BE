package kr.mywork.domain.project.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import kr.mywork.common.auth.components.dto.LoginMemberDetail;
import kr.mywork.domain.company.errors.CompanyErrorType;
import kr.mywork.domain.company.errors.CompanyNotFoundException;
import kr.mywork.domain.company.model.Company;
import kr.mywork.domain.company.model.CompanyType;
import kr.mywork.domain.company.repository.CompanyRepository;
import kr.mywork.domain.dashboard.service.dto.response.DashboardPopularProjectsResponse;
import kr.mywork.domain.member.errors.MemberErrorType;
import kr.mywork.domain.member.errors.MemberTypeNotFoundException;
import kr.mywork.domain.member.model.Member;
import kr.mywork.domain.member.model.MemberRole;
import kr.mywork.domain.member.repository.MemberRepository;
import kr.mywork.domain.member.service.dto.response.MemberProjectInfoResponse;
import kr.mywork.domain.post.repository.PostRepository;
import kr.mywork.domain.project.errors.ProjectAssignNotFoundException;
import kr.mywork.domain.project.errors.ProjectErrorType;
import kr.mywork.domain.project.errors.ProjectNotFoundException;
import kr.mywork.domain.project.model.Project;
import kr.mywork.domain.project.model.ProjectAssign;
import kr.mywork.domain.project.model.ProjectMember;
import kr.mywork.domain.project.repository.ProjectAssignRepository;
import kr.mywork.domain.project.repository.ProjectRepository;
import kr.mywork.domain.project.service.dto.request.ProjectCreateRequest;
import kr.mywork.domain.project.service.dto.request.ProjectUpdateRequest;
import kr.mywork.domain.project.service.dto.response.NearDeadlineProjectResponse;
import kr.mywork.domain.project.service.dto.response.ProjectDetailResponse;
import kr.mywork.domain.project.service.dto.response.ProjectMemberResponse;
import kr.mywork.domain.project.service.dto.response.ProjectSelectResponse;
import kr.mywork.domain.project.service.dto.response.ProjectUpdateResponse;
import kr.mywork.domain.project_member.repository.ProjectMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class ProjectService {

	private static final String CLIENT_COMPANY_NAME = "CLIENT_COMPANY_NAME";
	private static final String DEV_COMPANY_NAME = "DEV_COMPANY_NAME";
	private static final String PROJECT_NAME = "PROJECT_NAME";

	@Value("${project.page.size}")
	private int projectPageSize;

	@Value("5")
	private int dashboardPageSize;

	private final ProjectRepository projectRepository;
	private final ProjectAssignRepository projectAssignRepository;
	private final CompanyRepository companyRepository;
	private final MemberRepository memberRepository;
	private final PostRepository postRepository;
	private final ProjectMemberRepository projectMemberRepository;

	@Transactional
	public UUID createProject(ProjectCreateRequest request) {

		final Project savedProject = projectRepository.save(
			new Project(request.name(), request.startAt(), request.endAt(), request.step(), request.detail()));

		projectAssignRepository.save(
			new ProjectAssign(savedProject.getId(), request.devCompanyId(), request.clientCompanyId()));

		return savedProject.getId();
	}

	@Transactional
	public UUID deleteProject(UUID projectId) {
		var project = projectRepository.findById(projectId)
			.orElseThrow(() -> new ProjectNotFoundException(ProjectErrorType.PROJECT_NOT_FOUND));

		project.setDeleted(true);
		return project.getId();
	}

	@Transactional
	public ProjectUpdateResponse updateProject(UUID projectId, ProjectUpdateRequest request) {
		var project = projectRepository.findById(projectId)
			.orElseThrow(() -> new ProjectNotFoundException(ProjectErrorType.PROJECT_NOT_FOUND));

		project.updateFrom(request);
		return ProjectUpdateResponse.from(project);
	}

	@Transactional(readOnly = true)
	public ProjectDetailResponse findProjectDetailsById(UUID projectId) {
		final Project project = projectRepository.findById(projectId)
			.orElseThrow(() -> new ProjectNotFoundException(ProjectErrorType.PROJECT_NOT_FOUND));

		final ProjectAssign projectAssign = projectAssignRepository.findByProjectId(projectId)
			.orElseThrow(() -> new ProjectAssignNotFoundException(ProjectErrorType.PROJECT_ASSIGN_NOT_FOUND));

		final Company devCompany = companyRepository.findById(projectAssign.getDevCompanyId())
			.orElseThrow(() -> new CompanyNotFoundException(CompanyErrorType.COMPANY_NOT_FOUND));

		final Company clientCompany = companyRepository.findById(projectAssign.getClientCompanyId())
			.orElseThrow(() -> new CompanyNotFoundException(CompanyErrorType.COMPANY_NOT_FOUND));

		return new ProjectDetailResponse(project.getId(), project.getName(), project.getStartAt(), project.getEndAt(),
			project.getStep(), project.getDetail(), project.getDeleted(), project.getCreatedAt(), devCompany.getId(),
			devCompany.getName(), devCompany.getContactPhoneNumber(), clientCompany.getId(), clientCompany.getName(),
			clientCompany.getContactPhoneNumber());
	}

	@Transactional(readOnly = true)
	public List<ProjectSelectResponse> findProjectsBySearchConditionWithPaging(
		final String keywordType, final String keyword, final String step, final Integer page) {
		if (keywordType == null) {
			final List<Project> projects =
				projectRepository.findAllByStepAndNameWithPaging(step, null, page, projectPageSize);

			final List<UUID> projectIds = projects.stream().map(Project::getId).toList();
			projects.sort(Comparator.comparing(Project::getId));

			final Map<UUID, ProjectAssign> projectIdAssignMap =
				transformProjectAssignMap(projectAssignRepository.findAllByProjectIds(projectIds));

			final List<UUID> devCompanyIds = projectIdAssignMap.values()
				.stream()
				.map(ProjectAssign::getDevCompanyId)
				.toList();

			final List<UUID> clientCompanyIds = projectIdAssignMap.values()
				.stream().map(ProjectAssign::getClientCompanyId).toList();

			final Map<UUID, Company> devCompanyMap = transformCompanyMap(companyRepository.findAllByIds(devCompanyIds));
			final Map<UUID, Company> clientCompanyMap = transformCompanyMap(
				companyRepository.findAllByIds(clientCompanyIds));

			return transformProjectSelectResponses(projects, projectIdAssignMap, devCompanyMap, clientCompanyMap);

		}

		switch (keywordType) {
			case DEV_COMPANY_NAME -> {
				final Map<UUID, Company> devCompanyMap =
					transformCompanyMap(companyRepository.findAllByNameAndType(keyword, CompanyType.DEV.name()));

				final List<ProjectAssign> devCompanyProjectAssigns = projectAssignRepository.findAllByCompanyIdsAndType(
					devCompanyMap.keySet(), CompanyType.DEV.name());

				final Map<UUID, ProjectAssign> projectIdAssignMap = transformProjectAssignMap(devCompanyProjectAssigns);
				final List<Project> projects = projectRepository.findAllByIdsAndStep(projectIdAssignMap.keySet(), step,
					page, projectPageSize);

				projects.sort(Comparator.comparing(Project::getId));

				List<UUID> clientCompanyIds = projects.stream()
					.map(project -> {
						final ProjectAssign projectAssign = projectIdAssignMap.get(project.getId());
						return projectAssign.getClientCompanyId();
					}).toList();

				final Map<UUID, Company> clientCompanyMap =
					transformCompanyMap(companyRepository.findAllByIds(clientCompanyIds));

				return transformProjectSelectResponses(projects, projectIdAssignMap, devCompanyMap, clientCompanyMap);
			}

			case CLIENT_COMPANY_NAME -> {
				final Map<UUID, Company> clientCompanyMap =
					transformCompanyMap(companyRepository.findAllByNameAndType(keyword, CompanyType.CLIENT.name()));

				final List<ProjectAssign> clientCompanyProjectAssigns = projectAssignRepository.findAllByCompanyIdsAndType(
					clientCompanyMap.keySet(), CompanyType.CLIENT.name());

				final Map<UUID, ProjectAssign> projectIdAssignMap =
					transformProjectAssignMap(clientCompanyProjectAssigns);

				final List<Project> projects =
					projectRepository.findAllByIdsAndStep(projectIdAssignMap.keySet(), step, page, projectPageSize);

				projects.sort(Comparator.comparing(Project::getId));

				List<UUID> devCompanyIds = projects.stream()
					.map(project -> {
						final ProjectAssign projectAssign = projectIdAssignMap.get(project.getId());
						return projectAssign.getDevCompanyId();
					}).toList();

				final Map<UUID, Company> devCompanyMap = transformCompanyMap(
					companyRepository.findAllByIds(devCompanyIds));

				return transformProjectSelectResponses(projects, projectIdAssignMap, devCompanyMap, clientCompanyMap);
			}

			case PROJECT_NAME -> {
				final List<Project> projects = projectRepository.findAllByStepAndNameWithPaging(step,
					keyword, page, projectPageSize);

				projects.sort(Comparator.comparing(Project::getId));

				final List<UUID> projectIds = projects.stream().map(Project::getId).collect(Collectors.toList());
				final List<ProjectAssign> projectAssigns = projectAssignRepository.findAllByProjectIds(projectIds);
				final List<UUID> devCompanyIds = projectAssigns.stream().map(ProjectAssign::getDevCompanyId).toList();
				final List<UUID> clientCompanyIds = projectAssigns.stream()
					.map(ProjectAssign::getClientCompanyId)
					.toList();

				final Map<UUID, ProjectAssign> projectIdAssignMap =
					transformProjectAssignMap(projectAssignRepository.findAllByProjectIds(projectIds));
				final Map<UUID, Company> devCompanyMap = transformCompanyMap(
					companyRepository.findAllByIds(devCompanyIds));
				final Map<UUID, Company> clientCompanyMap = transformCompanyMap(
					companyRepository.findAllByIds(clientCompanyIds));

				return transformProjectSelectResponses(projects, projectIdAssignMap, devCompanyMap, clientCompanyMap);
			}
		}

		throw new IllegalStateException();
	}

	private List<ProjectSelectResponse> transformProjectSelectResponses(
		final List<Project> projects,
		final Map<UUID, ProjectAssign> projectIdAssignMap,
		final Map<UUID, Company> devCompanyMap,
		final Map<UUID, Company> clientCompanyMap) {

		return projects.stream()
			.map(project -> {
				final ProjectAssign projectAssign = projectIdAssignMap.get(project.getId());
				final Company devCompany = devCompanyMap.get(projectAssign.getDevCompanyId());
				final Company clientCompany = clientCompanyMap.get(projectAssign.getClientCompanyId());

				return new ProjectSelectResponse(
					project.getId(), project.getName(), project.getStartAt(), project.getEndAt(), project.getStep(),
					devCompany.getId(), devCompany.getName(), clientCompany.getId(), clientCompany.getName());
			}).toList();
	}

	private Map<UUID, ProjectAssign> transformProjectAssignMap(final List<ProjectAssign> projectAssigns) {
		return projectAssigns.stream()
			.collect(Collectors.toMap(ProjectAssign::getProjectId, projectAssign -> projectAssign));
	}

	private Map<UUID, Company> transformCompanyMap(final List<Company> companies) {
		return companies.stream()
			.collect(Collectors.toMap(Company::getId, company -> company));
	}

	@Transactional(readOnly = true)
	public Long countTotalProjectsByCondition(final String keywordType, final String keyword, final String step) {
		if (PROJECT_NAME.equals(keywordType)) {
			return projectRepository.countTotalProjectsByNameAndStep(keyword, step);
		}

		if (DEV_COMPANY_NAME.equals(keywordType)) {
			final List<Company> devCompanies = companyRepository.findAllByNameAndType(keyword, CompanyType.DEV.name());
			final List<UUID> devCompanyIds = devCompanies.stream().map(Company::getId).toList();

			final List<ProjectAssign> projectAssigns =
				projectAssignRepository.findAllByCompanyIdsAndType(devCompanyIds, CompanyType.DEV.name());
			final List<UUID> projectAssignIds = projectAssigns.stream().map(ProjectAssign::getProjectId).toList();

			return projectRepository.countTotalProjectIdsAndStep(projectAssignIds, step);
		}

		if (CLIENT_COMPANY_NAME.equals(keywordType)) {
			final List<Company> clientCompanies =
				companyRepository.findAllByNameAndType(keyword, CompanyType.CLIENT.name());
			final List<UUID> clientCompanyIds = clientCompanies.stream().map(Company::getId).toList();

			final List<ProjectAssign> projectAssigns =
				projectAssignRepository.findAllByCompanyIdsAndType(clientCompanyIds, CompanyType.CLIENT.name());
			final List<UUID> projectAssignIds = projectAssigns.stream().map(ProjectAssign::getProjectId).toList();

			return projectRepository.countTotalProjectIdsAndStep(projectAssignIds, step);
		}

		return projectRepository.countTotalProjectIdsAndStep(null, step);
	}

	@Transactional
	public List<ProjectMemberResponse> findMemberByCompanyId(UUID companyId, UUID projectId) {

		List<Member> companyMembers = memberRepository.findMemberListByCompanyId(companyId, projectId);

		return companyMembers.stream()
			.map(member -> new ProjectMemberResponse(
				member.getId(),
				member.getName()
			)).collect(Collectors.toList());
	}

	@Transactional
	public List<MemberProjectInfoResponse> findProjectsAssignedMember(UUID memberId) {

		List<MemberProjectInfoResponse> memberProjectList = projectRepository.findeMemberProjectList(memberId);

		return memberProjectList;
	}

	@Transactional(readOnly = true)
	public List<NearDeadlineProjectResponse> findNearDeadlineProjectsByLoginMember(
		final int page,
		final LoginMemberDetail loginMemberDetail
	) {
		final String userType = loginMemberDetail.roleName();

		if (MemberRole.SYSTEM_ADMIN.getRoleName().equals(userType)) {
			final List<Project> projects = projectRepository.findAllNearDeadlineProjects(page, dashboardPageSize);
			return projects.stream()
				.map(this::toResponseWithDday)
				.toList();
		}

		if (MemberRole.CLIENT_ADMIN.getRoleName().equals(userType) || MemberRole.DEV_ADMIN.getRoleName().equals(userType)) {
			final UUID companyId = loginMemberDetail.companyId();
			final List<ProjectAssign> assigns = projectAssignRepository.findAllByCompanyId(companyId);
			final List<UUID> projectIds = assigns.stream()
				.map(ProjectAssign::getProjectId)
				.distinct()
				.toList();

			final List<Project> projects = projectRepository.findAllNearDeadlineProjectsByProjectIds(projectIds, page, dashboardPageSize);
			return projects.stream()
				.map(this::toResponseWithDday)
				.toList();
		}

		final UUID memberId = loginMemberDetail.memberId();
		final List<ProjectMember> projectMembers = projectMemberRepository.findAllByMemberId(memberId);
		final List<UUID> projectIds = projectMembers.stream()
			.map(ProjectMember::getProjectId)
			.distinct()
			.toList();

		final List<Project> projects = projectRepository.findAllNearDeadlineProjectsByProjectIds(projectIds, page, dashboardPageSize);
		return projects.stream()
			.map(this::toResponseWithDday)
			.toList();
	}

	private NearDeadlineProjectResponse toResponseWithDday(Project project) {
		int dday = Math.max(0, (int) ChronoUnit.DAYS.between(LocalDate.now(), project.getEndAt().toLocalDate()));
		return NearDeadlineProjectResponse.of(project.getId(), project.getName(), project.getEndAt(), dday);
	}

	@Transactional(readOnly = true)
	public Long countNearDeadlineProjectsByLoginMember(final LoginMemberDetail loginMemberDetail) {
		final String userType = loginMemberDetail.roleName();

		if (MemberRole.SYSTEM_ADMIN.getRoleName().equals(userType)) {
			return projectRepository.countNearDeadlineProjects();
		}

		if (MemberRole.CLIENT_ADMIN.getRoleName().equals(userType) || MemberRole.DEV_ADMIN.getRoleName().equals(userType)) {
			final UUID companyId = loginMemberDetail.companyId();
			final List<ProjectAssign> assigns = projectAssignRepository.findAllByCompanyId(companyId);
			final List<UUID> projectIds = assigns.stream()
				.map(ProjectAssign::getProjectId)
				.distinct()
				.toList();

			return projectRepository.countNearDeadlineProjectsByProjectIds(projectIds);
		}

		final UUID memberId = loginMemberDetail.memberId();
		final List<ProjectMember> projectMembers = projectMemberRepository.findAllByMemberId(memberId);
		final List<UUID> projectIds = projectMembers.stream()
			.map(ProjectMember::getProjectId)
			.distinct()
			.toList();

		return projectRepository.countNearDeadlineProjectsByProjectIds(projectIds);
	}

	@Transactional
	public List<DashboardPopularProjectsResponse> getMostPostProjectsTopFive(LoginMemberDetail memberDetail) {
		final String memberRole = memberDetail.roleName();
		//가져온 프로젝트들의 ID에 이름을 과 순서를 매칭 해주는 메소드 [ buildPopularResponse ]
		if(MemberRole.SYSTEM_ADMIN.isSameRoleName(memberRole)){
			return buildPopularResponse(postRepository.findMostPostProjectTopFive(null));
		}
		if(MemberRole.CLIENT_ADMIN.isSameRoleName(memberRole) || MemberRole.DEV_ADMIN.isSameRoleName(memberRole)){
			final UUID companyId = memberDetail.companyId(); //회사 기준으로 -> 프로젝트 ID들 가져와야함.
			final List<UUID> companyProjectIds = projectAssignRepository.findCompanyProjectsByCompanyId(companyId,memberRole);

			return buildPopularResponse(postRepository.findMostPostProjectTopFive(companyProjectIds));
		}
		if(MemberRole.USER.isSameRoleName(memberRole)){
			UUID memberId = memberDetail.memberId(); //멤버 아이디 기준으로 프로젝트 ID를 가져와야함
			final List<UUID> memberProjectIds = projectMemberRepository.findProjectIdsByMemberId(memberId);

			return buildPopularResponse(postRepository.findMostPostProjectTopFive(memberProjectIds));
		}
		throw new MemberTypeNotFoundException(MemberErrorType.TYPE_NOT_FOUND);

	}
	private List<DashboardPopularProjectsResponse> buildPopularResponse(List<DashboardMostPostProjectResponse> mostPostProjects) {
		List<UUID> ids = mostPostProjects.stream()
				.map(DashboardMostPostProjectResponse::projectId)
				.toList();
		//프로젝트들의 ID 이름을 가져온다.
		List<Project> popularProjects = projectRepository.findProjectsNameById(ids);
		//리스트를 맵형태로 전환
		Map<UUID, Project> projectMap = popularProjects.stream()
			.collect(Collectors.toMap(Project::getId, p -> p));
		//리스트에 값을 맵에서 찾아서 맵핑
		return mostPostProjects.stream()
			.map(dto -> {
				Project p = projectMap.get(dto.projectId());
				return new DashboardPopularProjectsResponse(p.getId(), p.getName(), dto.postCount());
			})
			.toList();
	}
}
