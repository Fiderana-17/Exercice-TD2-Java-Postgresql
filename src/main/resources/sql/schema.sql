CREATE TYPE category_enum AS ENUM ('VEGETABLE', 'ANIMAL', 'MARINE', 'DAIRY', 'OTHER');
CREATE TYPE dish_type_enum AS ENUM ('START', 'MAIN', 'DESSERT');

Create table Dish(
    id serial primary key ,
    name varchar(255) not null,
    dish_type dish_type_enum not null
);

CREATE TABLE Ingredient (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price NUMERIC NOT NULL,
    category category_enum NOT NULL,
    required_quantity NUMERIC,
    id_dish INT,
    CONSTRAINT fk_dish
    FOREIGN KEY (id_dish)
    REFERENCES Dish(id)
    ON DELETE SET NULL
);

