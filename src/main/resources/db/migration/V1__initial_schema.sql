-- ============================================================================
-- ScienceLove Database Schema
-- Версия: 1.0
-- Дата: 2026-05-28
-- PostgreSQL 12+
-- ============================================================================

-- ============================================================================
-- ЧАСТЬ 1: ПОДГОТОВКА (расширения и очистка)
-- ============================================================================

-- Включаем необходимые расширения
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";


-- ============================================================================
-- ЧАСТЬ 2: ОСНОВНЫЕ ТАБЛИЦЫ (в порядке зависимостей)
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Таблица: Пользователи (единая для студентов и менторов)
-- ----------------------------------------------------------------------------
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Аутентификация
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email_verified BOOLEAN DEFAULT FALSE,
    
    -- Профиль
    full_name VARCHAR(255) NOT NULL,
    avatar_url TEXT,
    phone VARCHAR(20),
    
    -- Роль и статус
    role VARCHAR(20) CHECK (role IN ('STUDENT', 'MENTOR')) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    last_seen_at TIMESTAMP,
    
    -- Метаданные
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    
    -- Индексы и ограничения
    CONSTRAINT users_email_check CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role_active ON users(role, is_active);

-- ----------------------------------------------------------------------------
-- Таблица: Научные области (иерархическая структура)
-- ----------------------------------------------------------------------------
CREATE TABLE scientific_fields (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Иерархия
    parent_id UUID REFERENCES scientific_fields(id) ON DELETE CASCADE,
    
    -- Основные поля
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    level INT NOT NULL DEFAULT 0,
    
    -- Метаданные для расширения
    synonyms TEXT[],
    related_fields UUID[],
    is_active BOOLEAN DEFAULT TRUE,
    
    -- Статистика
    total_mentors INT DEFAULT 0,
    total_requests INT DEFAULT 0,
    
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_scientific_fields_parent ON scientific_fields(parent_id);
CREATE INDEX idx_scientific_fields_slug ON scientific_fields(slug);
CREATE INDEX idx_scientific_fields_level ON scientific_fields(level);
CREATE INDEX idx_scientific_fields_active ON scientific_fields(is_active) WHERE is_active = TRUE;

-- ----------------------------------------------------------------------------
-- Таблица: Профили менторов
-- ----------------------------------------------------------------------------
CREATE TABLE mentor_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    
    -- Академические данные
    degree VARCHAR(255),
    university VARCHAR(255),
    department VARCHAR(255),
    
    -- Контент профиля
    bio TEXT,
    expertise TEXT[],
    
    -- Рейтинги и статистика
    rating DECIMAL(3,2) DEFAULT 0 CHECK (rating BETWEEN 0 AND 5),
    total_consultations INT DEFAULT 0,
    total_reviews INT DEFAULT 0,
    
    -- Доступность и цены
    hourly_rate DECIMAL(10,2) CHECK (hourly_rate >= 0),
    min_session_minutes INT DEFAULT 30,
    available_days INT[],
    available_hours_start TIME,
    available_hours_end TIME,
    
    -- Premium-статус
    is_premium BOOLEAN DEFAULT FALSE,
    premium_expires_at TIMESTAMP,
    
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_mentor_profiles_premium ON mentor_profiles(is_premium) WHERE is_premium = TRUE;
CREATE INDEX idx_mentor_profiles_rating ON mentor_profiles(rating DESC) WHERE rating > 0;

-- ----------------------------------------------------------------------------
-- Таблица: Связь ментор ↔ научные интересы
-- ----------------------------------------------------------------------------
CREATE TABLE mentor_interests (
    mentor_id UUID REFERENCES mentor_profiles(id) ON DELETE CASCADE,
    field_id UUID REFERENCES scientific_fields(id) ON DELETE CASCADE,
    
    proficiency_level INT CHECK (proficiency_level BETWEEN 1 AND 5) DEFAULT 3,
    years_experience INT,
    publications_count INT,
    
    PRIMARY KEY (mentor_id, field_id),
    CONSTRAINT unique_mentor_field UNIQUE (mentor_id, field_id)
);

CREATE INDEX idx_mentor_interests_field ON mentor_interests(field_id);

-- ----------------------------------------------------------------------------
-- Таблица: Профили студентов
-- ----------------------------------------------------------------------------
CREATE TABLE student_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    
    -- Обучение
    university VARCHAR(255),
    faculty VARCHAR(255),
    course INT CHECK (course BETWEEN 1 AND 6),
    degree_type VARCHAR(50) CHECK (degree_type IN ('bachelor', 'master', 'specialist')),
    
    -- Интересы и цели
    interests TEXT[],
    goals TEXT[],
    
    -- Статистика
    total_requests INT DEFAULT 0,
    total_consultations INT DEFAULT 0,
    
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- ----------------------------------------------------------------------------
-- Таблица: Заявки студентов
-- ----------------------------------------------------------------------------
CREATE TABLE requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID REFERENCES student_profiles(id) ON DELETE CASCADE,
    
    -- Содержание заявки
    title VARCHAR(255) NOT NULL,
    topic TEXT NOT NULL,
    discipline VARCHAR(255) NOT NULL,
    
    -- Сроки и бюджет
    deadline DATE NOT NULL,
    budget DECIMAL(10,2),
    urgency VARCHAR(20) CHECK (urgency IN ('low', 'medium', 'high', 'urgent')),
    
    -- Статус
    status VARCHAR(20) DEFAULT 'OPEN' CHECK (
        status IN ('OPEN', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'ARCHIVED')
    ),
    
    -- Метаданные
    matching_preferences JSONB,
    excluded_mentors UUID[],
    
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    
    CONSTRAINT valid_deadline CHECK (deadline >= CURRENT_DATE)
);

