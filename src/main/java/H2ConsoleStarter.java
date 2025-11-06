import org.h2.tools.Server;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class H2ConsoleStarter {
    public static void main(String[] args) throws Exception {
        //->> "alter" Code      Start H2 web console on port 8082
        //                      Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082").start();
        //                      System.out.println("H2 console started at http://localhost:8082");

        try {
            Connection conn = DriverManager.getConnection("jdbc:h2:~/bi_mobile_db", "sa", "");
            Statement stmt = conn.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS TEST (ID INT PRIMARY KEY, NAME VARCHAR(255));");
            stmt.close();
            conn.close();

            // Web Console Starten (Port 8082)
            Server webServer = Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082").start();
            // optionale TCP-Server (wenn andere Prozesse per Java Database Call darauf zugreifen sollen)
            Server tcpServer= Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "9092").start();

            System.out.println("H2 Web Console started at http://localhost:8082/h2-console");
            System.out.println("JDBC URL (file): jdbc:h2:~/bi_mobile_db");
            System.out.println("JDBC URL (mem) : jdbc:h2:mem:bi_mobile_db");
            System.out.println("Press Ctrl+C to stop servers.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
