package com.github.mybridge.sharding.impl;

import java.util.List;
import java.util.Set;

/**
 * <h2>分片</h2>
 * @author xiebiao
 */
public class Shard {

    /**
     * 分片id
     */
    private long                id;
    /**
     * 分片名称
     */
    private String              shardName;
    /**
     * id%分片数量 = 分片id
     */
    public Set<String>          hashValue;
    /**
     * 对应ShardGroup中的id
     */
    private long                groupId;

    /**
     * 分片中所有表
     */
    private List<FragmentTable> tables;
    /**
     * 数据库节点
     */
    private List<Node>          nodes;

    public Shard() {}

    public Shard(int id, String name, long groupId) {
        this.id = id;
        this.shardName = name;
        this.groupId = groupId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getShardName() {
        return shardName;
    }

    public void setShardName(String name) {
        this.shardName = name;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public List<FragmentTable> getTables() {
        return tables;
    }

    public void setTables(List<FragmentTable> tables) {
        this.tables = tables;
    }

    public Set<String> getHashValue() {
        return hashValue;
    }

    public void setHashValue(Set<String> hash) {
        this.hashValue = hash;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public String toString() {
        return "{id=" + this.id + " ,sharName:" + this.shardName + "}";
    }
}
