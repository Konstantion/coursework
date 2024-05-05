CREATE TABLE IF NOT EXISTS public.category
(
    id
         UUID
                      NOT
                          NULL
        DEFAULT
            gen_random_uuid
            (
            ),
    name VARCHAR(255) NOT NULL,
    creator_id UUID,
    CONSTRAINT pk_category PRIMARY KEY
        (
         id
            ),
    CONSTRAINT fk_category_on_user FOREIGN KEY
        (
         creator_id
            ) REFERENCES public.user ON DELETE SET NULL
);