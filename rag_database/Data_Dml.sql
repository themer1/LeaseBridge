-- public.accounts definition

-- Drop table

-- DROP TABLE public.accounts;

CREATE TABLE public.accounts (
	account_uuid uuid DEFAULT gen_random_uuid() NOT NULL,
	account_id bpchar(12) NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	CONSTRAINT accounts_account_id_key UNIQUE (account_id),
	CONSTRAINT accounts_pkey PRIMARY KEY (account_uuid),
	CONSTRAINT ck_account_id_format CHECK ((account_id ~ '^[A-Z]{4}[0-9]{8}$'::text))
);


-- public.contract_chunks definition

-- Drop table

-- DROP TABLE public.contract_chunks;

CREATE TABLE public.contract_chunks (
	id uuid NOT NULL,
	envelope_id text NOT NULL,
	document_id text NOT NULL,
	chunk_index int4 NOT NULL,
	"text" text NOT NULL,
	embedding public.vector NULL,
	sha256 text NOT NULL,
	created_at timestamptz DEFAULT now() NULL,
	topic varchar NULL,
	CONSTRAINT contract_chunks_pkey PRIMARY KEY (id),
	CONSTRAINT contract_chunks_sha256_key UNIQUE (sha256)
);
CREATE INDEX contract_chunks_env_idx ON public.contract_chunks USING btree (envelope_id);
CREATE INDEX contract_chunks_vec_idx ON public.contract_chunks USING ivfflat (embedding vector_cosine_ops);


-- public.contract_documents definition

-- Drop table

-- DROP TABLE public.contract_documents;

CREATE TABLE public.contract_documents (
	envelope_id text NOT NULL,
	document_id text NOT NULL,
	document_name text NULL,
	saved_path text NULL,
	sha256 text NULL,
	page_count int4 NULL,
	size_bytes int8 NULL,
	created_at timestamptz DEFAULT now() NULL,
	CONSTRAINT contract_documents_pkey PRIMARY KEY (envelope_id, document_id)
);


-- public.users definition

-- Drop table

-- DROP TABLE public.users;

CREATE TABLE public.users (
	"uuid" uuid DEFAULT gen_random_uuid() NOT NULL,
	account_id bpchar(12) NOT NULL,
	username text NOT NULL,
	"password" text NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	CONSTRAINT uq_user_per_account_username UNIQUE (account_id, username),
	CONSTRAINT users_pkey PRIMARY KEY (uuid),
	CONSTRAINT users_account_id_fkey FOREIGN KEY (account_id) REFERENCES public.accounts(account_id) ON DELETE CASCADE
);
CREATE INDEX idx_login_lookup ON public.users USING btree (account_id, username);