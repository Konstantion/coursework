INSERT INTO public."table" (name, capacity, table_type, hall_id, order_id, created_at, deleted_at, password, active)
VALUES ('Table', 10, 'COMMON', null, null, now()::timestamptz, null, crypt('111', gen_salt('bf', 8)), true);