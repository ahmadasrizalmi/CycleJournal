-- Cloudflare D1 SQL Schema: monthly_backups table
-- Zero-knowledge encrypted snapshot vault for CycleJournal

CREATE TABLE IF NOT EXISTS monthly_backups (
    user_id TEXT NOT NULL,
    backup_month TEXT NOT NULL, -- Format: 'YYYY-MM' (e.g. '2026-09')
    cipher_payload TEXT NOT NULL, -- Base64 (Salt + IV + Ciphertext + AuthTag)
    payload_hash TEXT NOT NULL, -- SHA-256 hex checksum
    created_at INTEGER NOT NULL, -- Unix timestamp ms
    updated_at INTEGER NOT NULL, -- Unix timestamp ms
    PRIMARY KEY (user_id, backup_month)
);

CREATE INDEX IF NOT EXISTS idx_user_backups ON monthly_backups (user_id, updated_at DESC);
