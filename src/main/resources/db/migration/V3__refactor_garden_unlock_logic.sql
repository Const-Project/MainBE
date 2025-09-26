-- users 테이블에 정원 해금 카운트 컬럼 추가하고 기본값을 1로 설정
ALTER TABLE users ADD COLUMN unlockable_garden_count INT NOT NULL DEFAULT 1;

-- wish_tree 테이블에서 더 이상 사용하지 않는 is_unlockable 컬럼 삭제
ALTER TABLE wish_tree DROP COLUMN is_unlockable;
