CREATE TABLE IF NOT EXISTS public.gear
(
    id
                  UUID
                                   NOT
                                       NULL
                          DEFAULT
                              gen_random_uuid
                              (
                              ),
    name          VARCHAR(255)     NOT NULL,
    price         DOUBLE PRECISION NOT NULL,
    weight        DOUBLE PRECISION,
    category_id   UUID,
    image_bytes   BYTEA,
    description   VARCHAR(255),
    creator_id    UUID,
    created_at    TIMESTAMP WITHOUT TIME ZONE,
    deactivate_at TIMESTAMP
                      WITHOUT TIME ZONE,
    active        BOOLEAN DEFAULT true,
    CONSTRAINT pk_gear PRIMARY KEY
        (
         id
            ),
    CONSTRAINT fk_product_on_category FOREIGN KEY
        (
         category_id
            ) REFERENCES public.category
        ON DELETE SET NULL,
    CONSTRAINT fk_product_on_user FOREIGN KEY
        (
         creator_id
            ) REFERENCES public.user
        ON DELETE SET NULL
);