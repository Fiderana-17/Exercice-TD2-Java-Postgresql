CREATE TYPE category_enum AS ENUM ('VEGETABLE', 'ANIMAL', 'MARINE', 'DAIRY', 'OTHER');
CREATE TYPE dish_type_enum AS ENUM ('START', 'MAIN', 'DESSERT');

Create table dish(
    id serial not null,
    name varchar(255) not null,
    dish_type dish_type_enum not null
)

Create table Ingredient(
    id serial primary key,
    name varchar(255) not null,
    price numeric not null,
    category category_enum not null,
    id_dish Integer references dish(id)
)