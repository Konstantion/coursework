CREATE TABLE public.order_product
(
    order_id   UUID NOT NULL,
    product_id UUID NOT NULL,
    CONSTRAINT fk_order_product_on_order FOREIGN KEY (order_id) REFERENCES public.equipment ON DELETE CASCADE,
    CONSTRAINT fk_order_product_on_product FOREIGN KEY (product_id) REFERENCES public.gear ON DELETE CASCADE
);