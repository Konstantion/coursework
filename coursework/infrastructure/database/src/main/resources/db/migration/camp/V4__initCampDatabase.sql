CREATE TABLE IF NOT EXISTS public.camp
(
    id
               UUID
                            NOT
                                NULL
                                     default
                                         gen_random_uuid
                                         (
                                         ),
    name       VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    active     BOOLEAN      NOT NULL DEFAULT true,
    CONSTRAINT pk_camp PRIMARY KEY
        (
         id
            )
);