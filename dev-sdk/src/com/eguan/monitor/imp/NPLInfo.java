package com.eguan.monitor.imp;

import java.io.Serializable;

/**
 * 网络类型变化、地理位置变化、SDK生命周期变化实体类，单例模式
 * 
 * @author Machenike
 *
 */
public class NPLInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	/**
	 * 网络类型，如“2142343-3G|2142343-4G“
	 */
	private String networkType;
	/**
	 * 地理位置，由时间戳、经度和纬度组成，用减号-连接，如：“2142343-123-456|2142389-127-456”
	 */
	private String geographyLocation;
	/**
	 * 进程生命周期，转换成时间戳，如：“1296035800-1296035890|1296035890-1296035900“
	 */
	private String processLifecycle;

	private static NPLInfo instance = null;

	private NPLInfo() {
	}

	public static synchronized NPLInfo getInstance() {
		if (instance == null) {
			instance = new NPLInfo();
		}
		return instance;
	}

	public String getNetworkType() {
		return networkType;
	}

	public void setNetworkType(String networkType) {
		this.networkType = networkType;
	}

	public String getGeographyLocation() {
		return geographyLocation;
	}

	public void setGeographyLocation(String geographyLocation) {
		this.geographyLocation = geographyLocation;
	}

	public String getProcessLifecycle() {
		return processLifecycle;
	}

	public void setProcessLifecycle(String processLifecycle) {
		this.processLifecycle = processLifecycle;
	}

	@Override
	public String toString() {
		return "NPLInfo [networkType=" + networkType + ", geographyLocation="
				+ geographyLocation + ", processLifecycle=" + processLifecycle
				+ "]";
	}

}
