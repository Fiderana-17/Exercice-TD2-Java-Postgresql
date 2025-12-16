CREATE TYPE category_enum AS ENUM ('VEGETABLE', 'ANIMAL', 'MARINE', 'DAIRY', 'OTHER');
CREATE TYPE dish_type_enum AS ENUM ('START', 'MAIN', 'DESSERT');

Create table Dish(
    id serial primary key ,
    name varchar(255) not null,
    dish_type dish_type_enum not null
);

Create table Ingredient(
    id serial primary key,
    name varchar(255) not null,
    price numeric not null,
    category category_enum not null,
    id_dish INT,
    CONSTRAINT fk_dish FOREIGN KEY (id_dish) REFERENCES Dish(id) ON DELETE SET NULL
)
