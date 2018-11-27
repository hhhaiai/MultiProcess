/*
 * Copyright (C) 2015. Jared Rummler <jared.rummler@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.analysys.dev.internal.impl.proc;

import android.os.Build;
import android.os.Parcel;
import java.io.File;
import java.io.IOException;

public class AppProcess extends Process {

    private static final boolean SYS_SUPPORTS_SCHEDGROUPS = new File("/dev/cpuctl/tasks").exists();

    /**
     * {@code true} if the process is in the foreground
     */
    public final boolean foreground;

    /**
     * The user id of this process.
     */
    public final int uid;

    public AppProcess(int pid) throws IOException, NotAndroidAppProcessException {
        super(pid);
        final boolean foreground;
        int uid;

        if (SYS_SUPPORTS_SCHEDGROUPS) {
            Cgroup cgroup = cgroup();
            ControlGroup cpuacct = cgroup.getGroup("cpuacct");
            ControlGroup cpu = cgroup.getGroup("cpu");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (cpu == null || cpuacct == null || !cpuacct.group.contains("pid_")) {
                    throw new NotAndroidAppProcessException(pid);
                }
                foreground = !cpu.group.contains("bg_non_interactive");
                try {
                    uid = Integer.parseInt(cpuacct.group.split("/")[1].replace("uid_", ""));
                } catch (Exception e) {
                    uid = status().getUid();
                }
            } else {
                if (cpu == null || cpuacct == null || !cpu.group.contains("apps")) {
                    throw new NotAndroidAppProcessException(pid);
                }
                foreground = !cpu.group.contains("bg_non_interactive");
                try {
                    uid = Integer.parseInt(cpuacct.group.substring(cpuacct.group.lastIndexOf("/") + 1));
                } catch (Exception e) {
                    uid = status().getUid();
                }
            }
        } else {
            if (name.startsWith("/") || !new File("/data/data", getPackageName()).exists()) {
                throw new NotAndroidAppProcessException(pid);
            }
            Stat stat = stat();
            Status status = status();
            foreground = stat.policy() == 0;
            uid = status.getUid();
        }

        this.foreground = foreground;
        this.uid = uid;
    }

    /**
     * @return the app's package name
     * @see #name
     */
    public String getPackageName() {
        return name.split(":")[0];
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeByte((byte) (foreground ? 0x01 : 0x00));
        dest.writeInt(uid);
    }

    protected AppProcess(Parcel in) {
        super(in);
        foreground = in.readByte() != 0x00;
        uid = in.readInt();
    }

    public static final Creator<AppProcess> CREATOR = new Creator<AppProcess>() {

        @Override
        public AppProcess createFromParcel(Parcel source) {
            return new AppProcess(source);
        }

        @Override
        public AppProcess[] newArray(int size) {
            return new AppProcess[size];
        }
    };

    @SuppressWarnings("serial")
	public static final class NotAndroidAppProcessException extends Exception {

        public NotAndroidAppProcessException(int pid) {
            super(String.format("The process %d does not belong to any application", pid));
        }
    }

}
