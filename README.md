# CSYE7200_Scala_Project_Team7
Yelp Image Classification Model - using scala, deeplearning4j
Team7_UI - using Play and Scala


Build a predictive model which classify user-submitted photos and automatically tag restaurants with multiple labels 
* 0: good_for_lunch
* 1: good_for_dinner
* 2: takes_reservations
* 3: outdoor_seating
* 4: restaurant_is_expensive 

# Infrastructure used in this project:

* User Interface – PlayFramework, Scala Controllers, Lightbend Activator JavaScript, Bootstrap, CSS, HTML
* Database – In memory database (MySQL) and Slick for database querying
* Apache Spark – Scala Integration – Convolutional Neural Network Algorithm implemented in Scala using DeepLearning4J.
* Data Cleaning & Preparation – Scala, MS Excel

# Magnitude of Dataset:

* train_photos.tgz - photos of the training set  (234,545 images)
* test_photos.tgz - photos of the test set (500 images)
* train_photo_to_biz_ids.csv - maps the photo id to business id (234,545 rows)
* test_photo_to_biz_ids.csv - maps the photo id to business id ( 500 rows)
* train.csv - main training dataset. Includes the business id's, and their corresponding labels. (1996 rows) 








