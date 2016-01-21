package com.github.mybridge;


public interface Launcher {
    /**
     * 启动
     */
    void start();

    /**
     * 初始化配置
     *
     * @throws Exception
     */
    void init();

    void stop();
}
