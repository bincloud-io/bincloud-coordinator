DELETE FROM `mysql`.`user` WHERE User NOT IN ('mariadb.sys');
CREATE USER 'bincloud'@'%' IDENTIFIED BY 'bincloud';
GRANT ALL PRIVILEGES ON *.* TO 'bincloud'@'%' WITH GRANT OPTION;
CREATE SCHEMA `bc_central` CHARACTER SET utf8  COLLATE utf8_general_ci;
DROP DATABASE `test`;
