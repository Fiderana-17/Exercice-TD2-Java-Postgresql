create type mouvement_type as enum ('IN', 'OUT');

create table if not exists StockMovement(
    id serial primary key,
    id_ingredient int,
    quantity numeric(10,2) not null,
    type mouvement_type not null,
    unit unit_type not null,
    creation_datetime timestamp default current_timestamp,
    foreign key (id_ingredient) references ingredient(id)
)