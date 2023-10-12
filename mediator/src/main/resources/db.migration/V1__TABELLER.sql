CREATE TABLE IF NOT EXISTS sak
(
    id        TEXT                                                              PRIMARY KEY,
    ident     VARCHAR(11)                                                       NOT NULL,
    opprettet TIMESTAMP WITH TIME ZONE DEFAULT (NOW() AT TIME ZONE 'utc'::TEXT) NOT NULL,
    endret    TIMESTAMP                                                         NOT NULL
);

CREATE INDEX IF NOT EXISTS sak_ident_idx ON sak (ident);

CREATE TABLE IF NOT EXISTS iverksetting
(
    id               BIGSERIAL    PRIMARY KEY,
    sak_id           TEXT         NOT NULL,
    vedtakId         UUID         NOT NULL,
    behandlingId     UUID         NOT NULL,
    vedtakstidspunkt TIMESTAMP    NOT NULL,
    virkningsdato    DATE         NOT NULL,
    utfall           TEXT         NOT NULL,
    opprettet        TIMESTAMP WITH TIME ZONE DEFAULT (NOW() AT TIME ZONE 'utc'::TEXT) NOT NULL
);

CREATE INDEX IF NOT EXISTS iverksetting_sak_idx ON iverksetting (sak_id);
