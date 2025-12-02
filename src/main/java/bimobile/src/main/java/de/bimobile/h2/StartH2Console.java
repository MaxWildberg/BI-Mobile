package de.bimobile.h2;

import org.h2.tools.Server;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class StartH2Console {
    public static void main(String[] args) {

        try {
            Connection conn = DriverManager.getConnection("jdbc:h2:~/bi_mobile_db", "sa", "");
            Statement stmt = conn.createStatement();
            stmt.execute("");
            stmt.close();
            conn.close();

            // Web Console Starten (Port 8082)
            Server webServer = Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082").start();
            // optionale TCP-Server (wenn andere Prozesse per Java Database Call darauf zugreifen sollen)
            Server tcpServer= Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "9092").start();

            System.out.println("H2 Web Console started at http://localhost:8082");
            System.out.println("JDBC URL (file): jdbc:h2:~/bi_mobile_db");
            System.out.println("JDBC URL (mem) : jdbc:h2:mem:bi_mobile_db");
            System.out.println("Press Ctrl+C to stop servers.");

        } catch (Exception e) {
            e.printStackTrace();
        }



    }
}
