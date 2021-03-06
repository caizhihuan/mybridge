package com.github.mybridge.core;

import java.util.List;

import com.github.mybridge.mysql.packet.Packet;

/**
 * (线程安全)
 * @author xiebiao
 */
public interface MySQLHandler {

    public List<Packet> execute(Packet packet) throws ExecuteException;

    public void setCharset(String charset);

    public void setDatabase(String database);

}
