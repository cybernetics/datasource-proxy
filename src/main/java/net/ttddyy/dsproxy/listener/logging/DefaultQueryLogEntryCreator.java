package net.ttddyy.dsproxy.listener.logging;

import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.StatementType;
import net.ttddyy.dsproxy.proxy.ParameterSetOperation;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;

/**
 * @author Tadaya Tsuyukubo
 * @since 1.4
 */
public class DefaultQueryLogEntryCreator extends AbstractQueryLogEntryCreator {

    @Override
    public String getLogEntry(ExecutionInfo execInfo, List<QueryInfo> queryInfoList, boolean writeDataSourceName) {
        final StringBuilder sb = new StringBuilder();

        if (writeDataSourceName) {
            writeDataSourceNameEntry(sb, execInfo, queryInfoList);
        }

        // Time
        writeTimeEntry(sb, execInfo, queryInfoList);

        // Success
        writeResultEntry(sb, execInfo, queryInfoList);

        // Type
        writeTypeEntry(sb, execInfo, queryInfoList);

        // Batch
        writeBatchEntry(sb, execInfo, queryInfoList);

        // QuerySize
        writeQuerySizeEntry(sb, execInfo, queryInfoList);

        // BatchSize
        writeBatchSizeEntry(sb, execInfo, queryInfoList);

        // Queries
        writeQueriesEntry(sb, execInfo, queryInfoList);

        // Params
        writeParamsEntry(sb, execInfo, queryInfoList);

        return sb.toString();
    }

    /**
     * Write datasource name when enabled.
     *
     * <p>default: Name: myDS,
     *
     * @param sb            StringBuilder to write
     * @param execInfo      execution info
     * @param queryInfoList query info list
     * @since 1.3.3
     */
    protected void writeDataSourceNameEntry(StringBuilder sb, ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
        String name = execInfo.getDataSourceName();
        sb.append("Name:");
        sb.append(name == null ? "" : name);
        sb.append(", ");
    }

    /**
     * Write elapsed time.
     *
     * <p>default: Time: 123,
     *
     * @param sb            StringBuilder to write
     * @param execInfo      execution info
     * @param queryInfoList query info list
     * @since 1.3.3
     */
    protected void writeTimeEntry(StringBuilder sb, ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
        sb.append("Time:");
        sb.append(execInfo.getElapsedTime());
        sb.append(", ");
    }

    /**
     * Write query result whether successful or not.
     *
     * <p>default: Success: True,
     *
     * @param sb            StringBuilder to write
     * @param execInfo      execution info
     * @param queryInfoList query info list
     * @since 1.3.3
     */
    protected void writeResultEntry(StringBuilder sb, ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
        sb.append("Success:");
        sb.append(execInfo.isSuccess() ? "True" : "False");
        sb.append(", ");
    }

    /**
     * Write statement type.
     *
     * <p>default: Type: Prepared,
     *
     * @param sb            StringBuilder to write
     * @param execInfo      execution info
     * @param queryInfoList query info list
     * @since 1.3.3
     */
    protected void writeTypeEntry(StringBuilder sb, ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
        sb.append("Type:");
        sb.append(getStatementType(execInfo.getStatementType()));
        sb.append(", ");
    }

    /**
     * Write whether batch execution or not.
     *
     * <p>default: Batch: True,
     *
     * @param sb            StringBuilder to write
     * @param execInfo      execution info
     * @param queryInfoList query info list
     * @since 1.3.3
     */
    protected void writeBatchEntry(StringBuilder sb, ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
        sb.append("Batch:");
        sb.append(execInfo.isBatch() ? "True" : "False");
        sb.append(", ");
    }

    /**
     * Write query size.
     *
     * <p>default: QuerySize: 1,
     *
     * @param sb            StringBuilder to write
     * @param execInfo      execution info
     * @param queryInfoList query info list
     * @since 1.3.3
     */
    protected void writeQuerySizeEntry(StringBuilder sb, ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
        sb.append("QuerySize:");
        sb.append(queryInfoList.size());
        sb.append(", ");
    }

    /**
     * Write batch size.
     *
     * <p>default: BatchSize: 1,
     *
     * @param sb            StringBuilder to write
     * @param execInfo      execution info
     * @param queryInfoList query info list
     * @since 1.3.3
     */
    protected void writeBatchSizeEntry(StringBuilder sb, ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
        sb.append("BatchSize:");
        sb.append(execInfo.getBatchSize());
        sb.append(", ");
    }

    /**
     * Write queries.
     *
     * <p>default: Query:["select 1", "select 2"],
     *
     * @param sb            StringBuilder to write
     * @param execInfo      execution info
     * @param queryInfoList query info list
     * @since 1.3.3
     */
    protected void writeQueriesEntry(StringBuilder sb, ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
        sb.append("Query:[");
        for (QueryInfo queryInfo : queryInfoList) {
            sb.append("\"");
            sb.append(queryInfo.getQuery());
            sb.append("\",");
        }
        chompIfEndWith(sb, ',');
        sb.append("], ");
    }

    /**
     * Write query parameters.
     *
     * <p>default for prepared: Params:[(foo,100),(bar,101)],
     * <p>default for callable: Params:[(1=foo,key=100),(1=bar,key=101)],
     *
     * @param sb            StringBuilder to write
     * @param execInfo      execution info
     * @param queryInfoList query info list
     * @since 1.3.3
     */
    protected void writeParamsEntry(StringBuilder sb, ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {

        boolean isPrepared = execInfo.getStatementType() == StatementType.PREPARED;

        sb.append("Params:[");

        for (QueryInfo queryInfo : queryInfoList) {
            for (List<ParameterSetOperation> parameters : queryInfo.getParametersList()) {
                SortedMap<String, String> paramMap = getParametersToDisplay(parameters);

                // parameters per batch.
                //   for prepared: (val1,val2,...)
                //   for callable: (key1=val1,key2-val2,...)
                if (isPrepared) {
                    writeParamsEntryForSinglePreparedEntry(sb, paramMap, execInfo, queryInfoList);
                } else {
                    writeParamsForSingleCallableEntry(sb, paramMap, execInfo, queryInfoList);
                }


            }
        }

        chompIfEndWith(sb, ',');
        sb.append("]");
    }

    /**
     * Write query parameters for PreparedStatement.
     *
     * <p>default: Params:[(foo,100),(bar,101)],
     *
     * @param sb            StringBuilder to write
     * @param paramMap      sorted parameters map
     * @param execInfo      execution info
     * @param queryInfoList query info list
     * @since 1.4
     */
    protected void writeParamsEntryForSinglePreparedEntry(StringBuilder sb, SortedMap<String, String> paramMap, ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
        sb.append("(");
        for (Map.Entry<String, String> paramEntry : paramMap.entrySet()) {
            sb.append(paramEntry.getValue());
            sb.append(",");
        }
        chompIfEndWith(sb, ',');
        sb.append("),");
    }

    /**
     * Write parameters for single execution.
     *
     * <p>default: (1=foo,2=100),
     *
     * @param sb            StringBuilder to write
     * @param paramMap      sorted parameters map
     * @param execInfo      execution info
     * @param queryInfoList query info list
     * @since 1.4
     */
    protected void writeParamsForSingleCallableEntry(StringBuilder sb, SortedMap<String, String> paramMap, ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
        sb.append("(");
        for (Map.Entry<String, String> paramEntry : paramMap.entrySet()) {
            sb.append(paramEntry.getKey());
            sb.append("=");
            sb.append(paramEntry.getValue());
            sb.append(",");
        }
        chompIfEndWith(sb, ',');
        sb.append("),");
    }


}
