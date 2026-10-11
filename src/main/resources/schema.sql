-- ==========================================
-- Supabase / PostgreSQL schema (with relationships)
-- ==========================================

-- 1. USERS
CREATE TABLE IF NOT EXISTS public.users (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP(6) DEFAULT now(),
    display_name VARCHAR(255),
    password_hash VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL UNIQUE
);

-- 2. CONTACTS (user -> contact)
CREATE TABLE IF NOT EXISTS public.contacts (
    contact_id UUID NOT NULL,
    user_id UUID NOT NULL,
    added_at TIMESTAMP(6) DEFAULT now(),
    PRIMARY KEY (contact_id, user_id),
    CONSTRAINT fk_contacts_contact
        FOREIGN KEY (contact_id) REFERENCES public.users(id) ON DELETE CASCADE,
    CONSTRAINT fk_contacts_user
        FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE,
    CONSTRAINT chk_contacts_not_self CHECK (contact_id <> user_id)
);
CREATE INDEX IF NOT EXISTS idx_contacts_user ON public.contacts(user_id);

-- 3. CONVERSATIONS (1:1 chat)
CREATE TABLE IF NOT EXISTS public.conversations (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP(6) DEFAULT now(),
    user_a_id UUID NOT NULL,
    user_b_id UUID NOT NULL,
    CONSTRAINT uq_conversations_users UNIQUE (user_a_id, user_b_id),
    CONSTRAINT fk_conv_user_a
        FOREIGN KEY (user_a_id) REFERENCES public.users(id) ON DELETE CASCADE,
    CONSTRAINT fk_conv_user_b
        FOREIGN KEY (user_b_id) REFERENCES public.users(id) ON DELETE CASCADE,
    -- always store the smaller UUID in user_a_id, so (A,B) and (B,A) can't both exist
    CONSTRAINT chk_conv_order CHECK (user_a_id < user_b_id)
);
CREATE INDEX IF NOT EXISTS idx_conv_user_b ON public.conversations(user_b_id);

-- 4. GROUPS
CREATE TABLE IF NOT EXISTS public.groups (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP(6) DEFAULT now(),
    created_by UUID,
    name VARCHAR(100) NOT NULL,
    CONSTRAINT fk_groups_creator
        FOREIGN KEY (created_by) REFERENCES public.users(id) ON DELETE SET NULL
);

-- 5. GROUP MEMBERS
CREATE TABLE IF NOT EXISTS public.group_members (
    group_id UUID NOT NULL,
    user_id UUID NOT NULL,
    joined_at TIMESTAMP(6) DEFAULT now(),
    PRIMARY KEY (group_id, user_id),
    CONSTRAINT fk_gm_group
        FOREIGN KEY (group_id) REFERENCES public.groups(id) ON DELETE CASCADE,
    CONSTRAINT fk_gm_user
        FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_gm_user ON public.group_members(user_id);

-- 6. MESSAGES
CREATE TABLE IF NOT EXISTS public.messages (
    id UUID PRIMARY KEY,
    content VARCHAR NOT NULL,
    conversation_id UUID,
    created_at TIMESTAMP(6) DEFAULT now(),
    delivery_status VARCHAR(20),
    group_id UUID,
    media_checksum VARCHAR(64),
    media_file_name VARCHAR(255),
    media_mime_type VARCHAR(100),
    media_size_bytes BIGINT,
    sender_id UUID NOT NULL,
    type VARCHAR(20) NOT NULL,
    CONSTRAINT fk_msg_conversation
        FOREIGN KEY (conversation_id) REFERENCES public.conversations(id) ON DELETE CASCADE,
    CONSTRAINT fk_msg_group
        FOREIGN KEY (group_id) REFERENCES public.groups(id) ON DELETE CASCADE,
    CONSTRAINT fk_msg_sender
        FOREIGN KEY (sender_id) REFERENCES public.users(id),
    -- a message belongs to exactly one target: a conversation OR a group
    CONSTRAINT chk_msg_target CHECK (
        (conversation_id IS NOT NULL AND group_id IS NULL) OR
        (conversation_id IS NULL AND group_id IS NOT NULL)
    )
);
CREATE INDEX IF NOT EXISTS idx_msg_conv_time  ON public.messages(conversation_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_msg_group_time ON public.messages(group_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_msg_sender     ON public.messages(sender_id);
