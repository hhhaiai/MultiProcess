package com.mp.qs;

import java.io.Closeable;
import java.net.HttpURLConnection;

/**
 * @Copyright © 2020 analysys Inc. All rights reserved.
 * @Description: 关闭
 * @Version: 1.0
 * @Create: Jul 30, 2020 1:53:05 PM
 * @author: sanbo
 */
public class Closer {

	public static void close(Object... os) {
		if (os != null && os.length > 0) {
			for (Object o : os) {
				if (o != null) {
					try {
						if (o instanceof HttpURLConnection) {
							((HttpURLConnection) o).disconnect();
						} else if (o instanceof Closeable) {
							((Closeable) o).close();
						}
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

}
