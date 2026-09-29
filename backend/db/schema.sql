CREATE TABLE Users(
user_id INT PRIMARY KEY AUTO_INCREMENT,
name VARCHAR(150) NOT NULL,
email VARCHAR(150) NOT NULL UNIQUE,
phone VARCHAR(20) NOT NULL,
address VARCHAR(300) NOT NULL,
password VARCHAR(150) NOT NULL,
usertype VARCHAR(150) NOT NULL
);

CREATE TABLE PaymentMethod (
    card_number VARCHAR(30) PRIMARY KEY,
    security_pin VARCHAR(5) NOT NULL,
    expiration_date DATE NOT NULL,
    card_type VARCHAR(30) NOT NULL,
    billing_name VARCHAR(100) NOT NULL
);

CREATE TABLE ShoppingCart (
    cart_id INT PRIMARY KEY AUTO_INCREMENT,
    status VARCHAR(100) NOT NULL DEFAULT 'Active',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE 
CURRENT_TIMESTAMP,
    delivery_fee DECIMAL(15,2) NOT NULL DEFAULT 0.00 CHECK (delivery_fee IN (0, 10)),
    total_amount DECIMAL(15,2) NOT NULL DEFAULT 0.00 CHECK (total_amount >= 0),
    weight DECIMAL(15,3) NOT NULL DEFAULT 0.000 CHECK (weight >= 0),
    customer_id INT NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES Users(user_id)
);

CREATE TABLE Product (
    product_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    weight DECIMAL(15,3) NOT NULL CHECK (weight > 0),
    price DECIMAL(15,2) NOT NULL CHECK (price > 0),
    image_url VARCHAR(5000),
    quantity INT NOT NULL DEFAULT 0 CHECK (quantity >= 0)
);

CREATE TABLE CartItem (
    cart_item_id INT PRIMARY KEY AUTO_INCREMENT,
    quantity INT NOT NULL DEFAULT 1 CHECK (quantity > 0),
    weight DECIMAL(15,3) NOT NULL CHECK (weight > 0),
    price DECIMAL(15,2) NOT NULL CHECK (price >= 0),
    cart_id INT NOT NULL,
    product_id INT NOT NULL,
    FOREIGN KEY (cart_id) REFERENCES ShoppingCart(cart_id),
    FOREIGN KEY (product_id) REFERENCES Product(product_id)
);

CREATE TABLE Orders (
    order_id INT PRIMARY KEY AUTO_INCREMENT,
    status VARCHAR(100) NOT NULL DEFAULT 'Pending' CHECK (status IN ('Pending', 'Out For Delivery', 'Delivered', 'Undeliverable', 'Canceled')),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE 
CURRENT_TIMESTAMP,
    delivery_fee DECIMAL(15,2) NOT NULL CHECK (delivery_fee IN (0, 10)),
    total_amount DECIMAL(15,2) NOT NULL CHECK (total_amount > 0),
    weight DECIMAL(15,3) NOT NULL CHECK (weight > 0),
    card_number VARCHAR(30) NOT NULL,
    cart_id INT NOT NULL,
    FOREIGN KEY (card_number) REFERENCES PaymentMethod(card_number),
    FOREIGN KEY (cart_id) REFERENCES ShoppingCart(cart_id)
);

CREATE TABLE OrderItem (
    order_item_id INT PRIMARY KEY AUTO_INCREMENT,
    quantity INT NOT NULL DEFAULT 1 CHECK (quantity > 0),
    weight DECIMAL(15,3) NOT NULL CHECK (weight > 0),
    price DECIMAL(15,2) NOT NULL CHECK (price > 0),
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    FOREIGN KEY (order_id) REFERENCES Orders(order_id),
    FOREIGN KEY (product_id) REFERENCES Product(product_id)
);

CREATE TABLE DeliveryTask (
    task_id INT PRIMARY KEY AUTO_INCREMENT,
    status VARCHAR(100) NOT NULL DEFAULT 'Assigned',
    assigned_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    start_time DATETIME DEFAULT NULL,
    end_time DATETIME DEFAULT NULL,
    delivery_address VARCHAR(500) NOT NULL,
    order_id INT NOT NULL,
    FOREIGN KEY (order_id) REFERENCES Orders(order_id)
);

CREATE TABLE Saves (
    card_number VARCHAR(30) NOT NULL,
    customer_id INT NOT NULL,
    PRIMARY KEY (customer_id, card_number),
    FOREIGN KEY (customer_id) REFERENCES Users(user_id),
    FOREIGN KEY (card_number) REFERENCES PaymentMethod(card_number)
);

CREATE TABLE Updates (
    update_id INT PRIMARY KEY AUTO_INCREMENT,
    product_id INT NOT NULL,
    employee_id INT NOT NULL,
    description VARCHAR(1000) NOT NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE        
        CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES Product(product_id),
    FOREIGN KEY (employee_id) REFERENCES Users(user_id)
);
