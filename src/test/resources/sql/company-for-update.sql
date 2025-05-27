-- 회사 ID 먼저 생성
INSERT INTO company_id VALUES (UNHEX('0196f7a610b67123a2dc32c3861ea55e'));

-- 회사 데이터 생성
INSERT INTO company (
    id,
    name,
    detail,
    business_number,
    address,
    type,
    contact_phone_number,
    contact_email,
    logo_image_path,
    created_at,
    modified_at,
    deleted
) VALUES (
    UNHEX('0196f7a610b67123a2dc32c3861ea55e'),
     '테스트 회사',
     '회사 상세 설명',
     '123-45-67890',
     '서울시 강남구 테헤란로 123',
     'DEV',
     '02-1234-5678',
     'test@company.com',
     '/image/url',
     NOW(),
     NOW(),
     FALSE
 );