CREATE INDEX idx_requests_status_deadline ON requests(status, deadline);
CREATE INDEX idx_requests_discipline ON requests(discipline);
CREATE INDEX idx_requests_student ON requests(student_id);

-- ----------------------------------------------------------------------------
-- Таблица: Связь заявки <-> научные интересы
-- ----------------------------------------------------------------------------
CREATE TABLE request_fields (
    request_id UUID REFERENCES requests(id) ON DELETE CASCADE,
    field_id UUID REFERENCES scientific_fields(id) ON DELETE CASCADE,
    
    weight DECIMAL(3,2) DEFAULT 1.0 CHECK (weight BETWEEN 0.1 AND 2.0),
    
    PRIMARY KEY (request_id, field_id)
);

CREATE INDEX idx_request_fields_field ON request_fields(field_id);

-- ----------------------------------------------------------------------------
-- Таблица: Результаты матчинга
-- ----------------------------------------------------------------------------
CREATE TABLE matches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    request_id UUID REFERENCES requests(id) ON DELETE CASCADE,
    mentor_id UUID REFERENCES mentor_profiles(id) ON DELETE CASCADE,
    
    match_score DECIMAL(5,4) NOT NULL CHECK (match_score BETWEEN 0 AND 1),
    score_breakdown JSONB,
    algorithm_version VARCHAR(20) DEFAULT '1.0',
    
    is_viewed_by_student BOOLEAN DEFAULT FALSE,
    is_viewed_by_mentor BOOLEAN DEFAULT FALSE,
    
    created_at TIMESTAMP DEFAULT NOW(),
    
    UNIQUE(request_id, mentor_id)
);

CREATE INDEX idx_matches_request_score ON matches(request_id, match_score DESC);
CREATE INDEX idx_matches_mentor ON matches(mentor_id);

-- ----------------------------------------------------------------------------
-- Таблица: Чаты
-- ----------------------------------------------------------------------------
CREATE TABLE chats (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID REFERENCES student_profiles(id) ON DELETE CASCADE,
    mentor_id UUID REFERENCES mentor_profiles(id) ON DELETE CASCADE,
    request_id UUID REFERENCES requests(id),
    
    is_archived BOOLEAN DEFAULT FALSE,
    last_message_at TIMESTAMP,
    unread_count_student INT DEFAULT 0,
    unread_count_mentor INT DEFAULT 0,
    
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    
    UNIQUE(student_id, mentor_id, request_id)
);

CREATE INDEX idx_chats_participants ON chats(student_id, mentor_id);
CREATE INDEX idx_chats_last_message ON chats(last_message_at DESC) WHERE is_archived = FALSE;

-- ----------------------------------------------------------------------------
-- Таблица: Сообщения
-- ----------------------------------------------------------------------------
CREATE TABLE messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chat_id UUID REFERENCES chats(id) ON DELETE CASCADE,
    sender_id UUID REFERENCES users(id) ON DELETE CASCADE,
    
    content TEXT NOT NULL,
    has_attachment BOOLEAN DEFAULT FALSE,
    
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP,
    
    message_type VARCHAR(50) DEFAULT 'text' CHECK (
        message_type IN ('text', 'file', 'meeting_proposal', 'system')
    ),
    metadata JSONB,
    
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_messages_chat ON messages(chat_id, created_at);
CREATE INDEX idx_messages_unread ON messages(chat_id, is_read) WHERE is_read = FALSE;

