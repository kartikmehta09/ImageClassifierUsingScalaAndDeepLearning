# CSYE7200_Scala_Project_Team7
Yelp Image Classification Model - using scala, deeplearning4j
Team7_UI - using Play and Scala


Build a predictive model which classify user-submitted photos and automatically tag restaurants with multiple labels 
* 0: good_for_lunch
* 1: good_for_dinner
* 2: takes_reservations
* 3: outdoor_seating
* 4: restaurant_is_expensive 

Infrastructure used in this project:

User Interface – PlayFramework, Scala Controllers, Lightbend Activator JavaScript, Bootstrap, CSS, HTML
Database – In memory database (MySQL) and Slick for database querying
Apache Spark – Scala Integration – Convolutional Neural Network Algorithm implemented in Scala using DeepLearning4J.
Data Cleaning & Preparation – Scala, MS Excel

Magnitude of Data:

In order to make the features for our convolutional neural network, we have proccessed 4 different datasets of different magnitudes. Though we have restricted the magnitude of our dataset so that it can run efficiently on our system.

The dataset are :
train_photos (7000 images)
train_photo_to_biz_ids(7000 rows)

By this data we essentially made Map of businessid and the images, there was a seperate function to convert the pre proccessed images into  vector of RGB components and then map the busineessid with the image vectors and the list of business labels.

This data is further converted into ND4J dataset as our CNN model requires data in nd4j type. So basically our final data for the features in the nd4J dataset looks like this:

LIST(String(biz_id), Int(Image_id) , Vector(INT)(Image RGB components), LIST(INT) (Labels for this business))








