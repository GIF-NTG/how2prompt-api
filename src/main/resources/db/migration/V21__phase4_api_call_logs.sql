-- =====================================================================
-- PHASE 4 - File 6/6: API Call Logs (US-9.7, Gap 2 - phuong an a)
-- Tach rieng khoi audit_logs vi dac tinh khac nhau: audit_logs ghi
-- HANH DONG nghiep vu nhay cam (tan suat thap, tra cuu theo user/action),
-- con api_call_logs ghi TUNG LAN GOI API (tan suat cao, tra cuu theo
-- api_key + thoi gian, can cot rieng cho endpoint/status_code/latency).
-- BIGSERIAL giong audit_logs: append-only, khong bi FK tro nguoc.
-- =====================================================================

CREATE TABLE api_call_logs (
  id          BIGSERIAL PRIMARY KEY,
  api_key_id  UUID NOT NULL REFERENCES api_keys(id),
  endpoint    VARCHAR(255) NOT NULL,
  method      VARCHAR(10) NOT NULL,
  status_code INT NOT NULL,
  latency_ms  INT,
  created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_api_call_logs_api_key_id_created_at ON api_call_logs (api_key_id, created_at);
