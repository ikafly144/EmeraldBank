package net.sabafly.emeraldbank.database.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.sabafly.emeraldbank.EmeraldBank;
import net.sabafly.emeraldbank.database.Database;

import java.sql.Connection;
import java.sql.SQLException;

public class MySQL extends Base implements Database {

    private final HikariConfig config = new HikariConfig();
    private HikariDataSource dataSource;

    @Override
    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void setup() {
        config.setPoolName("EmeraldBank-Pool");

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(5);
        config.setMaxLifetime(1800000);
        config.setKeepaliveTime(60000);
        config.setConnectionTimeout(5000);

        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setJdbcUrl("jdbc:mysql://" + EmeraldBank.config().database.host + ":" + EmeraldBank.config().database.port + "/" + EmeraldBank.config().database.database + "?useSSL=false");
        config.addDataSourceProperty("user", EmeraldBank.config().database.username);
        config.addDataSourceProperty("password", EmeraldBank.config().database.password);

        dataSource = new HikariDataSource(config);

        super.setup();
    }

    @Override
    public void close() {
        try {
            dataSource.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}