INSERT INTO project (id, name, start_at, end_at, step, created_at, modified_at, detail, deleted)
VALUES
    (UNHEX(REPLACE('0197893a-ad34-734c-97d7-2e0dd6429247', '-', '')), 'ERP 시스템', NOW(), NOW(), '기획', NOW(), NOW(), 'ERP 프로젝트입니다.', 0),
    (UNHEX(REPLACE('0197893a-ad34-7936-a45c-8abf499013ae', '-', '')), '스마트 팩토리', NOW(), NOW(), '설계', NOW(), NOW(), '스마트 팩토리 구현', 0),
    (UNHEX(REPLACE('0197893a-ad34-7fbc-8898-1e3908a63f16', '-', '')), '이커머스 플랫폼', NOW(), NOW(), '개발', NOW(), NOW(), '이커머스 구축', 0);


INSERT INTO project_step (id, project_id, title, order_num, created_at)
VALUES
-- ERP
(UNHEX(REPLACE('0197893a-ad34-7585-b6b8-ce4336b688ad', '-', '')), UNHEX(REPLACE('0197893a-ad34-734c-97d7-2e0dd6429247', '-', '')), '기획', 1, NOW()),
-- 스마트 팩토리
(UNHEX(REPLACE('0197893a-ad34-7f8b-b8d0-a5508c80d874', '-', '')), UNHEX(REPLACE('0197893a-ad34-7936-a45c-8abf499013ae', '-', '')), '설계', 1, NOW()),
-- 이커머스
(UNHEX(REPLACE('0197893c-49c5-7ff8-8b4c-dcec3cedd277', '-', '')), UNHEX(REPLACE('0197893a-ad34-7fbc-8898-1e3908a63f16', '-', '')), '개발', 1, NOW());


-- ERP: 게시글 5개
INSERT INTO post (id, project_step_id, title, company_name, author_name, content, created_at, modified_at, approval, deleted) VALUES
(UNHEX(REPLACE(UUID(), '-', '')), UNHEX(REPLACE('0197893a-ad34-7585-b6b8-ce4336b688ad', '-', '')), 'ERP 글1', 'ERP Corp', '홍길동', '내용입니다.', NOW(), NOW(), '대기', 0),
(UNHEX(REPLACE(UUID(), '-', '')), UNHEX(REPLACE('0197893a-ad34-7585-b6b8-ce4336b688ad', '-', '')), 'ERP 글2', 'ERP Corp', '홍길동', '내용입니다.', NOW(), NOW(), '대기', 0),
(UNHEX(REPLACE(UUID(), '-', '')), UNHEX(REPLACE('0197893a-ad34-7585-b6b8-ce4336b688ad', '-', '')), 'ERP 글3', 'ERP Corp', '홍길동', '내용입니다.', NOW(), NOW(), '대기', 0),
(UNHEX(REPLACE(UUID(), '-', '')), UNHEX(REPLACE('0197893a-ad34-7585-b6b8-ce4336b688ad', '-', '')), 'ERP 글4', 'ERP Corp', '홍길동', '내용입니다.', NOW(), NOW(), '대기', 0),
(UNHEX(REPLACE(UUID(), '-', '')), UNHEX(REPLACE('0197893a-ad34-7585-b6b8-ce4336b688ad', '-', '')), 'ERP 글5', 'ERP Corp', '홍길동', '내용입니다.', NOW(), NOW(), '대기', 0);

-- 스마트 팩토리 3개
INSERT INTO post (id, project_step_id, title, company_name, author_name, content, created_at, modified_at, approval, deleted) VALUES
(UNHEX(REPLACE(UUID(), '-', '')), UNHEX(REPLACE('0197893a-ad34-7f8b-b8d0-a5508c80d874', '-', '')), '팩토리 글1', '팩토리 Inc', '이순신', '내용입니다.', NOW(), NOW(), '완료', 0),
(UNHEX(REPLACE(UUID(), '-', '')), UNHEX(REPLACE('0197893a-ad34-7f8b-b8d0-a5508c80d874', '-', '')), '팩토리 글2', '팩토리 Inc', '이순신', '내용입니다.', NOW(), NOW(), '완료', 0),
(UNHEX(REPLACE(UUID(), '-', '')), UNHEX(REPLACE('0197893a-ad34-7f8b-b8d0-a5508c80d874', '-', '')), '팩토리 글3', '팩토리 Inc', '이순신', '내용입니다.', NOW(), NOW(), '완료', 0);

-- 이커머스 4개
INSERT INTO post (id, project_step_id, title, company_name, author_name, content, created_at, modified_at, approval, deleted) VALUES
(UNHEX(REPLACE(UUID(), '-', '')), UNHEX(REPLACE('0197893c-49c5-7ff8-8b4c-dcec3cedd277', '-', '')), '이커머스 글1', '이커머스 Inc', '장보고', '내용입니다.', NOW(), NOW(), '대기', 0),
(UNHEX(REPLACE(UUID(), '-', '')), UNHEX(REPLACE('0197893c-49c5-7ff8-8b4c-dcec3cedd277', '-', '')), '이커머스 글2', '이커머스 Inc', '장보고', '내용입니다.', NOW(), NOW(), '대기', 0),
(UNHEX(REPLACE(UUID(), '-', '')), UNHEX(REPLACE('0197893c-49c5-7ff8-8b4c-dcec3cedd277', '-', '')), '이커머스 글3', '이커머스 Inc', '장보고', '내용입니다.', NOW(), NOW(), '대기', 0),
(UNHEX(REPLACE(UUID(), '-', '')), UNHEX(REPLACE('0197893c-49c5-7ff8-8b4c-dcec3cedd277', '-', '')), '이커머스 글4', '이커머스 Inc', '장보고', '내용입니다.', NOW(), NOW(), '대기', 0);
