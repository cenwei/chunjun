/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dtstack.flinkx.source;

import com.dtstack.flinkx.conf.FlinkxConf;
import com.dtstack.flinkx.constants.ConfigConstant;
import com.dtstack.flinkx.streaming.api.functions.source.DtInputFormatSourceFunction;
import org.apache.commons.collections.CollectionUtils;
import org.apache.flink.api.common.io.InputFormat;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.GenericTypeInfo;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;
import org.apache.flink.types.Row;
import org.apache.flink.util.Preconditions;

import java.util.List;

/**
 * Abstract specification of Reader Plugin
 *
 * Company: www.dtstack.com
 * @author huyifan.zju@163.com
 */
public abstract class BaseDataSource {

    protected StreamExecutionEnvironment env;
    protected FlinkxConf config;

    protected BaseDataSource(FlinkxConf config, StreamExecutionEnvironment env) {
        this.env = env;
        initColumn(config);
        this.config = config;
    }

    /**
     * Build the read data flow object
     *
     * @return DataStream
     */
    public abstract DataStream<Row> readData();

    @SuppressWarnings("unchecked")
    protected DataStream<Row> createInput(InputFormat inputFormat, String sourceName) {
        Preconditions.checkNotNull(sourceName);
        Preconditions.checkNotNull(inputFormat);
        TypeInformation typeInfo = TypeExtractor.getInputFormatTypes(inputFormat);
        DtInputFormatSourceFunction function = new DtInputFormatSourceFunction(inputFormat, typeInfo);
        return env.addSource(function, sourceName, typeInfo);
    }

    protected DataStream<Row> createInput(RichParallelSourceFunction<Row> function, String sourceName) {
        Preconditions.checkNotNull(sourceName);
        TypeInformation<Row> typeInfo = new GenericTypeInfo<>(Row.class);
        return env.addSource(function, sourceName, typeInfo);
    }

    protected DataStream<Row> createInput(InputFormat inputFormat) {
        return createInput(inputFormat, this.getClass().getSimpleName().toLowerCase());
    }

    /**
     *
     * getMetaColumns(columns, true); 默认对column里index为空时处理为对应数据在数组里的下标而不是-1
     * 如果index为-1是有特殊逻辑 需要覆盖此方法使用 getMetaColumns(List columns, false) 代替
     * @param config 配置信息
     */
    protected void initColumn(FlinkxConf config){
        List<MetaColumn> readerColumnList = MetaColumn.getMetaColumns(config.getReader().getMetaColumn());
        if(CollectionUtils.isNotEmpty(readerColumnList)){
            config.getReader().getParameter().put(ConfigConstant.KEY_COLUMN, readerColumnList);
        }
    }

    public FlinkxConf getConfig() {
        return config;
    }

    public void setConfig(FlinkxConf config) {
        this.config = config;
    }
}
