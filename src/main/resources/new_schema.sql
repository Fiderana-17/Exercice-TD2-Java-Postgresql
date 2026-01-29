create type unit_type as enum ('PCS' , 'KG', 'L');
create table if not exists DishIngredient (
    id serial primary key,
    id_dish int,
    id_ingredient int,
    quantity_required numeric,
    unit unit_type,
    constraint dish_fk foreign key (id_dish) references dish(id),
    constraint ingredient_fk foreign key (id_ingredient) references ingredient(id)
);

-- alter table dish rename column price to selling_price;

alter table public.ingredient drop column if exists id_dish;

