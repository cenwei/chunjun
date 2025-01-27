/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dtstack.flinkx.connector.oracle.table;

import com.dtstack.flinkx.connector.jdbc.conf.JdbcConf;
import com.dtstack.flinkx.connector.jdbc.dialect.JdbcDialect;
import com.dtstack.flinkx.connector.jdbc.source.JdbcInputFormatBuilder;
import com.dtstack.flinkx.connector.jdbc.table.JdbcDynamicTableFactory;
import com.dtstack.flinkx.connector.jdbc.util.JdbcUtil;
import com.dtstack.flinkx.connector.oracle.dialect.OracleDialect;
import com.dtstack.flinkx.connector.oracle.source.OracleInputFormat;

import java.util.Properties;

/**
 * company www.dtstack.com
 *
 * @author jier
 */
public class OracleDynamicTableFactory extends JdbcDynamicTableFactory {

    /** 通过该值查找具体插件 */
    private static final String IDENTIFIER = "oracle-x";

    @Override
    public String factoryIdentifier() {
        return IDENTIFIER;
    }

    @Override
    protected JdbcInputFormatBuilder getInputFormatBuilder() {
        return new JdbcInputFormatBuilder(new OracleInputFormat());
    }

    @Override
    protected JdbcDialect getDialect() {
        return new OracleDialect();
    }

    @Override
    protected void rebuildJdbcConf(JdbcConf jdbcConf) {
        super.rebuildJdbcConf(jdbcConf);

        Properties properties = new Properties();
        if (jdbcConf.getConnectTimeOut() != 0) {
            properties.put(
                    "oracle.jdbc.ReadTimeout", String.valueOf(jdbcConf.getConnectTimeOut() * 1000));
            properties.put(
                    "oracle.net.CONNECT_TIMEOUT",
                    String.valueOf((jdbcConf.getConnectTimeOut()) * 1000));
        }
        JdbcUtil.putExtParam(jdbcConf, properties);
    }
}
