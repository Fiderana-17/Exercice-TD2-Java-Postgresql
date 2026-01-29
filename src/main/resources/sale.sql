CREATE TYPE payment_status AS ENUM ('PAID', 'UNPAID');

CREATE TABLE sale (
                      id SERIAL PRIMARY KEY,
                      creation_datetime TIMESTAMP NOT NULL,
                      id_order INT UNIQUE NOT NULL,
                      CONSTRAINT fk_sale_order
                          FOREIGN KEY (id_order)
                              REFERENCES "Order"(id)
);

SELECT * FROM pg_type WHERE typname = 'payment_status';
