ALTER TABLE public.equipment
    ADD CONSTRAINT fk_order_on_bill FOREIGN KEY (bill_id) REFERENCES public.log ON DELETE SET NULL;