-- ----------------------------------------------------------------------------
-- Таблица: Встречи (ИСПРАВЛЕНО)
-- ----------------------------------------------------------------------------
CREATE TABLE meetings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chat_id UUID REFERENCES chats(id) ON DELETE CASCADE,
    
    proposed_by UUID REFERENCES users(id) ON DELETE CASCADE,
    
    start_time TIMESTAMP NOT NULL,
    duration_minutes INT NOT NULL CHECK (duration_minutes BETWEEN 15 AND 180),
    
    location_type VARCHAR(20) CHECK (location_type IN ('ONLINE', 'OFFLINE', 'HYBRID')),
    location_details TEXT,
    
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (
        status IN ('PENDING', 'CONFIRMED', 'DECLINED', 'COMPLETED', 'CANCELLED', 'RESCHEDULED')
    ),
    
    reminder_sent BOOLEAN DEFAULT FALSE,
    recording_url TEXT,
    notes TEXT,
    
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    
    CONSTRAINT valid_meeting_time CHECK (start_time > NOW())
);

-- Индексы
CREATE INDEX idx_meetings_chat_status ON meetings(chat_id, status);
CREATE INDEX idx_meetings_upcoming ON meetings(start_time) 
    WHERE status IN ('PENDING', 'CONFIRMED');


-- ----------------------------------------------------------------------------
-- Таблица: Файлы
-- ----------------------------------------------------------------------------
CREATE TABLE files (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    uploaded_by UUID REFERENCES users(id) ON DELETE CASCADE,
    
    original_name VARCHAR(255) NOT NULL,
    stored_path VARCHAR(500) NOT NULL,
    mime_type VARCHAR(100),
    size_bytes BIGINT NOT NULL,
    checksum VARCHAR(64),
    
    context_type VARCHAR(50),
    context_id UUID,
    
    is_public BOOLEAN DEFAULT FALSE,
    access_expires_at TIMESTAMP,
    
    uploaded_at TIMESTAMP DEFAULT NOW(),
    
    CONSTRAINT valid_mime CHECK (mime_type ~* '^(image|application|text)/')
);

CREATE INDEX idx_files_context ON files(context_type, context_id);
CREATE INDEX idx_files_uploaded_by ON files(uploaded_by, uploaded_at DESC);

-- ----------------------------------------------------------------------------
-- Таблица: Уведомления
-- ----------------------------------------------------------------------------
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    
    target_type VARCHAR(50),
    target_id UUID,
    
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP,
    
    actions JSONB,
    
    created_at TIMESTAMP DEFAULT NOW(),
    
    CONSTRAINT valid_target CHECK (
        (target_type IS NULL AND target_id IS NULL) OR 
        (target_type IS NOT NULL AND target_id IS NOT NULL)
    )
);

CREATE INDEX idx_notifications_user_unread ON notifications(user_id, is_read) WHERE is_read = FALSE;
CREATE INDEX idx_notifications_created ON notifications(user_id, created_at DESC);

