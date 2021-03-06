package com.github.mybridge.core;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;

import com.github.mybridge.engine.DefaultEngine;
import com.github.mybridge.engine.Engine;
import com.github.mybridge.mysql.packet.CommandsPacket;
import com.github.mybridge.mysql.packet.EofPacket;
import com.github.mybridge.mysql.packet.ErrPacket;
import com.github.mybridge.mysql.packet.FieldDescriptionPacket;
import com.github.mybridge.mysql.packet.OkPacket;
import com.github.mybridge.mysql.packet.Packet;
import com.github.mybridge.mysql.packet.ResultSetPacket;
import com.github.mybridge.mysql.packet.RowDataPacket;
import com.github.mybridge.sharding.ConnectionPool;
import com.github.mybridge.sharding.NodeExecuter;
import com.github.mybridge.sharding.support.SimpleConnectionPool;

public class DefaultMySQLHandler implements MySQLHandler {

    private static final org.slf4j.Logger logger  = org.slf4j.LoggerFactory.getLogger(DefaultMySQLHandler.class);
    private String                        charset = "utf-8";
    private String                        database;
    private Engine                        engine;
    private static final ConnectionPool   cp      = new SimpleConnectionPool();

    public DefaultMySQLHandler() {
        this.engine = new DefaultEngine();
    }

    public List<Packet> execute(Packet packet) throws ExecuteException {
        List<Packet> packetList = null;
        CommandsPacket cmdPacket = (CommandsPacket) packet;
        int cmdType = cmdPacket.getType();
        try {
            packetList = new ArrayList<Packet>();
            switch (cmdType) {
                case MySQLCommand.COM_QUERY:
                    String sql = new String(cmdPacket.getValue(), charset);
                    logger.debug("COM_QUERY: " + sql);
                    return executeSQL(sql);
                case MySQLCommand.COM_QUIT:
                    return null;
                case MySQLCommand.COM_FIELD_LIST:
                    packetList.add(new EofPacket());
                    return packetList;
                case MySQLCommand.COM_INIT_DB:
                    String db = new String(cmdPacket.getValue(), charset);
                    sql = "USE" + db;
                    logger.debug("COM_INIT_DB: " + db);
                    setDatabase(db);
                    return executeSQL(sql);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ExecuteException("Command excute error");
        }
        return packetList;
    }

    /**
     * 执行Mysql Commond sql:
     * <p>
     * 如：SHOW SESSION VARIABLES,Show Collection,sql
     * </p>
     * @param sql
     */
    private List<Packet> executeSQL(String sql) {
        List<Packet> packetList = new ArrayList<Packet>();
        try {
            packetList = execute(sql);
        } catch (Exception e) {
            packetList.add(new ErrPacket());
        }
        return packetList;
    }

    private List<Packet> execute(String sql) throws SQLException {
        List<Packet> packetList = new ArrayList<Packet>();
        //不能解析 SHOW *类的sql
        //NodeExecuter ne = this.engine.getNodeExecuter(sql);
        Connection connection = this.cp.getConnection();
        boolean result;
        Statement statement;
        try {
            statement = connection.createStatement();
            result = statement.execute(sql);
        } catch (SQLException e) {
            ErrPacket err = new ErrPacket(e.getErrorCode(), e.getSQLState(), e.getMessage());
            packetList.add(err);
            return packetList;
        }
        if (result == false) {
            OkPacket ok = new OkPacket();
            ok.setAffectedRows(statement.getUpdateCount());
            packetList.add(ok);
            return packetList;
        }
        ResultSet rs = statement.getResultSet();
        ResultSetMetaData meta = rs.getMetaData();
        ResultSetPacket resultPacket = new ResultSetPacket(meta.getColumnCount());
        packetList.add(resultPacket);
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            FieldDescriptionPacket fieldPacket = new FieldDescriptionPacket();
            fieldPacket.setDatabase(meta.getCatalogName(i));
            fieldPacket.setTable(meta.getTableName(i));
            fieldPacket.setOrgTable(meta.getTableName(i));
            fieldPacket.setName(meta.getColumnName(i));
            fieldPacket.setOrgName(meta.getColumnName(i));
            fieldPacket.setType((byte) MySQLCommand.javaTypeToMysql(meta.getColumnType(i)));
            fieldPacket.setLength(meta.getColumnDisplaySize(i));
            packetList.add(fieldPacket);
        }
        packetList.add(new EofPacket());
        while (rs.next()) {
            RowDataPacket rowPacket = new RowDataPacket(charset);
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                String value = rs.getString(i);
                rowPacket.addValue(value);
            }
            packetList.add(rowPacket);
        }
        packetList.add(new EofPacket());
        rs.close();
        statement.close();
        connection.close();
        DbUtils.closeQuietly(connection, statement, rs);
        return packetList;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public void setDatabase(String db) {
        this.database = db;
    }

}
