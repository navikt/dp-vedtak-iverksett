CREATE TABLE IF NOT EXISTS sak
(
    id               TEXT                                                              PRIMARY KEY,
    ident            VARCHAR(11)                                                       NOT NULL,
    opprettet        TIMESTAMP WITH TIME ZONE DEFAULT (NOW() AT TIME ZONE 'utc'::TEXT) NOT NULL,
    endret           TIMESTAMP                                                         NOT NULL
);

CREATE INDEX IF NOT EXISTS sak_ident_idx ON sak (ident);

CREATE TABLE IF NOT EXISTS iverksetting
(
    id               BIGSERIAL                                                         PRIMARY KEY,
    sak_id           TEXT           REFERENCES sak (id)                                NOT NULL,
    vedtak_id        UUID                                                              NOT NULL UNIQUE,
    behandling_id    UUID                                                              NOT NULL,
    vedtakstidspunkt TIMESTAMP                                                         NOT NULL,
    virkningsdato    DATE                                                              NOT NULL,
    utfall           TEXT                                                              NOT NULL,
    opprettet        TIMESTAMP WITH TIME ZONE DEFAULT (NOW() AT TIME ZONE 'utc'::TEXT) NOT NULL
);

CREATE INDEX IF NOT EXISTS iverksetting_sak_idx ON iverksetting (sak_id);
CREATE INDEX IF NOT EXISTS iverksetting_vedtak_idx ON iverksetting (vedtak_id);
CREATE INDEX IF NOT EXISTS iverksetting_behandling_idx ON iverksetting (behandling_id);
CREATE INDEX IF NOT EXISTS iverksetting_vedtakstidspunkt_idx ON iverksetting (sak_id, vedtakstidspunkt);

CREATE TABLE IF NOT EXISTS iverksettingsdag
(
    id               BIGSERIAL                                                         PRIMARY KEY,
    iverksetting_id  BIGINT       REFERENCES iverksetting (id)                         NOT NULL,
    dato             DATE                                                              NOT NULL,
    beløp            DECIMAL                                                           NOT NULL,
    opprettet        TIMESTAMP WITH TIME ZONE DEFAULT (NOW() AT TIME ZONE 'utc'::TEXT) NOT NULL,
    UNIQUE(iverksetting_id, dato)
);

CREATE INDEX IF NOT EXISTS iverksettingsdag_iverksetting_idx ON iverksettingsdag (iverksetting_id);
