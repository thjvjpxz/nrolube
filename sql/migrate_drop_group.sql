-- Migration: Thêm drop_group vào mob_reward để phân nhóm drop
-- Phase 1: Game Backend + DB Migration
-- ==========================================
-- CẢNH BÁO: Script này chỉ chạy 1 LẦN cho mỗi DB.
-- Nếu cần chạy lại, phải DROP COLUMN và INDEX trước.
-- Các UPDATE backfill phía dưới đã an toàn chạy lại.
-- ==========================================

-- Step 1: Thêm cột drop_group (có guard chống chạy lại)
-- Nếu cột đã tồn tại, ALTER TABLE bị skip.
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'mob_reward'
    AND COLUMN_NAME = 'drop_group');

SET @alter_sql = IF(@col_exists = 0,
    'ALTER TABLE `mob_reward` ADD COLUMN `drop_group` VARCHAR(32) NOT NULL DEFAULT ''NORMAL'' AFTER `condition_type`, ADD INDEX `idx_mob_reward_drop_group` (`drop_group`)',
    'SELECT ''[SKIP] drop_group column already exists'' AS msg');

PREPARE stmt FROM @alter_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Step 2: Backfill dữ liệu cũ theo thứ tự ưu tiên (ghi đè ít -> nhiều)
-- event_key IS NOT NULL -> EVENT
UPDATE mob_reward SET drop_group = 'EVENT' WHERE event_key IS NOT NULL;

-- condition_type IS NOT NULL -> SPECIAL (có thể ghi đè EVENT ở bước trên, nhưng event có condition nên ưu tiên EVENT)
UPDATE mob_reward SET drop_group = 'SPECIAL' WHERE condition_type IS NOT NULL AND drop_group = 'NORMAL';

-- item_template.type = 9 -> GOLD
UPDATE mob_reward mr
JOIN item_template it ON mr.item_template_id = it.id
SET mr.drop_group = 'GOLD'
WHERE it.type = 9 AND mr.drop_group = 'NORMAL';

-- item_template.type IN (12, 14, 30) -> GEM
UPDATE mob_reward mr
JOIN item_template it ON mr.item_template_id = it.id
SET mr.drop_group = 'GEM'
WHERE it.type IN (12, 14, 30) AND mr.drop_group = 'NORMAL';

-- item_template.type BETWEEN 0 AND 4 -> EQUIPMENT
UPDATE mob_reward mr
JOIN item_template it ON mr.item_template_id = it.id
SET mr.drop_group = 'EQUIPMENT'
WHERE it.type BETWEEN 0 AND 4 AND mr.drop_group = 'NORMAL';

-- Còn lại -> MATERIAL
UPDATE mob_reward SET drop_group = 'MATERIAL' WHERE drop_group = 'NORMAL';

-- Verify kết quả
SELECT drop_group, COUNT(*) AS cnt FROM mob_reward GROUP BY drop_group ORDER BY cnt DESC;
