-- V3__financial_integrity.sql
-- Phase C1: Saldos derivados, transferências como transações, arquivamento de contas

-- 1. Campos de transferência em transactions
ALTER TABLE transactions ADD COLUMN transfer_group_id UUID;
ALTER TABLE transactions ADD COLUMN transfer_direction VARCHAR(3);
CREATE INDEX idx_transactions_transfer_group ON transactions(transfer_group_id);

-- 2. Arquivamento de contas (soft-delete)
ALTER TABLE accounts ADD COLUMN archived_at TIMESTAMP;

-- 3. Remover colunas de saldo mutável (agora derivados)
ALTER TABLE accounts DROP COLUMN current_balance;
ALTER TABLE credit_cards DROP COLUMN available_limit;

-- 4. Categoria de sistema: Pagamento de Fatura
INSERT INTO categories (id, name, icon, color, type, is_system_default) VALUES
  ('00000000-0000-0000-0000-00000000000B', 'Pagamento de Fatura', 'CreditCard', '#6366F1', 'EXPENSE', TRUE);

-- 5. Categoria de sistema: Transferência
INSERT INTO categories (id, name, icon, color, type, is_system_default) VALUES
  ('00000000-0000-0000-0000-00000000000C', 'Transferência', 'ArrowLeftRight', '#8B5CF6', 'EXPENSE', TRUE);

-- 6. Índices para queries de cálculo de saldo
CREATE INDEX idx_transactions_account_type_status ON transactions(account_id, type, status);
CREATE INDEX idx_invoices_card_status ON credit_card_invoices(credit_card_id, status);
