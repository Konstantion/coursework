INSERT INTO public.gear (name, price, weight, category_id, image_bytes, description, creator_id, created_at,
                         deactivate_at, active)
VALUES ('LED Headlamp', 29.99, 150, '2deb444a-fdc4-4949-943e-3fb74a691380', null,
        'High-lumen waterproof LED headlamp for cave exploration.', null,
        now()::timestamptz, null, true),
       ('Cave Lantern', 34.99, 300, '2deb444a-fdc4-4949-943e-3fb74a691380', null,
        'Durable lantern with long battery life for extended trips.', null, now()::timestamptz, null,
        true),
       ('Backup Light', 19.99, 100, '2deb444a-fdc4-4949-943e-3fb74a691380', null, 'Compact, emergency backup light.',
        null, now()::timestamptz,
        null, true);

INSERT INTO public.gear (name, price, weight, category_id, image_bytes, description, creator_id, created_at,
                         deactivate_at, active)
VALUES ('Dynamic Rope', 99.99, 10000, 'd988f011-15c1-43eb-b855-7b5da1aaa98d', null,
        'High-strength, elastic rope for climbing and rappelling.', null, now()::timestamptz, null,
        true),
       ('Climbing Harness', 59.99, 500, 'd988f011-15c1-43eb-b855-7b5da1aaa98d', null,
        'Comfortable, adjustable climbing harness.', null, now()::timestamptz, null,
        true),
       ('Carabiners Set', 29.99, 400, 'd988f011-15c1-43eb-b855-7b5da1aaa98d', null,
        'Set of high-grade steel carabiners.', null, now()::timestamptz, null,
        true);

INSERT INTO public.gear (name, price, weight, category_id, image_bytes, description, creator_id, created_at,
                         deactivate_at, active)
VALUES ('Climbing Shoes', 79.99, 600, 'fd6ca92a-7020-4594-921c-bd3da9b68bbf', null,
        'Specialized shoes for optimal grip and comfort.', null, now()::timestamptz,
        null, true),
       ('Gloves', 25.99, 100, 'fd6ca92a-7020-4594-921c-bd3da9b68bbf', null,
        'Durable gloves for protection and better handling.', null, now()::timestamptz,
        null, true),
       ('Climbing Helmet', 49.99, 500, 'fd6ca92a-7020-4594-921c-bd3da9b68bbf', null,
        'Helmet designed for caving and climbing safety.', null, now()::timestamptz, null,
        true);

INSERT INTO public.gear (name, price, weight, category_id, image_bytes, description, creator_id, created_at,
                         deactivate_at, active)
VALUES ('First Aid Kit', 39.99, 1500, 'ebc3892c-c0db-48e0-8329-a4256bb52947', null,
        'Comprehensive first aid kit for emergency treatment.', null, now()::timestamptz, null,
        true),
       ('Communications Radio', 199.99, 1000, 'ebc3892c-c0db-48e0-8329-a4256bb52947', null,
        'Portable, high-range communications radio for team coordination.', null, now()::timestamptz, null,
        true),
       ('Emergency Beacon', 249.99, 500, 'ebc3892c-c0db-48e0-8329-a4256bb52947', null,
        'GPS-enabled emergency beacon for location tracking.', null, now()::timestamptz, null,
        true);
