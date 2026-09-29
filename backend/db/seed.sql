INSERT INTO 	
Users (name, email, phone, address, password, usertype)
VALUES 
('Anthony Kieu', 'anthony.k.130550@gmail.com', '11231231234', 'One Washington Square, San Jose, CA 95192 ', 'SeedPasswordTestRun', 'customer'),
('Nyi Htet', 'nyi.htet@sjsu.edu', '11231231234', 'One Washington Square, San Jose, CA 95192 ',  'SeedPasswordTestRun', 'employee'),
('Johnathan Aye', 'johnathan.aye@sjsu.edu', '11231231234', 'One Washington Square, San Jose, CA 95192 ', 'SeedPasswordTestRun', 'manager');

INSERT INTO
	PaymentMethod(card_number, security_pin, expiration_date, card_type, billing_name)
VALUES
	('1234567891011121', '123', '2028-12-30', 'Visa', 'Anthony Kieu'),
	('5555555555554444', '456', '2029-03-31', 'Mastercard', 'Nyi Htet'),
('378282246310005', '789', '2027-11-30', 'American Express', 'Johnathan Aye');

INSERT INTO 
	Saves(card_number, customer_id)
VALUES
	('1234567891011121', 1),
	('5555555555554444', 2),
	('378282246310005', 3);

INSERT INTO Product
	(name, description, weight, price, image_url, quantity)
VALUES
	('Apple', 'fresh apples from California', 0.400, 1.00, 'https://marketplace.canva.com/Pk_Vc/MAFsWnPk_Vc/1/tl/canva-red-apple-fruit-MAFsWnPk_Vc.png', 100),
	('Carrot', 'fresh carrots', 0.220, 1.40, 'https://img.magnific.com/free-psd/vibrant-orange-carrot-with-fresh-green-tops-isolated-against-black-background_84443-58548.jpg?semt=ais_hybrid&w=740&q=80', 400),
	('Toast' , 'toast from local San Jose bakery', 1.500, 5.45, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSUQFfQYNYlHwLNSL40TDXQVT0_2VuZaFI2H6GVUjDzYNkwGxm3TEEnyOU&s=10', 80),
	('Avocado', 'fresh avocados from Mexico', 0.415, 1.2, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTWnciocchU11uhY1RhzfJGnMcqTTxOqvzWUoV-STShwtO7L5P5s8rrrJI&s=10', 200),
	('Banana', 'a hand of bananas from Brazil', 3.512, 5.12, 'https://img.magnific.com/free-vector/vector-ripe-yellow-banana-bunch-isolated-white-background_1284-45456.jpg?semt=ais_hybrid&w=740&q=80', 100),
	('Tomato', 'tomato grown in California', 0.333, 1.00, 'https://img.magnific.com/free-vector/vector-ripe-yellow-banana-bunch-isolated-white-background_1284-45456.jpg?semt=ais_hybrid&w=740&q=80', 30),
	('Milk', '1 gallon of milk', 8.601, 6.13, 'https://media.istockphoto.com/id/496681092/photo/gallon-milk-bottle-with-blue-cap-isolated-on-white.jpg?s=612x612&w=0&k=20&c=vFIn8_k0QG118eoZIrFvVsPd5gcPMagYrGeIOfz6EsU=', 40),
	('Romaine Lettuce', 'a fresh head of lettuce from eastern US', 1.753, 3.10, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQBP1rl94vEtDlHNlZO6_1EtQeahXfHWShaa-gevFyR3Q&s=10', 100);

/*
Orders, OrderItem, ShoppingCart, CartItem, DeliveryTask, Updates don't need any initial data to get the program working
*/
