package com.moor.im.event;

public enum ReconnectEvent {
	/**
	 * 没有必要进行重连
	 */
	NONE,
	/**
	 * 可以进行重连了
	 */
    READY,
    /**
     * 重连成功了
     */
    SUCCESS,
    /**
     * 正在重连中
     */
    RECONNECTING,
    /**
     * 没有网络连接
     */
    DISABLE
}
