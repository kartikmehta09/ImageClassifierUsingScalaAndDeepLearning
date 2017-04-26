To run Yelp Image Classification Play UI using sbt:

 * `git clone` this repository
 * Update the MySQL server url, username and password in `conf/application.conf`
 * Create a `playdb` database on your MySQL server and load public\dbdump\playdb.sql dump.

```mysql
    CREATE DATABASE playdb;
```

 * Launch the demo using `sbt run`
 * Open the Play web server at <http://localhost:9000>
 * You should be prompted to apply the evolution script. Apply the script.
 * You should now see the app running.
