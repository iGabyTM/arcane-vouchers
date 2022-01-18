package me.gabytm.minecraft.arcanevouchers.comet.modules.implementations.updater.updaters;

import me.gabytm.minecraft.arcanevouchers.comet.utils.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UsagesUpdater {

    private static final Logger LOGGER = LoggerFactory.getLogger(UsagesUpdater.class);

    public UsagesUpdater() {
        final File limitsFile = new File("limits.yml");

        if (!Files.checkFile(limitsFile)) {
            return;
        }

        final File databaseFile = new File("usages.sql.db");

        if (!databaseFile.exists()) {
            try {
                databaseFile.createNewFile();
            } catch (IOException e) {
                LOGGER.error("Could not create " + databaseFile, e);
            }
        }

        Connection connection = null;

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile);
            connection.setAutoCommit(false);
        } catch (ClassNotFoundException e) {
            LOGGER.error("Could not find class org.sqlite.JDBC", e);
        } catch (SQLException e) {
            LOGGER.error("Could not establish database connection", e);
        }

        if (connection == null) {
            LOGGER.error("Connection is null");
            return;
        }

        try {
            final CommentedConfigurationNode root = YamlConfigurationLoader.builder()
                    .file(limitsFile)
                    .build()
                    .load();

            update(root, connection);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void update(final CommentedConfigurationNode root, final Connection connection) {
        try {
            Query.CREATE_GLOBAL_TABLE.prepare(connection).execute();
            Query.CREATE_PERSONAL_TABLE.prepare(connection).execute();
            connection.commit();
        } catch (SQLException e) {
            LOGGER.error("Could not create tables", e);
            return;
        }

        try {
            root.node("global").act(it -> {
                for (final CommentedConfigurationNode voucherNode : it.childrenMap().values()) {
                    final PreparedStatement statement = Query.INSERT_GLOBAL.prepare(connection);
                    statement.setString(1, voucherNode.key().toString());
                    statement.setInt(2, voucherNode.getInt());
                    statement.executeUpdate();
                }
            });

            connection.commit();
        } catch (SQLException e) {
            LOGGER.error("Could not update global usages", e);
        }

        try {
            root.node("personal").act(it -> {
                for (final CommentedConfigurationNode uuidNode : it.childrenMap().values()) {
                    final String uuid = uuidNode.key().toString();

                    for (final CommentedConfigurationNode voucherNode : uuidNode.childrenMap().values()) {
                        final PreparedStatement statement = Query.INSERT_PERSONAL.prepare(connection);
                        statement.setString(1, uuid);
                        statement.setString(2, voucherNode.key().toString());
                        statement.setInt(3, voucherNode.getInt());
                        statement.executeUpdate();
                    }
                }
            });

            connection.commit();
        } catch (SQLException e) {
            LOGGER.error("Could not update personal usages", e);
        }
    }

    private enum Query {

        CREATE_GLOBAL_TABLE(
                "CREATE TABLE IF NOT EXISTS `global_usages` (",
                "id INTEGER PRIMARY KEY,",
                "voucher VARCHAR(128),",
                "usages INTEGER",
                ");"
        ),

        CREATE_PERSONAL_TABLE(
                "CREATE TABLE IF NOT EXISTS `personal_usages` (",
                "id INTEGER PRIMARY KEY,",
                "uuid VARCHAR(36),",
                "voucher VARCHAR(128),",
                "usages INTEGER",
                ");"
        ),

        INSERT_GLOBAL(
                "INSERT INTO `global_usages` (voucher, usages) ",
                "VALUES (?, ?);"
        ),

        INSERT_PERSONAL(
                "INSERT INTO `personal_usages` (uuid, voucher, usages) ",
                "VALUES (?, ?, ?)"
        );

        private String query;

        Query(String... args) {
            this.query = String.join("", args);
        }

        private PreparedStatement prepare(final Connection connection) throws SQLException {
            return connection.prepareStatement(this.query);
        }

    }

}
