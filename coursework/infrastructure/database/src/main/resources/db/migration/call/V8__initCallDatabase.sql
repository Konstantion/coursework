CREATE TABLE public.call
(
    id            UUID NOT NULL DEFAULT gen_random_uuid(),
    expedition_id UUID NOT NULL,
    purpose       VARCHAR(255),
    opened_at     TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_call PRIMARY KEY (id),
    CONSTRAINT fk_call_on_expedition FOREIGN KEY (expedition_id) REFERENCES public.expedition ON DELETE CASCADE
);