-- ----------------------------------------------------------------------------
-- Таблица: Версии алгоритмов матчинга
-- ----------------------------------------------------------------------------
CREATE TABLE matching_algorithms (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version VARCHAR(20) UNIQUE NOT NULL,
    
    config JSONB NOT NULL,
    description TEXT,
    
    is_active BOOLEAN DEFAULT FALSE,
    is_default BOOLEAN DEFAULT FALSE,
    
    total_matches INT DEFAULT 0,
    avg_conversion_rate DECIMAL(5,4),
    
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- ----------------------------------------------------------------------------
-- Таблица: Логи матчинга
-- ----------------------------------------------------------------------------
CREATE TABLE matching_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    request_id UUID REFERENCES requests(id),
    algorithm_version VARCHAR(20),
    
    input_snapshot JSONB,
    
    candidates_count INT,
    top_match_score DECIMAL(5,4),
    execution_time_ms INT,
    
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_matching_logs_request ON matching_logs(request_id);

-- ============================================================================
-- ЧАСТЬ 3: ФУНКЦИИ И ПРЕДСТАВЛЕНИЯ
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Рекурсивное представление для иерархии научных тем
-- ----------------------------------------------------------------------------
--
CREATE OR REPLACE VIEW scientific_fields_tree AS
WITH RECURSIVE field_path AS (
    SELECT 
        id, 
        name, 
        slug, 
        parent_id, 
        level,
        ARRAY[name::TEXT] AS path_names,
        ARRAY[id] AS path_ids
    FROM scientific_fields
    WHERE parent_id IS NULL
    
    UNION ALL
    
    SELECT 
        sf.id, 
        sf.name, 
        sf.slug, 
        sf.parent_id, 
        sf.level,
        fp.path_names || ARRAY[sf.name::TEXT],
        fp.path_ids || ARRAY[sf.id]
    FROM scientific_fields sf
    INNER JOIN field_path fp ON sf.parent_id = fp.id
)
SELECT * FROM field_path;

-- ----------------------------------------------------------------------------
-- Функция: Получить все дочерние темы (для матчинга)
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION get_all_descendant_fields(field_id UUID)
RETURNS TABLE(id UUID, name VARCHAR, slug VARCHAR, level INT) AS $$
BEGIN
    RETURN QUERY
    WITH RECURSIVE descendants AS (
        SELECT id, name, slug, level, parent_id
        FROM scientific_fields
        WHERE id = field_id
        
        UNION ALL
        
        SELECT sf.id, sf.name, sf.slug, sf.level, sf.parent_id
        FROM scientific_fields sf
        INNER JOIN descendants d ON sf.parent_id = d.id
        WHERE sf.is_active = TRUE
    )
    SELECT id, name, slug, level FROM descendants;
END;
$$ LANGUAGE plpgsql;

-- ----------------------------------------------------------------------------
-- Функция: Автоматическое обновление updated_at
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Триггеры для авто-обновления updated_at
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_scientific_fields_updated_at BEFORE UPDATE ON scientific_fields
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_mentor_profiles_updated_at BEFORE UPDATE ON mentor_profiles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_student_profiles_updated_at BEFORE UPDATE ON student_profiles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_requests_updated_at BEFORE UPDATE ON requests
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_chats_updated_at BEFORE UPDATE ON chats
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_meetings_updated_at BEFORE UPDATE ON meetings
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- ЧАСТЬ 4: НАЧАЛЬНЫЕ ДАННЫЕ (научные темы)
-- ============================================================================

-- Корневые категории
INSERT INTO scientific_fields (name, slug, level, is_active) VALUES
('Естественные науки', 'natural-sciences', 0, TRUE),
('Технические науки', 'engineering', 0, TRUE),
('Гуманитарные науки', 'humanities', 0, TRUE),
('Медицинские науки', 'medical', 0, TRUE),
('Социальные науки', 'social', 0, TRUE);

-- Физика (дочерняя Естественных наук)
INSERT INTO scientific_fields (name, slug, parent_id, level, synonyms, is_active) VALUES
('Физика', 'physics', 
 (SELECT id FROM scientific_fields WHERE slug = 'natural-sciences'),
 1, ARRAY['физические науки'], TRUE);

-- Химия
INSERT INTO scientific_fields (name, slug, parent_id, level, synonyms, is_active) VALUES
('Химия', 'chemistry', 
 (SELECT id FROM scientific_fields WHERE slug = 'natural-sciences'),
 1, ARRAY['химические науки'], TRUE);

-- Биология
INSERT INTO scientific_fields (name, slug, parent_id, level, synonyms, is_active) VALUES
('Биология', 'biology', 
 (SELECT id FROM scientific_fields WHERE slug = 'natural-sciences'),
 1, ARRAY['биологические науки'], TRUE);

-- Ядерная физика
INSERT INTO scientific_fields (name, slug, parent_id, level, synonyms, is_active) VALUES
('Ядерная физика', 'nuclear-physics', 
 (SELECT id FROM scientific_fields WHERE slug = 'physics'),
 2, ARRAY['ядерные реакции', 'атомная физика'], TRUE);

-- Термоядерный синтез
INSERT INTO scientific_fields (name, slug, parent_id, level, synonyms, is_active) VALUES
('Термоядерный синтез', 'fusion', 
 (SELECT id FROM scientific_fields WHERE slug = 'nuclear-physics'),
 3, 
 ARRAY['термояд', 'fusion energy', 'плазменный синтез'],
 TRUE);

-- IT / Программирование
INSERT INTO scientific_fields (name, slug, parent_id, level, synonyms, is_active) VALUES
('Информатика', 'computer-science', 
 (SELECT id FROM scientific_fields WHERE slug = 'engineering'),
 1, ARRAY['IT', 'программирование'], TRUE);

INSERT INTO scientific_fields (name, slug, parent_id, level, synonyms, is_active) VALUES
('Машинное обучение', 'machine-learning', 
 (SELECT id FROM scientific_fields WHERE slug = 'computer-science'),
 2, 
 ARRAY['ML', 'AI', 'искусственный интеллект'],
 TRUE);

-- Экономика
INSERT INTO scientific_fields (name, slug, parent_id, level, synonyms, is_active) VALUES
('Экономика', 'economics', 
 (SELECT id FROM scientific_fields WHERE slug = 'social'),
 1, ARRAY['экономические науки'], TRUE);

-- ============================================================================
-- ЧАСТЬ 5: ПРОВЕРОЧНЫЕ ЗАПРОСЫ
-- ============================================================================

-- Проверка количества таблиц
-- SELECT COUNT(*) AS table_count FROM information_schema.tables 
-- WHERE table_schema = 'public' AND table_type = 'BASE TABLE';

-- Проверка научных тем
-- SELECT name, slug, level FROM scientific_fields ORDER BY level, name;

-- Проверка иерархии (дерево тем)
-- SELECT * FROM scientific_fields_tree ORDER BY level, name;

-- ============================================================================
-- КОНЕЦ СКРИПТА
-- ============================================================================
