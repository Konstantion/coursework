CREATE TABLE IF NOT EXISTS expedition_guide
(
    expedition_id
        UUID
        NOT
            NULL,
    guide_id
        UUID
        NOT
            NULL,
    CONSTRAINT
        fk_expedition_guide_on_expedition
        FOREIGN
            KEY
            (
             expedition_id
                ) REFERENCES public.expedition ON DELETE CASCADE,
    CONSTRAINT fk_expedition_guide_on_user FOREIGN KEY
        (
         guide_id
            ) REFERENCES public.user
        ON DELETE CASCADE
);


