package kr.mywork.domain.post.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.lang.Nullable;

import kr.mywork.domain.post.model.Post;
import kr.mywork.domain.post.service.dto.request.PostCreateRequest;
import kr.mywork.domain.post.service.dto.response.PostSelectResponse;
import kr.mywork.domain.project.service.dto.response.DashboardMostPostProjectResponse;
import kr.mywork.domain.project_step.model.ProjectStep;

public interface PostRepository {
	Post save(PostCreateRequest postCreateRequest);

	Optional<Post> findById(UUID id);

	//프로젝트 스텝 있는경우
	List<PostSelectResponse> findPostsByProjectStepSearchConditionWithPaging(int page, int postPageSize, UUID projectStepId, String projectStepTitle,
		String keyword, Boolean deleted, UUID projectId, String keywordType, String approval);

	//프로젝트 스텝 없는경우 -> 프로젝트의 모든 게시물 조회
	List<PostSelectResponse> findPostsByProjectSearchConditionWithPaging(int page, int postPageSize,
		String keyword, Boolean deleted, UUID projectId, String keywordType, String approval);

	Long countTotalPostsByProjectCondition(List<ProjectStep> projectSteps, String keyword, Boolean deleted, UUID projectId, String keywordType, String approval);

	Long countTotalPostsByProjectStepCondition(UUID projectStepId, String keyword, Boolean deleted, UUID projectId, String keywordType, String approval);

	List<DashboardMostPostProjectResponse> findMostPostProjectTopFive(@Nullable List<UUID> limitedProjectIds);
}
