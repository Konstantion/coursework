CREATE TABLE IF NOT EXISTS public.log
(
    id
               UUID
                                NOT
                                    NULL
                                         DEFAULT
                                             gen_random_uuid
                                             (
                                             ),
    waiter_id  UUID,
    order_id   UUID             NOT NULL UNIQUE,
    guest_id   UUID,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    closed_at  TIMESTAMP
                   WITHOUT TIME ZONE,
    active     BOOLEAN          NOT NULL DEFAULT true,
    price      DOUBLE PRECISION NOT NULL,
    price_with_discount DOUBLE PRECISION NOT NULL,
    CONSTRAINT pk_bill PRIMARY KEY
        (
         id
            ),
    CONSTRAINT fk_bill_order FOREIGN KEY
        (
         order_id
            ) REFERENCES public."equipment"
        ON DELETE CASCADE,
    CONSTRAINT fk_bill_user FOREIGN KEY
        (
         waiter_id
            ) REFERENCES public."user"
        ON DELETE SET NULL,
    CONSTRAINT fk_bill_guest FOREIGN KEY
        (
         guest_id
            ) REFERENCES public."guest"
        ON DELETE SET NULL
);