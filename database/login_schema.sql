create database login_schema;
use login_schema;
CREATE TABLE `users` (
  `idusers` int NOT NULL AUTO_INCREMENT,
  `username` varchar(45) NOT NULL,
  `password` varchar(45) NOT NULL,
  PRIMARY KEY (`idusers`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

insert into `login_schema`.`users`
(
	`username`,
    `password`
) values 
(
	"junayed",
    "pass123"
);
select *from users; 

CREATE TABLE `items` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `item_name` VARCHAR(100) NOT NULL,
  `quantity` INT NOT NULL,
  `price` DECIMAL(10,2) NOT NULL,
  PRIMARY KEY (`id`)
);

-- Sample data (optional)
INSERT INTO `items` (`item_name`, `quantity`, `price`)
VALUES ('Pen', 100, 5.00),
       ('Notebook', 50, 15.50);
       
select *from items;     

CREATE TABLE IF NOT EXISTS `sales` (
  `sale_id` INT NOT NULL AUTO_INCREMENT,
  `item_id` INT NOT NULL,
  `quantity_sold` INT NOT NULL,
  `sale_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`item_id`) REFERENCES `items`(`id`) ON DELETE CASCADE,
  PRIMARY KEY (`sale_id`)
);