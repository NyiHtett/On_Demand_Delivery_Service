CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    user_name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone VARCHAR(20) DEFAULT NULL,
    user_address VARCHAR(300) DEFAULT NULL,
    password_hash VARCHAR(255) NOT NULL,
    user_type CHAR(8) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CHECK (user_type IN ('Customer', 'Employee'))
);


CREATE TABLE sessions (
    session_id VARCHAR(128) PRIMARY KEY,
    user_id INT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at DATETIME NOT NULL,
    revoked_at DATETIME DEFAULT NULL,

    FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE,

    CHECK (expires_at > created_at)
);


CREATE TABLE payment_methods (
    card_number VARCHAR(30) PRIMARY KEY,
    security_pin VARCHAR(5) NOT NULL,
    expiration_date DATE NOT NULL,
    card_type VARCHAR(30) NOT NULL,
    billing_name VARCHAR(100) NOT NULL,
    billing_address VARCHAR(300) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE user_payment_methods (
    user_id INT NOT NULL,
    card_number VARCHAR(30) NOT NULL,

    PRIMARY KEY (user_id, card_number),

    FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE,

    FOREIGN KEY (card_number)
        REFERENCES payment_methods(card_number)
        ON DELETE CASCADE
);


CREATE TABLE products (
    product_id INT PRIMARY KEY AUTO_INCREMENT,
    product_name VARCHAR(200) NOT NULL UNIQUE,
    product_description VARCHAR(1000) DEFAULT NULL,
    unit_weight DECIMAL(15,3) NOT NULL,
    unit_price DECIMAL(15,2) NOT NULL,
    image_url VARCHAR(5000) DEFAULT NULL,
    quantity INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CHECK (unit_weight > 0),
    CHECK (unit_price > 0),
    CHECK (quantity >= 0)
);


CREATE TABLE shopping_carts (
    cart_id INT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT DEFAULT NULL,
    table_status VARCHAR(20) NOT NULL DEFAULT 'Active',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (customer_id)
        REFERENCES users(user_id)
        ON DELETE SET NULL,

    CHECK (
        table_status IN (
            'Active',
            'Ordered',
            'Cancelled',
            'Shipping',
            'Completed'
        )
    )
);


CREATE TABLE cart_items (
    cart_item_id INT PRIMARY KEY AUTO_INCREMENT,
    cart_id INT NOT NULL,
    product_id INT DEFAULT NULL,

    -- Product information preserved if the product is deleted.
    product_name VARCHAR(200) NOT NULL,
    unit_weight DECIMAL(15,3) NOT NULL,
    unit_price DECIMAL(15,2) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (cart_id)
        REFERENCES shopping_carts(cart_id)
        ON DELETE CASCADE,

    FOREIGN KEY (product_id)
        REFERENCES products(product_id)
        ON DELETE SET NULL,

    UNIQUE (cart_id, product_id),

    CHECK (unit_weight > 0),
    CHECK (unit_price > 0),
    CHECK (quantity > 0)
);


CREATE TABLE orders (
    order_id INT PRIMARY KEY AUTO_INCREMENT,

    -- Nullable so historical orders survive account deletion.
    customer_id INT DEFAULT NULL,

    -- UNIQUE enforces one order per shopping cart.
    cart_id INT NOT NULL UNIQUE,

    -- Nullable so an order survives payment-method deletion.
    card_number VARCHAR(30) DEFAULT NULL,

    order_status VARCHAR(20) NOT NULL DEFAULT 'Ordered',

    -- Checkout-time snapshots.
    delivery_address VARCHAR(500) NOT NULL,
    subtotal DECIMAL(15,2) NOT NULL,
    delivery_fee DECIMAL(15,2) NOT NULL,
    total_weight DECIMAL(15,3) NOT NULL,

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (customer_id)
        REFERENCES users(user_id)
        ON DELETE SET NULL,

    FOREIGN KEY (cart_id)
        REFERENCES shopping_carts(cart_id)
        ON DELETE RESTRICT,

    FOREIGN KEY (card_number)
        REFERENCES payment_methods(card_number)
        ON DELETE SET NULL,

    CHECK (
        order_status IN (
            'Active',
            'Ordered',
            'Cancelled',
            'Shipping',
            'Completed'
        )
    ),

    CHECK (subtotal > 0),
    CHECK (total_weight > 0),
    CHECK (delivery_fee IN (0.00, 10.00)),

    CHECK (
        (total_weight < 20.000 AND delivery_fee = 0.00)
        OR
        (total_weight >= 20.000 AND delivery_fee = 10.00)
    )
);


CREATE TABLE order_items (
    order_item_id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    product_id INT DEFAULT NULL,

    -- Checkout-time product snapshots.
    product_name VARCHAR(200) NOT NULL,
    unit_weight DECIMAL(15,3) NOT NULL,
    unit_price DECIMAL(15,2) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,

    FOREIGN KEY (order_id)
        REFERENCES orders(order_id)
        ON DELETE CASCADE,

    FOREIGN KEY (product_id)
        REFERENCES products(product_id)
        ON DELETE SET NULL,

    UNIQUE (order_id, product_id),

    CHECK (unit_weight > 0),
    CHECK (unit_price > 0),
    CHECK (quantity > 0)
);


CREATE TABLE delivery_tasks (
    task_id INT PRIMARY KEY AUTO_INCREMENT,
    delivery_task_status VARCHAR(20) NOT NULL DEFAULT 'Not Started',

    -- Store address preserved for this particular delivery run.
    pickup_address VARCHAR(500) NOT NULL,

    assigned_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    start_time DATETIME DEFAULT NULL,
    end_time DATETIME DEFAULT NULL,

    CHECK (
        delivery_task_status IN (
            'Not Started',
            'En Route',
            'Completed',
            'Failed'
        )
    ),

    CHECK (
        end_time IS NULL
        OR start_time IS NULL
        OR end_time >= start_time
    )
);


CREATE TABLE delivery_task_orders (
    task_id INT NOT NULL,
    order_id INT NOT NULL,
    estimated_arrival DATETIME DEFAULT NULL,

    PRIMARY KEY (task_id, order_id),

    -- An order can only belong to one delivery task.
    UNIQUE (order_id),

    FOREIGN KEY (task_id)
        REFERENCES delivery_tasks(task_id)
        ON DELETE CASCADE,

    FOREIGN KEY (order_id)
        REFERENCES orders(order_id)
        ON DELETE CASCADE
);


CREATE TABLE inventory_updates (
    update_id INT PRIMARY KEY AUTO_INCREMENT,

    -- Nullable so audit history survives product deletion.
    product_id INT DEFAULT NULL,
    product_name VARCHAR(200) NOT NULL,

    -- Nullable so audit history survives employee deletion.
    employee_id INT DEFAULT NULL,
    employee_name VARCHAR(150) NOT NULL,

    inventory_updates_action VARCHAR(20) NOT NULL,
    field_name VARCHAR(100) DEFAULT NULL,
    old_value TEXT DEFAULT NULL,
    new_value TEXT DEFAULT NULL,
    inventory_updates_description VARCHAR(1000) DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (product_id)
        REFERENCES products(product_id)
        ON DELETE SET NULL,

    FOREIGN KEY (employee_id)
        REFERENCES users(user_id)
        ON DELETE SET NULL,

    CHECK (
        inventory_updates_action IN (
            'Created',
            'Updated',
            'Deleted'
        )
    )
);
