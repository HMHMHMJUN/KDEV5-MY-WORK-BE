package kr.mywork.docs;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippet;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.uuid.Generators;
import java.util.UUID;
import kr.mywork.common.api.support.response.ResultType;
import kr.mywork.infrastructure.company.rdb.JpaCompanyRepository;
import kr.mywork.interfaces.company.controller.dto.request.CompanyCreateWebRequest;
import kr.mywork.interfaces.company.controller.dto.request.CompanyDeleteWebRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;

public class CompanyDocumentationTest extends RestDocsDocumentation {

	@Test
	@DisplayName("회사 아이디 생성 테스트 성공")
	void 회사_아이디_생성_테스트_성공() throws Exception {
		// given, when
		final ResultActions result = mockMvc.perform(
			post("/api/company/id/generate")
				.contentType(MediaType.APPLICATION_JSON));

		// then
		result.andExpectAll(
				status().isOk(),
				jsonPath("$.result").value(ResultType.SUCCESS.name()),
				jsonPath("$.data").exists(),
				jsonPath("$.error").doesNotExist())
			.andDo(document("company-id-create-success", companyIdCreateSuccessResource()));
	}

	private ResourceSnippet companyIdCreateSuccessResource() {
		return resource(
			ResourceSnippetParameters.builder()
				.tag("Company API")
				.summary("회사 아이디 API")
				.description("회사 아이디를 발급 받는다")
				.requestHeaders(
					headerWithName(HttpHeaders.CONTENT_TYPE).description("컨텐츠 타입"))
				.responseFields(
					fieldWithPath("result").type(JsonFieldType.STRING).description("응답 결과"),
					fieldWithPath("data.companyId").type(JsonFieldType.STRING).description("발급받은 회사 생성 아이디"),
					fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보"))
				.build()
		);
	}

	@Test
	@DisplayName("회사 생성 성공")
	@Sql("classpath:sql/company-id.sql")
	void 회사_생성_성공() throws Exception {
		// given
		UUID companyId = UUID.fromString("0196f7a6-10b6-7123-a2dc-32c3861ea55e"); // UUID ver7

		final CompanyCreateWebRequest companyCreateWebRequest =
			new CompanyCreateWebRequest(companyId, "회사 이름", "회사 디테일", "0123-123", "강남 밀왈빌딩", "DEV", "010-0000-0000",
				"company01@gmail.com", "/image/url");

		final String requestBody = objectMapper.writeValueAsString(companyCreateWebRequest);

		// when
		final ResultActions result = mockMvc.perform(
			post("/api/company") // HTTP method (URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody));

		// then
		result.andExpectAll(
				status().isOk(),
				jsonPath("$.result").value(ResultType.SUCCESS.name()),
				jsonPath("$.data").exists(),
				jsonPath("$.error").doesNotExist())
			.andDo(document("company-create-success", companyCreateSuccessResource()));
	}

	private ResourceSnippet companyCreateSuccessResource() {
		return resource(
			ResourceSnippetParameters.builder()
				.tag("Company API")
				.summary("회사 생성 API")
				.description("발급받은 회사 아이디를 통해 회사를 생성한다.")
				.requestHeaders(
					headerWithName(HttpHeaders.CONTENT_TYPE).description("컨텐츠 타입"))
				.responseFields(
					fieldWithPath("result").type(JsonFieldType.STRING).description("응답 결과"),
					fieldWithPath("data.companyId").type(JsonFieldType.STRING).description("생성한 회사 아이디"),
					fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보"))
				.build()
		);
	}

	@Test
	@DisplayName("회사 생성 실패 - 아이디가 존재하지 않는 경우")
	void 회사_생성_실패_아이디_미존재() throws Exception {
		// given
		UUID companyId = Generators.timeBasedEpochGenerator().generate(); // UUID ver7

		final CompanyCreateWebRequest companyCreateWebRequest =
			new CompanyCreateWebRequest(companyId, "회사 이름", "회사 디테일", "0123-123", "강남 밀왈빌딩", "DEV", "010-0000-0000",
				"company01@gmail.com", "/image/url");

		final String requestBody = objectMapper.writeValueAsString(companyCreateWebRequest);

		// when
		final ResultActions result = mockMvc.perform(
			post("/api/company") // HTTP method (URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody));

		// then
		result.andExpectAll(
				status().is4xxClientError(),
				jsonPath("$.result").value(ResultType.ERROR.name()),
				jsonPath("$.data").doesNotExist(),
				jsonPath("$.error").exists())
			.andDo(document("company-create-fail01", companyCreateFailResource01()));
	}

	private ResourceSnippet companyCreateFailResource01() {
		return resource(
			ResourceSnippetParameters.builder()
				.tag("Company API")
				.summary("회사 생성 API")
				.description("발급받은 회사 아이디를 통해 회사를 생성한다.")
				.requestHeaders(
					headerWithName(HttpHeaders.CONTENT_TYPE).description("컨텐츠 타입"))
				.responseFields(
					fieldWithPath("result").type(JsonFieldType.STRING).description("응답 결과"),
					fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터"),
					fieldWithPath("error.code").type(JsonFieldType.STRING).description("에러 코드"),
					fieldWithPath("error.message").type(JsonFieldType.STRING).description("에러 정보"),
					fieldWithPath("error.data").type(JsonFieldType.NULL).description("에러 정보"))
				.build()
		);
	}

	@Test
	@DisplayName("회사 정보 업데이트 성공")
	@Sql("classpath:sql/company-for-update.sql")
	void 회사_정보_업데이트_성공() throws Exception {
		//given
		final UUID id = UUID.fromString("0196f7a6-10b6-7123-a2dc-32c3861ea55e");

		final CompanyCreateWebRequest companyCreateWebRequest = new CompanyCreateWebRequest(id, "현대", "바뀐회사설명",
				"010234034", "부산", "DEV", "010-9999-9999", "suha730@naver.com", "/image/url");

		final String requestBody = objectMapper.writeValueAsString(companyCreateWebRequest);

		//when
		final ResultActions result = mockMvc.perform(
				put("/api/company")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody)
		);

		//then
		result.andExpectAll(
						status().isOk(),
						jsonPath("$.result").value(ResultType.SUCCESS.name()),
						jsonPath("$.data").exists(),
						jsonPath("$.error").doesNotExist())
				.andDo(document("company-update-success", companyUpdateSuccessResource()));

	}

	private ResourceSnippet companyUpdateSuccessResource() {
		return resource(
				ResourceSnippetParameters.builder()
						.tag("Company API")
						.summary("회사 업데이트 API")
						.description("새로운 회사 정보로 업데이트한다.")
						.requestHeaders(
								headerWithName(HttpHeaders.CONTENT_TYPE).description("컨텐츠 타입"))
						.responseFields(
								fieldWithPath("result").type(JsonFieldType.STRING).description("응답 결과"),
								fieldWithPath("data.companyId").type(JsonFieldType.STRING).description("업데이트된 회사 아이디"),  // 수정
								fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보"))  // 수정
						.build()
				);
	}

	@Test
	@DisplayName("회사 정보 업데이트 실패 - 잘못되 요청값(존재하지 않는 회사 타입 요청)")
	@Sql("classpath:sql/company-for-update.sql")
	void 회사_정보_업데이트_실패() throws Exception {
		//given
		final UUID id = UUID.fromString("0196f7a6-10b6-7123-a2dc-32c3861ea55e");

		final CompanyCreateWebRequest companyCreateWebRequest = new CompanyCreateWebRequest(id, "삼성", "회사 타입을 존재하지 않는 타입으로 변경 요청",
				"010234034", "부산", "INVALID_TYPE", "010-9999-9999", "suha730@naver.com", "/image/url");

		final String requestBody = objectMapper.writeValueAsString(companyCreateWebRequest);

		//when
		final ResultActions result = mockMvc.perform(
				put("/api/company")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody)
		);

		//then
		result.andExpectAll(
						status().is4xxClientError(),
						jsonPath("$.result").value(ResultType.ERROR.name()),
						jsonPath("$.data").doesNotExist(),
						jsonPath("$.error").exists())
				.andDo(document("company-update-fail", companyUpdateFailResource()));
	}

	private ResourceSnippet companyUpdateFailResource() {
		return resource(
				ResourceSnippetParameters.builder()
						.tag("Company API")
						.summary("회사 업데이트 API")
						.description("새로운 회사 정보로 업데이트한다.")
						.requestHeaders(
								headerWithName(HttpHeaders.CONTENT_TYPE).description("컨텐츠 타입"))
						.responseFields(
								fieldWithPath("result").type(JsonFieldType.STRING).description("응답 결과"),
								fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터"),
								fieldWithPath("error.code").type(JsonFieldType.STRING).description("에러 코드"),
								fieldWithPath("error.message").type(JsonFieldType.STRING).description("에러 정보"),
								fieldWithPath("error.data").type(JsonFieldType.NULL).description("에러 정보"))
						.build()
		);
	}

	@Test
	@DisplayName("회사 삭제 성공")
	@Sql("classpath:sql/company-delete.sql")
	void 회사_삭제_성공() throws Exception {
		UUID companyId = UUID.fromString("0196f7a6-10b6-7123-a2dc-32c3861ea55e"); // company-id.sql과 동일한 값
		CompanyDeleteWebRequest deleteReq = new CompanyDeleteWebRequest(companyId);

		// JSON 요청 본문 생성
		String requestBody = objectMapper.writeValueAsString(deleteReq);

		// When
		ResultActions result = mockMvc.perform(
				delete("/api/company", companyId)
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody)
		);

		// Then
		result.andExpectAll(
				status().isOk(),
				jsonPath("$.result").value(ResultType.SUCCESS.name()),
				jsonPath("$.data.companyId").value(companyId.toString()),
				jsonPath("$.error").doesNotExist()
		).andDo(document("company-del-success",companyDeleteSuccess()));
	}

	private ResourceSnippet companyDeleteSuccess() {
		return resource(
			ResourceSnippetParameters.builder()
				.tag("Company API")
				.summary("회사 삭제 API")
				.description("발급받은 회사 아이디를 통해 회사를 삭제한다.")
				.requestHeaders(
					headerWithName(HttpHeaders.CONTENT_TYPE).description("컨텐츠 타입"))
				.responseFields(
						fieldWithPath("result").type(JsonFieldType.STRING).description("응답 결과"),
						fieldWithPath("data.companyId").type(JsonFieldType.STRING).description("삭제한 회사 아이디"),
						fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보"))
					.build()
		);
	}
}
