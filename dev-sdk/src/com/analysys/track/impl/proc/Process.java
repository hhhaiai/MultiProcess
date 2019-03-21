/*
 *
 * 具体参考https://github.com/wenmingvs/Process
 * 	Android进程的实体类
 *
 */
package com.analysys.track.impl.proc;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.IOException;

public class Process implements Parcelable {

	/** the process name */
	public final String name;

	/** the process id */
	public final String pid;

	public String getName() {
		return name;
	}

	public String getPid() {
		return pid;
	}

	/**
	 * Process constructor
	 *
	 * @param pid
	 *            the process id
	 * @throws IOException
	 *             if /proc/[pid] does not exist or we don't have read access.
	 */
	public Process(String pid ,String pname) throws IOException {
		this.pid = pid;
		this.name = pname;
	}


	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.name);
		dest.writeInt(Integer.parseInt(this.pid));
	}

	protected Process(Parcel in) {
		this.name = in.readString();
		this.pid = String.valueOf(in.readInt());
	}

	public static final Creator<Process> CREATOR = new Creator<Process>() {

		@Override
		public Process createFromParcel(Parcel source) {
			return new Process(source);
		}

		@Override
		public Process[] newArray(int size) {
			return new Process[size];
		}
	};

}
