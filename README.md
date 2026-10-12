# PawMatch — CS157A Team 10

Local development setup for Eclipse, Apache Tomcat, and MySQL. Tomcat serves
the existing landing-page prototype alongside a server check page and a JDBC
connection check. The prototype is not connected to the database.

## Requirements

- JDK 25 (the project targets Java 25).
- Eclipse IDE for Enterprise Java and Web Developers with Java 25 support.
- Apache Tomcat 11.
- MySQL Server and the `mysql` command-line client.
- MySQL Connector/J 26.7.0, available from the
  [official downloads](https://dev.mysql.com/downloads/connector/j/).

On macOS, JDK 25 can be installed with `brew install openjdk@25`. Select its
`libexec/openjdk.jdk/Contents/Home` directory under the Homebrew installation
when configuring an IDE. The `.java-version` file records the project version;
it does not install Java or change your shell's `JAVA_HOME` automatically.

## Eclipse and Tomcat

1. Import this repository through **File > Import > General > Existing Projects
   into Workspace**. Keep the Eclipse workspace outside the repository.
2. In **Settings/Preferences > Java > Installed JREs**, add your JDK 25 directory.
   Under **Execution Environments**, associate **JavaSE-25** with that JDK.
3. In **Server > Runtime Environments**, add **Apache Tomcat v11.0**, select your
   extracted Tomcat installation, and select JDK 25 as its JRE. Keep the runtime
   name `Apache Tomcat v11.0` to match the project's runtime reference.
4. Download the platform-independent Connector/J archive and extract it. Copy
   `mysql-connector-j-26.7.0.jar` into `src/main/webapp/WEB-INF/lib/` (create the
   directory if needed). Refresh the project. Eclipse's **Web App Libraries**
   adds the driver to the Java build path and deploys it with the web application.
   The downloaded JAR is intentionally ignored by Git.
5. Open the **Servers** view, add a Tomcat v11.0 server if needed, and use
   **Add and Remove…** to add `CS157A-team10`. Start the server.
6. Open <http://localhost:8080/CS157A-team10/> for the landing page. The server
   check at <http://localhost:8080/CS157A-team10/index.jsp> should report Java 25.
   If port 8080 is occupied, change the server's HTTP port and use it in the URL.

The Java sources are in `src/main/java`; deployable web files are in
`src/main/webapp`. The landing page lives at `src/main/webapp/index.html`.

## Create and seed the local database

Use a local MySQL account with permission to create the `pawmatch` database.
From the repository root, run these commands in order:

```sh
mysql --host=127.0.0.1 --port=3306 --user=root --password -e "CREATE DATABASE pawmatch"
mysql --host=127.0.0.1 --port=3306 --user=root --password pawmatch < db/schema.sql
mysql --host=127.0.0.1 --port=3306 --user=root --password pawmatch < db/seed.sql
```

Use your local port and username if different. `--password` prompts for the
password; do not put it in a command or commit it. You can also open and execute
each SQL file, in that order, in MySQL Workbench against the same local server.

`db/schema.sql` creates the 14 tables in the selected `pawmatch` database. `db/seed.sql` is the supplied fictional dataset, with 10 rows per table.
The seed file contains INSERT statements with foreign-key checks enabled.

These are first-time setup scripts, not migrations or reset commands. Run them
against a new `pawmatch` database. Re-running them reports existing tables or
duplicate keys; they do not drop tables or overwrite existing rows. If a local
`pawmatch` database already exists, inspect it before running either script.

## Check the JDBC connection

In Eclipse, right-click `src/main/java/MysqlCon.java` and select
**Run As > Java Application**. Under the run configuration's **Environment** tab,
set `DB_PASSWORD` to your local database password. Optionally set `DB_USER`
(default `root`) and `DB_URL` (default `jdbc:mysql://127.0.0.1:3306/pawmatch`).
Keep credentials in your local run configuration, not a shared launch file.

From Cursor's terminal, with JDK 25 on `PATH`:

```sh
mkdir -p build/classes
javac --release 25 -d build/classes src/main/java/MysqlCon.java
java -cp 'build/classes:src/main/webapp/WEB-INF/lib/*' MysqlCon
```

The terminal command prompts for the password. If your server uses another
port, set the URL before running it, for example:

```sh
export DB_URL='jdbc:mysql://127.0.0.1:3307/pawmatch'
```

On Windows, use `;` instead of `:` between the Java classpath entries. A fresh
seed should print `Connected to pawmatch`, followed by 10 owners, 10 pets, and
10 bookings. This checks JDBC connectivity; the setup page itself does not
query the database.
