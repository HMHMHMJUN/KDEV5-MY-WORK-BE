-- 프로젝트 데이터
INSERT INTO project (id, name, start_at, end_at, step, created_at, modified_at, detail, deleted)
VALUES
(UNHEX(REPLACE('24e203df-4f52-488a-bb23-ab7f9b946737','-','')),
 '개발 프로젝트01', NOW(), NOW(), 'NOT_STARTED', NOW(), NOW(), '고객사의 웹페이지를 구성해주는 프로젝트입니다01', 0),
(UNHEX(REPLACE('bb245db9-8fa5-42be-8aa4-8cb90163a4bd','-','')),
 '개발 프로젝트02', NOW(), NOW(), 'IN_PROGRESS', NOW(), NOW(), '고객사의 웹페이지를 구성해주는 프로젝트입니다02', 0);

-- 프로젝트 할당 데이터
INSERT INTO project_assign (id, project_id, dev_company_id, client_company_id, created_at)
VALUES
(UNHEX(REPLACE('99bc1529-93b6-4f10-838f-46174b9e7b68','-','')),
 UNHEX(REPLACE('24e203df-4f52-488a-bb23-ab7f9b946737','-','')),
 UNHEX(REPLACE('019739eb-cd83-7223-b9b0-f186641aef55','-','')),
 UNHEX(REPLACE('61f60ca6-83ff-4934-ac21-d9f574a0634b','-','')),
 NOW()),
(UNHEX(REPLACE('640d45ae-1941-48b2-9ba3-2a8e7adf8757','-','')),
 UNHEX(REPLACE('bb245db9-8fa5-42be-8aa4-8cb90163a4bd','-','')),
 UNHEX(REPLACE('019739eb-cd83-7223-b9b0-f186641aef55','-','')),
 UNHEX(REPLACE('6eb942ce-21c2-4727-8358-3cd04508c73e','-','')),
 NOW());

-- 프로젝트 단계
INSERT INTO project_step (id, project_id, title, order_num, created_at)
VALUES
(UNHEX(REPLACE('d7813835-06ba-4b21-be19-07ddd20ab9b5','-','')),
 UNHEX(REPLACE('24e203df-4f52-488a-bb23-ab7f9b946737','-','')),
 '기획', 1, NOW()),
(UNHEX(REPLACE('732a2d0d-fa62-4b7c-a9bd-b9614089f188','-','')),
 UNHEX(REPLACE('bb245db9-8fa5-42be-8aa4-8cb90163a4bd','-','')),
 '디자인', 1, NOW());

-- 체크리스트 (기획)
INSERT INTO project_check_list (id, title, project_step_id, approval, created_at, deleted)
VALUES
(UNHEX(REPLACE('d4997f0b-d25f-4a8e-a077-12173feda3f2','-','')),
 '프로젝트 체크 리스트 타이틀1',
 UNHEX(REPLACE('d7813835-06ba-4b21-be19-07ddd20ab9b5','-','')),
 'PENDING', DATE_SUB(NOW(), INTERVAL 3 DAY), false),
(UNHEX(REPLACE('ee4633ac-c741-4bd8-8d67-e16c9b7550cf','-','')),
 '프로젝트 체크 리스트 타이틀2',
 UNHEX(REPLACE('d7813835-06ba-4b21-be19-07ddd20ab9b5','-','')),
 'PENDING', DATE_SUB(NOW(), INTERVAL 6 DAY), false),
(UNHEX(REPLACE('cb4d30dc-7385-4ad4-aae4-d030aad08a10','-','')),
 '프로젝트 체크 리스트 타이틀3',
 UNHEX(REPLACE('d7813835-06ba-4b21-be19-07ddd20ab9b5','-','')),
 'PENDING', DATE_SUB(NOW(), INTERVAL 3 DAY), false);

-- 체크리스트 (디자인)
INSERT INTO project_check_list (id, title, project_step_id, approval, created_at, deleted)
VALUES
(UNHEX(REPLACE('4c1e816a-efe8-4817-9c0f-ba6aa8b706ee','-','')),
 '프로젝트 체크 리스트 타이틀2-1',
 UNHEX(REPLACE('732a2d0d-fa62-4b7c-a9bd-b9614089f188','-','')),
 'PENDING', DATE_SUB(NOW(), INTERVAL 3 DAY), false),
(UNHEX(REPLACE('6345bdcf-823a-4b78-819e-edd6bd61ed87','-','')),
 '프로젝트 체크 리스트 타이틀2-2',
 UNHEX(REPLACE('732a2d0d-fa62-4b7c-a9bd-b9614089f188','-','')),
 'PENDING', DATE_SUB(NOW(), INTERVAL 6 DAY), false),
(UNHEX(REPLACE('9d3e19c8-918b-4ae0-8204-95b0667e3ab7','-','')),
 '프로젝트 체크 리스트 타이틀2-3',
 UNHEX(REPLACE('732a2d0d-fa62-4b7c-a9bd-b9614089f188','-','')),
 'PENDING', DATE_SUB(NOW(), INTERVAL 3 DAY), false);
