-- Dish
INSERT INTO dish (id, name, dish_type) VALUES
(1, 'Salade fraîche', 'START'),
(2, 'Poulet grillé', 'MAIN'),
(3, 'Riz aux légumes', 'MAIN'),
(4, 'Gâteau au chocolat', 'DESSERT'),
(5, 'Salade de fruits', 'DESSERT');

-- Ingredient
INSERT INTO Ingredient (name, price, category, required_quantity, id_dish) VALUES
('Laitue',    800.00,  'VEGETABLE', 1,    1),
('Tomate',    600.00,  'VEGETABLE', 2,    1),
('Poulet',   4500.00,  'ANIMAL',    0.5,  2),
('Chocolat', 3000.00,  'OTHER',     NULL, 4),
('Beurre',   2500.00,  'DAIRY',     NULL, 4);
