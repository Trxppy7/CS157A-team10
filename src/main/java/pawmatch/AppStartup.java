package pawmatch;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.SessionTrackingMode;
import jakarta.servlet.annotation.WebListener;
import java.util.Set;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

@WebListener
public class AppStartup implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent event) {
        event.getServletContext().getSessionCookieConfig().setHttpOnly(true);
        event.getServletContext().setSessionTrackingModes(Set.of(SessionTrackingMode.COOKIE));
        try {
            event.getServletContext().log(Db.initialize());
        } catch (Exception error) {
            throw new IllegalStateException("PawMatch database startup failed: " + error.getMessage(), error);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            if (driver.getClass().getClassLoader() == getClass().getClassLoader()) {
                try {
                    DriverManager.deregisterDriver(driver);
                } catch (SQLException error) {
                    event.getServletContext().log("Could not release MySQL driver.", error);
                }
            }
        }
    }
}
