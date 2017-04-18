# User schema

# --- !Ups
create table `business` (
  `biz_id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `biz_name` TEXT NOT NULL,
  `good_for_lunch` TEXT NOT NULL,
  `good_for_dinner` TEXT NOT NULL,
  `take_reservations` TEXT NOT NULL,
  `outdoor_sitting` TEXT NOT NULL,
  `restaurant_is_expensive` TEXT NOT NULL
)

# --- !Downs
drop table `business`