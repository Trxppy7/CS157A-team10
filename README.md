# PawMatch

CS157A Team 10 project. Uses Java, JSP, Tomcat, and MySQL.
The landing page is in `src/main/webapp/index.html`.

## Setup

You need JDK 25, Tomcat 11, MySQL, and MySQL Connector/J 26.7.0.
For Eclipse, use the Enterprise Java and Web Developers version with Java 25 support.

Copy `mysql-connector-j-26.7.0.jar` into `src/main/webapp/WEB-INF/lib/`.
Create that folder if it is missing. The jar is not included in Git.

Password recovery also needs these jars in the same folder:

- [jakarta.mail-api-2.1.5.jar](https://repo.maven.apache.org/maven2/jakarta/mail/jakarta.mail-api/2.1.5/jakarta.mail-api-2.1.5.jar)
- [angus-mail-2.0.5.jar](https://repo.maven.apache.org/maven2/org/eclipse/angus/angus-mail/2.0.5/angus-mail-2.0.5.jar)
- [jakarta.activation-api-2.1.4.jar](https://repo.maven.apache.org/maven2/jakarta/activation/jakarta.activation-api/2.1.4/jakarta.activation-api-2.1.4.jar)
- [angus-activation-2.0.3.jar](https://repo.maven.apache.org/maven2/org/eclipse/angus/angus-activation/2.0.3/angus-activation-2.0.3.jar)

From the project folder:

```sh
cp .env.example .env
```

Edit `.env` with your MySQL username, password, and port. Use `KEY=value`
without quotes. If a value contains a backslash, write it as `\\`.
Keep this file on your computer; Git ignores it.

## Run in Eclipse

1. Import with **File > Import > General > Existing Projects into Workspace**.
2. Add JDK 25 under **Java > Installed JREs** and select it for **JavaSE-25**.
3. Add your Tomcat installation under **Server > Runtime Environments**.
   Name it `Apache Tomcat v11.0` and use JDK 25.
4. Refresh the project after copying the MySQL jar.
5. In the **Servers** tab, create a Tomcat 11 server and add `CS157A-team10`.
6. In the server's launch configuration, add the environment variable
   `PAWMATCH_CONFIG` with the full path to your `.env` file. Start the server.

Open <http://localhost:8080/CS157A-team10/>.
If Eclipse uses a different port, change the URL to match.
`index.jsp` is a small page for checking that Tomcat is using Java 25.

## Run from Cursor or a terminal

```sh
./run.sh
```

Open <http://localhost:8081/CS157A-team10/>. Ctrl+C stops it.
`./build.sh` builds the WAR without starting Tomcat.

The scripts use JDK 25 and the Homebrew Tomcat folder on macOS by default.
Set `JAVA_HOME` and `TOMCAT_HOME` if yours are somewhere else.
Set `PORT` if 8081 is taken. The scripts also work on Linux with these paths set.
They use `.local/tomcat` for this project's server files.

## Database

The app creates `pawmatch` and loads the sample data on its first run.
Your MySQL user needs permission to create the database and tables.
There are 14 tables in `db/schema.sql` and 10 sample rows per table in
`db/seed.sql`. The app also creates `Accounts` for logins. Later runs keep
the data already there. Password recovery adds `PasswordResetTokens`.
Sample owners and caregivers are not login accounts. Create an account with
a different email on the registration page.

If only some tables or sample data are present, or a required column is
missing or has the wrong type, startup stops and prints the problem in the
Tomcat log. Check the database before resetting it. MySQL table creation
cannot be rolled back.
Older local databases can keep their decimal money columns and date columns;
the app does not convert or delete those values.

To load the files yourself, create a new database and then select it:

```sh
mysql -h 127.0.0.1 -P 3306 -u root -p -e "CREATE DATABASE pawmatch"
mysql -h 127.0.0.1 -P 3306 -u root -p pawmatch < db/schema.sql
mysql -h 127.0.0.1 -P 3306 -u root -p pawmatch < db/seed.sql
```

Use your port and username. The SQL files use the database selected on the
command line. They are for an empty database; running them again can give
duplicate-table or duplicate-row errors.

## Reset the sample data

**This deletes everything in `pawmatch`.** Stop the app first and save
anything you need. Check the host and port, then run from the project folder:

```sh
cat db/reset.sql db/schema.sql db/seed.sql | mysql -h 127.0.0.1 -P 3306 -u root -p pawmatch
```

The reset drops each table in dependency order. Run it against an existing
`pawmatch` database with all its tables.
After the reset, restart the app to recreate the empty account tables.
Enter your local MySQL password when asked. Change the port or username if
needed. Fix any SQL errors before starting the app again.

## JDBC check

In Eclipse, run `src/main/java/MysqlCon.java` as a Java application.
Set `DB_PASSWORD` in its run configuration. You can also set `DB_USER`
and `DB_URL` if you are not using root at `127.0.0.1:3306`.
With the original sample data it should print 10 owners, 10 pets, and 10 bookings.

## Try the account pages

Create an owner account and a caregiver account with different emails.
Log in to each and check its account page, then log out. Owners can use
Edit Profile to change their name, email and phone. The other role's
pages should give an error.
Passwords are hashed; names, addresses, and phone numbers are ordinary text.

For a local admin account, run `./build.sh`, then:

```sh
java -cp build/classes pawmatch.Passwords
```

Enter a password, select `pawmatch` in MySQL Workbench, and copy the printed
hash into this SQL. Pick an unused account_id:

```sql
INSERT INTO Accounts (account_id, email, password_hash, role)
VALUES (100, 'admin@example.com', 'paste the hash here', 'admin');
```

Change the email if needed. There is no admin signup page. The app currently
covers account creation, login/logout, role access, owner profile editing,
and password recovery.
The landing-page care request is still just a demo.

## Password recovery

Set the SMTP fields in `.env` to your mail server's settings. Set `APP_BASE_URL`
to the address you use for the app: port 8081 for `run.sh`, usually 8080 in Eclipse.
Without SMTP settings, the app runs but cannot send recovery emails.
Reset links expire after 30 minutes and work once. A successful reset invalidates
the account's other links. Reset records are stored in MySQL.
