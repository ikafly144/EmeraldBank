package net.sabafly.emeraldbank.database.impl;

import net.sabafly.emeraldbank.EmeraldBank;
import org.h2.jdbc.JdbcConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class H2 extends Base {
    private H2Connection connection;

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public void setup() {
        try {
            //noinspection ResultOfMethodCallIgnored
            EmeraldBank.getInstance().getDataFolder().mkdirs();
            connection = new H2Connection("jdbc:h2:./" + EmeraldBank.getInstance().getDataFolder() + "/data;mode=MySQL", new Properties(), "", null, false);
            connection.setAutoCommit(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        super.setup();
    }

    @Override
    public void close() {
        try {
            connection.realClose();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static class H2Connection extends JdbcConnection {

        public H2Connection(String s, Properties properties, String s1, Object o, boolean b) throws SQLException {
            super(s, properties, s1, o, b);
        }

        @Override
        public synchronized void close() {
        }

        public synchronized void realClose() throws SQLException {
            super.close();
        }
    }
}
