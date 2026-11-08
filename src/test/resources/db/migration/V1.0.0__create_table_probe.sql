CREATE TABLE probe (
    id              UUID DEFAULT gen_random_uuid() NOT NULL,
    tax_code        VARCHAR(16) NOT NULL,
    last_name       VARCHAR(40) NOT NULL,
    version         BIGINT NOT NULL,
    created_by      TEXT NOT NULL,
    created_at      TIMESTAMPTZ DEFAULT now() NOT NULL,
    updated_by      TEXT NOT NULL,
    updated_at      TIMESTAMPTZ DEFAULT now() NOT NULL,
    CONSTRAINT probe_pk PRIMARY KEY (id)
);
