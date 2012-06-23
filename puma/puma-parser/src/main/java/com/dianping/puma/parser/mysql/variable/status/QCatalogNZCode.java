/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dianping.puma.parser.mysql.variable.status;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.dianping.puma.common.util.PacketUtils;
import com.dianping.puma.parser.mysql.StatusVariable;

/**
 * 
 * TODO Comment of QCatalogNzCode
 * 
 * @author Leo Liang
 * 
 */
public class QCatalogNZCode implements StatusVariable {

	private String	catalogName;

	public QCatalogNZCode(String catalogName) {
		this.catalogName = catalogName;
	}

	public String getCatalogName() {
		return catalogName;
	}

	public static QCatalogNZCode valueOf(ByteBuffer buf) throws IOException {
		int length = PacketUtils.readInt(buf, 1);
		return new QCatalogNZCode(PacketUtils.readFixedLengthString(buf, length));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "QCatalogNZCode [catalogName=" + catalogName + "]";
	}

}
