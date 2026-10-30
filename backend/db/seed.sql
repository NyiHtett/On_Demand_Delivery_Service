-- all users use the password "P@ssw0rd!" (bcrypt hash below, see passwordGen.java).
-- 
INSERT INTO users (
    user_name,
    email,
    phone,
    address,
    password_hash,
    user_type
)
VALUES
    ('Abe Jun, abe.jun@example.com', '5105554321',
     '44009 Osgood Rd, Fremont, CA 94539',
      '$2a$10$MROHWY27yUHhbx1jRu1IXOSr9ilUJgbkEPCWylhHDNQ5kNpNeuLue','Employee')
    ('Joel East, joel.east@example.com', '6505554321',
     '777 Story Rd, San Jose, CA 95122',
      '$2a$10$MROHWY27yUHhbx1jRu1IXOSr9ilUJgbkEPCWylhHDNQ5kNpNeuLue','Employee')
    ('Po Li, po.li@example.com', '2095554321',
     '3010 W Grant Line Rd, Tracy, CA 95304',
      '$2a$10$MROHWY27yUHhbx1jRu1IXOSr9ilUJgbkEPCWylhHDNQ5kNpNeuLue','Employee')
    -- extra customers at different addresses so robot batches have multi-stop routes
    ('Maria Lopez', 'maria.lopez@example.com', '14085550101',
     '777 The Alameda, San Jose, CA 95126',
     '$2a$10$MROHWY27yUHhbx1jRu1IXOSr9ilUJgbkEPCWylhHDNQ5kNpNeuLue', 'Customer'),
    ('Sam Patel', 'sam.patel@example.com', '14085550102',
     '1000 Lincoln Ave, San Jose, CA 95125',
     '$2a$10$MROHWY27yUHhbx1jRu1IXOSr9ilUJgbkEPCWylhHDNQ5kNpNeuLue', 'Customer'),
    ('Leah Ni, leah.ni@example.com', '2095554321',
     '3010 W Grant Line Rd, Tracy, CA 95304',
      '$2a$10$MROHWY27yUHhbx1jRu1IXOSr9ilUJgbkEPCWylhHDNQ5kNpNeuLue','Customer')
ON DUPLICATE KEY UPDATE
    user_name = VALUES(user_name),
    phone = VALUES(phone),
    address = VALUES(address),
    password_hash = VALUES(password_hash),
    user_type = VALUES(user_type);


INSERT INTO payment_methods (
    card_number,
    security_pin,
    expiration_date,
    card_type,
    billing_name,
    billing_address
)
VALUES
    ('1234567891011121', '123', '2028-12-30', 'Visa',
     'Anthony Kieu', 'One Washington Square, San Jose, CA 95192'),
    ('5555555555554444', '456', '2029-03-31', 'Mastercard',
     'Nyi Htet', 'One Washington Square, San Jose, CA 95192'),
    ('378282246310005', '789', '2027-11-30', 'American Express',
     'Johnathan Aye', 'One Washington Square, San Jose, CA 95192')
ON DUPLICATE KEY UPDATE
    security_pin = VALUES(security_pin),
    expiration_date = VALUES(expiration_date),
    card_type = VALUES(card_type),
    billing_name = VALUES(billing_name),
    billing_address = VALUES(billing_address);


-- only customers save payment methods in the application.
INSERT IGNORE INTO user_payment_methods (user_id, card_number)
SELECT user_id, '1234567891011121'
FROM users
WHERE email = 'anthony.k.130550@gmail.com';


INSERT INTO products (
    product_name,
    product_description,
    unit_weight,
    unit_price,
    image_url,
    quantity
)

VALUES
    ('Apple', 'fresh apples from California', 0.400, 1.00,
     'https://marketplace.canva.com/Pk_Vc/MAFsWnPk_Vc/1/tl/canva-red-apple-fruit-MAFsWnPk_Vc.png', 100),
    ('Carrot', 'fresh carrots', 0.220, 1.40,
     'https://img.magnific.com/free-psd/vibrant-orange-carrot-with-fresh-green-tops-isolated-against-black-background_84443-58548.jpg?semt=ais_hybrid&w=740&q=80', 400),
    ('Toast', 'toast from local San Jose bakery', 1.500, 5.45,
     'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSUQFfQYNYlHwLNSL40TDXQVT0_2VuZaFI2H6GVUjDzYNkwGxm3TEEnyOU&s=10', 80),
    ('Avocado', 'fresh avocados from Mexico', 0.415, 1.20,
     'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTWnciocchU11uhY1RhzfJGnMcqTTxOqvzWUoV-STShwtO7L5P5s8rrrJI&s=10', 200),
    ('Banana', 'a hand of bananas from Brazil', 3.512, 5.12,
     'https://img.magnific.com/free-vector/vector-ripe-yellow-banana-bunch-isolated-white-background_1284-45456.jpg?semt=ais_hybrid&w=740&q=80', 100),
    ('Tomato', 'tomato grown in California', 0.333, 1.00,
     'https://upload.wikimedia.org/wikipedia/commons/8/89/Tomato_je.jpg', 30),
    ('Milk', '1 gallon of milk', 8.601, 6.13,
     'https://media.istockphoto.com/id/496681092/photo/gallon-milk-bottle-with-blue-cap-isolated-on-white.jpg?s=612x612&w=0&k=20&c=vFIn8_k0QG118eoZIrFvVsPd5gcPMagYrGeIOfz6EsU=', 40),
    ('Romaine Lettuce', 'a fresh head of lettuce from eastern US', 1.753, 3.10,
     'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQBP1rl94vEtDlHNlZO6_1EtQeahXfHWShaa-gevFyR3Q&s=10', 100)
ON DUPLICATE KEY UPDATE
    product_description = VALUES(product_description),
    unit_weight = VALUES(unit_weight),
    unit_price = VALUES(unit_price),
    image_url = VALUES(image_url),
    quantity = VALUES(quantity);

-- Anthony's current (Active) cart, so the cart page has something in it.
INSERT INTO shopping_carts (cart_id, customer_id, table_status)
VALUES (1, 1, 'Active');

INSERT INTO cart_items (cart_id, product_id, product_name, unit_weight, unit_price, quantity)
VALUES (1, 1, 'Apple', 0.400, 1.00, 3),
    (1, 2, 'Carrot', 0.220, 1.40, 2);

-- Two past (Completed) orders for Anthony. Each order has its own cart because
-- orders.cart_id is UNIQUE; an Active cart must never already have an order.
INSERT INTO shopping_carts (cart_id, customer_id, table_status)
VALUES (2, 1, 'Completed'),
    (3, 1, 'Completed');

INSERT INTO orders (order_id, customer_id, cart_id, order_status, delivery_address, subtotal, delivery_fee, total_weight, paid_at)
VALUES (1, 1, 3, 'Completed', 'One Washington Square, San Jose, CA 95192', 22.03, 0, 13.601, NOW()),
    (2, 1, 2, 'Completed', 'One Washington Square, San Jose, CA 95192', 7.45, 0, 2.300, NOW());

INSERT INTO order_items (order_id, product_id, product_name, unit_weight, unit_price, quantity)
VALUES (1, 1, 'Apple', 0.400, 1.00, 5),
    (1, 3, 'Toast', 1.500, 5.45, 2),
    (1, 7, 'Milk', 8.601, 6.13, 1),
    (2, 1, 'Apple', 0.400, 1.00, 2),
    (2, 3, 'Toast', 1.500, 5.45, 1);

/*
sessions, shopping_carts, cart_items, orders, order_items,
delivery_tasks, delivery_task_orders, and inventory_updates
do not require initial data for the application to start.
*/
