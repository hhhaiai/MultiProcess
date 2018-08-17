package com.eguan.monitor.imp;

import java.io.Serializable;

/**
 * 已安装应用列表实体类
 * 
 * @author Machenike
 *
 */
public class InstalledAppInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 应用包名，如：“com.qzone”
	 */
	private String applicationPackageName;
	/**
	 * 应用程序名，如：“QQ空间”
	 */
	private String applicationName;
	/**
	 * 应用版本|应用版本号，如“5.4.1|89”
	 */
	private String applicationVersionCode;
	/**
	 * 应用是否新装还是卸载，“0”表示卸载；“1”表示新装
	 */
	private String isNew;

	public String getApplicationPackageName() {
		return applicationPackageName;
	}

	public void setApplicationPackageName(String applicationPackageName) {
		this.applicationPackageName = applicationPackageName;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public String getApplicationVersionCode() {
		return applicationVersionCode;
	}

	public void setApplicationVersionCode(String applicationVersionCode) {
		this.applicationVersionCode = applicationVersionCode;
	}

	public String getIsNew() {
		return isNew;
	}

	public void setIsNew(String isNew) {
		this.isNew = isNew;
	}

	/**
	 * 重写equals方法，用于表两个对象是否是同一个对象，以包名是否相同为参照标准
	 * 
	 * @param obj
	 * @return
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		} else {
			if (this.getClass() == obj.getClass()) {
				InstalledAppInfo u = (InstalledAppInfo) obj;
				return this.getApplicationPackageName().equals(
						u.getApplicationPackageName());

			} else {
				return false;
			}
		}
	}

	@Override
	public String toString() {
		return "InstalledAppInfo [applicationPackageName="
				+ applicationPackageName + ", applicationName="
				+ applicationName + ", applicationVersionCode="
				+ applicationVersionCode + ", isNew=" + isNew + "]";
	}

}
