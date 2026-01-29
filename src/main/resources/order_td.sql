create table if not exists "Order"(
                                      id serial primary key,
                                      reference varchar(255) not null,
                                      creation_datetime timestamp default now()
);

create table if not exists DishOrder(
                                        id serial primary key,
                                        id_order int,
                                        id_dish int,
                                        quantity int,
                                        foreign key (id_order) references "Order"(id),
                                        foreign key (id_dish) references dish(id)
);

ALTER TABLE "Order"
    ADD COLUMN payment_status payment_status NOT NULL DEFAULT 'UNPAID';

ALTER TABLE "Order"
    ADD COLUMN id_sale INT UNIQUE;



