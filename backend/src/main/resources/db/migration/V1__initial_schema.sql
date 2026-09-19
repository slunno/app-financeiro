-- V1__initial_schema.sql: Schema inicial do Sistema de Gestão Financeira Pessoal Gamificado

CREATE TABLE users (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE user_xp (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    current_level INT NOT NULL DEFAULT 1,
    current_xp BIGINT NOT NULL DEFAULT 0,
    total_xp BIGINT NOT NULL DEFAULT 0,
    streak_days INT NOT NULL DEFAULT 1,
    last_activity_date DATE,
    financial_health_score INT NOT NULL DEFAULT 70,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE categories (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    icon VARCHAR(50) NOT NULL,
    color VARCHAR(20) NOT NULL,
    type VARCHAR(20) NOT NULL,
    is_system_default BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE accounts (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(30) NOT NULL,
    initial_balance DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    current_balance DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    bank_name VARCHAR(100),
    color VARCHAR(20),
    icon VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE credit_cards (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    credit_limit DECIMAL(15, 2) NOT NULL,
    available_limit DECIMAL(15, 2) NOT NULL,
    closing_day INT NOT NULL,
    due_day INT NOT NULL,
    card_brand VARCHAR(50),
    color VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE credit_card_invoices (
    id UUID PRIMARY KEY,
    credit_card_id UUID NOT NULL REFERENCES credit_cards(id) ON DELETE CASCADE,
    reference_month INT NOT NULL,
    reference_year INT NOT NULL,
    total_amount DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    paid_amount DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    due_date DATE NOT NULL,
    closing_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    account_id UUID REFERENCES accounts(id) ON DELETE SET NULL,
    category_id UUID NOT NULL REFERENCES categories(id),
    credit_card_id UUID REFERENCES credit_cards(id) ON DELETE SET NULL,
    credit_card_invoice_id UUID REFERENCES credit_card_invoices(id) ON DELETE SET NULL,
    description VARCHAR(200) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    date DATE NOT NULL,
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    payment_method VARCHAR(30) NOT NULL,
    is_recurring BOOLEAN NOT NULL DEFAULT FALSE,
    recurrence_period VARCHAR(20),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE installments (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    transaction_id UUID REFERENCES transactions(id) ON DELETE SET NULL,
    credit_card_id UUID REFERENCES credit_cards(id) ON DELETE SET NULL,
    description VARCHAR(200) NOT NULL,
    total_amount DECIMAL(15, 2) NOT NULL,
    installment_amount DECIMAL(15, 2) NOT NULL,
    current_installment INT NOT NULL,
    total_installments INT NOT NULL,
    start_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE budgets (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id UUID NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    max_amount DECIMAL(15, 2) NOT NULL,
    spent_amount DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    "month" INT NOT NULL,
    "year" INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT uk_user_category_month_year UNIQUE (user_id, category_id, "month", "year")
);

CREATE TABLE goals (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    target_amount DECIMAL(15, 2) NOT NULL,
    current_amount DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    target_date DATE,
    category_icon VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE goal_contributions (
    id UUID PRIMARY KEY,
    goal_id UUID NOT NULL REFERENCES goals(id) ON DELETE CASCADE,
    amount DECIMAL(15, 2) NOT NULL,
    contribution_date DATE NOT NULL,
    notes VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE achievements (
    id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    icon VARCHAR(50) NOT NULL,
    category VARCHAR(30) NOT NULL,
    xp_reward BIGINT NOT NULL,
    condition_type VARCHAR(50) NOT NULL,
    condition_threshold BIGINT NOT NULL
);

CREATE TABLE user_achievements (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    achievement_id UUID NOT NULL REFERENCES achievements(id) ON DELETE CASCADE,
    unlocked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_achievement UNIQUE (user_id, achievement_id)
);

CREATE TABLE missions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    frequency VARCHAR(20) NOT NULL,
    difficulty VARCHAR(20) NOT NULL,
    xp_reward BIGINT NOT NULL,
    target_count INT NOT NULL,
    current_count INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(30) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    reference_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes de alta performance
CREATE INDEX idx_transactions_user_date ON transactions(user_id, date);
CREATE INDEX idx_transactions_user_account ON transactions(user_id, account_id);
CREATE INDEX idx_transactions_user_card ON transactions(user_id, credit_card_id);
CREATE INDEX idx_budgets_user_month_year ON budgets(user_id, "month", "year");
CREATE INDEX idx_notifications_user_read ON notifications(user_id, is_read);
CREATE INDEX idx_invoices_card_date ON credit_card_invoices(credit_card_id, reference_year, reference_month);

-- Seed de Categorias Padrão
INSERT INTO categories (id, name, icon, color, type, is_system_default) VALUES
    ('00000000-0000-0000-0000-000000000001', 'Alimentação & Restaurantes', 'Utensils', '#EF4444', 'EXPENSE', TRUE),
    ('00000000-0000-0000-0000-000000000002', 'Moradia & Contas', 'Home', '#F59E0B', 'EXPENSE', TRUE),
    ('00000000-0000-0000-0000-000000000003', 'Transporte & Combustível', 'Car', '#3B82F6', 'EXPENSE', TRUE),
    ('00000000-0000-0000-0000-000000000004', 'Saúde & Cuidados', 'HeartPulse', '#EC4899', 'EXPENSE', TRUE),
    ('00000000-0000-0000-0000-000000000005', 'Lazer & Entretenimento', 'Gamepad2', '#8B5CF6', 'EXPENSE', TRUE),
    ('00000000-0000-0000-0000-000000000006', 'Educação & Cursos', 'GraduationCap', '#10B981', 'EXPENSE', TRUE),
    ('00000000-0000-0000-0000-000000000007', 'Salário & Renda Fixa', 'Briefcase', '#22C55E', 'INCOME', TRUE),
    ('00000000-0000-0000-0000-000000000008', 'Investimentos & Rendimentos', 'TrendingUp', '#06B6D4', 'INCOME', TRUE),
    ('00000000-0000-0000-0000-000000000009', 'Outras Entradas', 'PlusCircle', '#84CC16', 'INCOME', TRUE),
    ('00000000-0000-0000-0000-00000000000A', 'Outras Despesas', 'MinusCircle', '#64748B', 'EXPENSE', TRUE);

-- Seed de Conquistas Globais (Achievements)
INSERT INTO achievements (id, code, title, description, icon, category, xp_reward, condition_type, condition_threshold) VALUES
    ('10000000-0000-0000-0000-000000000001', 'FIRST_TRANSACTION', 'Primeiros Passos', 'Registre seu primeiro lançamento financeiro na plataforma', 'Footprints', 'DISCIPLINE', 50, 'TRANSACTION_COUNT', 1),
    ('10000000-0000-0000-0000-000000000002', 'STREAK_7_DAYS', 'Disciplinado', 'Mantenha um streak de 7 dias consecutivos acompanhando suas finanças', 'Flame', 'STREAK', 150, 'STREAK_DAYS', 7),
    ('10000000-0000-0000-0000-000000000003', 'STREAK_30_DAYS', 'Mestre do Hábito', 'Mantenha 30 dias consecutivos de acompanhamento financeiro', 'Trophy', 'STREAK', 500, 'STREAK_DAYS', 30),
    ('10000000-0000-0000-0000-000000000004', 'BUDGET_SAVER', 'Guardião do Orçamento', 'Conclua um mês com todas as categorias dentro do orçamento planejado', 'ShieldCheck', 'BUDGETING', 300, 'BUDGETS_MET', 1),
    ('10000000-0000-0000-0000-000000000005', 'FIRST_GOAL_COMPLETED', 'Meta Conquistada', 'Alcance 100% da sua primeira meta financeira', 'Target', 'GOALS', 400, 'GOALS_COMPLETED', 1),
    ('10000000-0000-0000-0000-000000000006', 'ON_TIME_PAYER', 'Pontualidade Britânica', 'Pague 5 contas/faturas antes do dia do vencimento', 'ClockCheck', 'DISCIPLINE', 200, 'ON_TIME_PAYMENTS', 5);
