/**
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
package com.dtstack.flinkx.hive.writer;

import com.dtstack.flinkx.config.DataTransferConfig;
import com.dtstack.flinkx.config.WriterConfig;
import com.dtstack.flinkx.hive.TableInfo;
import com.dtstack.flinkx.hive.util.HiveUtil;
import com.dtstack.flinkx.writer.DataWriter;
import com.google.gson.Gson;
import org.apache.commons.collections.MapUtils;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.functions.sink.DtOutputFormatSinkFunction;
import org.apache.flink.types.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import static com.dtstack.flinkx.hive.HdfsConfigKeys.*;

/**
 * The writer plugin of Hdfs
 *
 * Company: www.dtstack.com
 * @author huyifan.zju@163.com
 */
public class HiveWriter extends DataWriter {

    protected final Logger LOG = LoggerFactory.getLogger(getClass());

//    protected String defaultFS;

    protected String fileType;

    protected String partition;

//    protected String path;

    protected String fieldDelimiter;

    protected String compress;

    protected String fileName;

    protected Map<String, TableInfo> tableInfos;

//    protected List<String> columnName;

//    protected List<String> columnType;

    protected Map<String,String> hadoopConfig;

    protected String charSet;

//    protected List<String> fullColumnName;

//    protected List<String> fullColumnType;

    protected static final String DATA_SUBDIR = ".data";

    protected static final String FINISHED_SUBDIR = ".finished";

    protected static final String SP = "/";

//    protected int rowGroupSize;

    protected long maxFileSize;


    private String jdbcUrl;

    private String database;

    private String username;

    private String password;

    public HiveWriter(DataTransferConfig config) {
        super(config);
        WriterConfig writerConfig = config.getJob().getContent().get(0).getWriter();
        hadoopConfig = (Map<String, String>) writerConfig.getParameter().getVal(KEY_HADOOP_CONFIG_MAP);
        String tablesColumn = writerConfig.getParameter().getStringVal(KEY_TABLE_COLUMN);
        fileType = writerConfig.getParameter().getStringVal(KEY_STORE);
        partition = writerConfig.getParameter().getStringVal(KEY_PARTITION, "pt");
//        defaultFS = writerConfig.getParameter().getStringVal(KEY_DEFAULT_FS);
//        path = writerConfig.getParameter().getStringVal(KEY_PATH);
        fieldDelimiter = writerConfig.getParameter().getStringVal(KEY_FIELD_DELIMITER, "\u0001");
        charSet = writerConfig.getParameter().getStringVal(KEY_ENCODING);
//        rowGroupSize = writerConfig.getParameter().getIntVal(KEY_ROW_GROUP_SIZE, ParquetWriter.DEFAULT_BLOCK_SIZE);
        maxFileSize = writerConfig.getParameter().getLongVal(KEY_MAX_FILE_SIZE, 1024 * 1024 * 1024);

        compress = writerConfig.getParameter().getStringVal(KEY_COMPRESS);

        if(StringUtil.isNotEmpty(tablesColumn)) {
            Map<String, Object> tableColumnMap = new Gson().fromJson(tablesColumn, Map.class);
            for (Map.Entry<String, Object> entry : tableColumnMap.entrySet()) {
                String tableName = entry.getKey();
                List<Map<String, Object>> tableColumns = (List<Map<String, Object>>) entry.getValue();
                TableInfo tableInfo = new TableInfo(tableColumns.size());
                tableInfo.setDatabase(database);
                tableInfo.addPartition(partition);
                tableInfo.setDelimiter(fieldDelimiter);
                tableInfo.setStore(fileType);
                tableInfo.setTableName(tableName);
                for (Map<String, Object> column : tableColumns) {
                    tableInfo.addColumnAndType(MapUtils.getString(column, HiveUtil.TABLE_COLUMN_KEY),  HiveUtil.convertType(MapUtils.getString(column, HiveUtil.TABLE_COLUMN_TYPE)));
                }
                String createTableSql = HiveUtil.getCreateTableHql(tableInfo);
                tableInfo.setCreateTableSql(createTableSql);

                tableInfos.put(tableName, tableInfo);
            }
        }

//        fullColumnName = (List<String>) writerConfig.getParameter().getVal(KEY_FULL_COLUMN_NAME_LIST);
//        fullColumnType = (List<String>) writerConfig.getParameter().getVal(KEY_FULL_COLUMN_TYPE_LIST);

        mode = writerConfig.getParameter().getStringVal(KEY_WRITE_MODE);
    }

    @Override
    public DataStreamSink<?> writeData(DataStream<Row> dataSet) {
        HdfsOutputFormatBuilder builder = new HdfsOutputFormatBuilder(fileType);
        builder.setHadoopConfig(hadoopConfig);
//        builder.setDefaultFS(defaultFS);
//        builder.setPath(path);
        builder.setFileName(fileName);
        builder.setWriteMode(mode);
//        builder.setColumnNames(columnName);
//        builder.setColumnTypes(columnType);
        builder.setCompress(compress);
        builder.setMonitorUrls(monitorUrls);
        builder.setErrors(errors);
        builder.setErrorRatio(errorRatio);
//        builder.setFullColumnNames(fullColumnName);
//        builder.setFullColumnTypes(fullColumnType);
        builder.setDirtyPath(dirtyPath);
        builder.setDirtyHadoopConfig(dirtyHadoopConfig);
        builder.setSrcCols(srcCols);
        builder.setCharSetName(charSet);
        builder.setDelimiter(fieldDelimiter);
//        builder.setRowGroupSize(rowGroupSize);
        builder.setRestoreConfig(restoreConfig);
        builder.setMaxFileSize(maxFileSize);

        DtOutputFormatSinkFunction sinkFunction = new DtOutputFormatSinkFunction(builder.finish());
        DataStreamSink<?> dataStreamSink = dataSet.addSink(sinkFunction);

        dataStreamSink.name("hdfswriter");

        return dataStreamSink;
    }
}
