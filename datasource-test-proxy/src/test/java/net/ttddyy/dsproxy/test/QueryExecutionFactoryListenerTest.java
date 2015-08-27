package net.ttddyy.dsproxy.test;

import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.StatementType;
import net.ttddyy.dsproxy.proxy.ParameterSetOperation;
import org.junit.Test;

import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;


/**
 * @author Tadaya Tsuyukubo
 * @since 1.4
 */
public class QueryExecutionFactoryListenerTest {

    @Test
    public void statement() {
        QueryExecutionFactoryListener listener = new QueryExecutionFactoryListener();

        ExecutionInfo executionInfo = new ExecutionInfo();
        executionInfo.setBatch(false);
        executionInfo.setSuccess(true);
        executionInfo.setStatementType(StatementType.STATEMENT);

        // for Statement, only one QueryInfo
        QueryInfo queryInfo = new QueryInfo();
        queryInfo.setQuery("SELECT id FROM foo");

        List<QueryInfo> queryInfoList = new ArrayList<QueryInfo>();
        queryInfoList.add(queryInfo);

        listener.afterQuery(executionInfo, queryInfoList);

        List<QueryExecution> queryExecutions = listener.getQueryExecutions();
        assertThat(queryExecutions).hasSize(1);
        assertThat(queryExecutions.get(0)).isInstanceOf(StatementExecution.class);
        assertThat(((StatementExecution) queryExecutions.get(0)).getQuery()).isEqualTo("SELECT id FROM foo");
    }

    @Test
    public void batchStatement() {
        QueryExecutionFactoryListener listener = new QueryExecutionFactoryListener();

        ExecutionInfo executionInfo = new ExecutionInfo();
        executionInfo.setBatch(true);
        executionInfo.setSuccess(true);
        executionInfo.setStatementType(StatementType.STATEMENT);

        QueryInfo queryInfo1 = new QueryInfo();
        queryInfo1.setQuery("SELECT 1");
        QueryInfo queryInfo2 = new QueryInfo();
        queryInfo2.setQuery("SELECT 2");

        List<QueryInfo> queryInfoList = new ArrayList<QueryInfo>();
        queryInfoList.add(queryInfo1);
        queryInfoList.add(queryInfo2);

        listener.afterQuery(executionInfo, queryInfoList);

        List<QueryExecution> queryExecutions = listener.getQueryExecutions();
        assertThat(queryExecutions).hasSize(1);
        assertThat(queryExecutions.get(0)).isInstanceOf(StatementBatchExecution.class);
        assertThat(((StatementBatchExecution) queryExecutions.get(0)).getQueries())
                .hasSize(2)
                .containsSequence("SELECT 1", "SELECT 2");
    }

    @Test
    public void prepared() {
        QueryExecutionFactoryListener listener = new QueryExecutionFactoryListener();

        ExecutionInfo executionInfo = new ExecutionInfo();
        executionInfo.setBatch(false);
        executionInfo.setSuccess(true);
        executionInfo.setStatementType(StatementType.PREPARED);

        ParameterSetOperation param1 = new ParameterSetOperation(null, new Object[]{1, 100});
        ParameterSetOperation param2 = new ParameterSetOperation(null, new Object[]{2, 200});
        List<ParameterSetOperation> params = new ArrayList<ParameterSetOperation>();
        params.addAll(Arrays.asList(param1, param2));


        QueryInfo queryInfo = new QueryInfo();
        queryInfo.setQuery("SELECT id FROM foo");
        queryInfo.getParametersList().add(params);

        List<QueryInfo> queryInfoList = new ArrayList<QueryInfo>();
        queryInfoList.add(queryInfo);

        listener.afterQuery(executionInfo, queryInfoList);

        List<QueryExecution> queryExecutions = listener.getQueryExecutions();
        assertThat(queryExecutions).hasSize(1);
        assertThat(queryExecutions.get(0)).isInstanceOf(PreparedExecution.class);
        PreparedExecution pe = (PreparedExecution) queryExecutions.get(0);
        assertThat(pe.getQuery()).isEqualTo("SELECT id FROM foo");
        assertThat(pe.getParamIndexes()).hasSize(2).contains(1, 2);
        assertThat(pe.getParamsByIndex()).hasSize(2).containsEntry(1, 100).containsEntry(2, 200);
    }

    @Test
    public void batchPrepared() {
        QueryExecutionFactoryListener listener = new QueryExecutionFactoryListener();

        ExecutionInfo executionInfo = new ExecutionInfo();
        executionInfo.setBatch(true);
        executionInfo.setSuccess(true);
        executionInfo.setStatementType(StatementType.PREPARED);

        ParameterSetOperation param1 = new ParameterSetOperation(null, new Object[]{1, 100});
        ParameterSetOperation param2 = new ParameterSetOperation(null, new Object[]{2, 200});
        List<ParameterSetOperation> params1 = new ArrayList<ParameterSetOperation>();
        params1.addAll(Arrays.asList(param1, param2));

        ParameterSetOperation param3 = new ParameterSetOperation(null, new Object[]{10, 1000});
        ParameterSetOperation param4 = new ParameterSetOperation(null, new Object[]{20, 2000});
        List<ParameterSetOperation> params2 = new ArrayList<ParameterSetOperation>();
        params2.addAll(Arrays.asList(param3, param4));


        QueryInfo queryInfo = new QueryInfo();
        queryInfo.setQuery("SELECT id FROM foo");
        queryInfo.getParametersList().add(params1);
        queryInfo.getParametersList().add(params2);

        List<QueryInfo> queryInfoList = new ArrayList<QueryInfo>();
        queryInfoList.add(queryInfo);

        listener.afterQuery(executionInfo, queryInfoList);

        List<QueryExecution> queryExecutions = listener.getQueryExecutions();
        assertThat(queryExecutions).hasSize(1);
        assertThat(queryExecutions.get(0)).isInstanceOf(PreparedBatchExecution.class);
        PreparedBatchExecution pbe = (PreparedBatchExecution) queryExecutions.get(0);
        assertThat(pbe.getQuery()).isEqualTo("SELECT id FROM foo");
        assertThat(pbe.getBatchExecutionEntries()).hasSize(2);
        assertThat(pbe.getBatchExecutionEntries().get(0)).isInstanceOf(PreparedBatchExecution.PreparedBatchExecutionEntry.class);
        PreparedBatchExecution.PreparedBatchExecutionEntry batchEntry1 = (PreparedBatchExecution.PreparedBatchExecutionEntry) pbe.getBatchExecutionEntries().get(0);
        assertThat(batchEntry1.getParamIndexes()).hasSize(2).containsSequence(1, 2);
        assertThat(batchEntry1.getParamsByIndex()).hasSize(2).containsEntry(1, 100).containsEntry(2, 200);
        assertThat(batchEntry1.getParamValues()).hasSize(2).containsSequence(100, 200);
        assertThat(pbe.getBatchExecutionEntries().get(1)).isInstanceOf(PreparedBatchExecution.PreparedBatchExecutionEntry.class);
        PreparedBatchExecution.PreparedBatchExecutionEntry batchEntry2 = (PreparedBatchExecution.PreparedBatchExecutionEntry) pbe.getBatchExecutionEntries().get(1);
        assertThat(batchEntry2.getParamIndexes()).hasSize(2).containsSequence(10, 20);
        assertThat(batchEntry2.getParamsByIndex()).hasSize(2).containsEntry(10, 1000).containsEntry(20, 2000);
        assertThat(batchEntry2.getParamValues()).hasSize(2).containsSequence(1000, 2000);
    }

    @Test
    public void callable() throws Exception {
        QueryExecutionFactoryListener listener = new QueryExecutionFactoryListener();

        ExecutionInfo executionInfo = new ExecutionInfo();
        executionInfo.setBatch(false);
        executionInfo.setSuccess(true);
        executionInfo.setStatementType(StatementType.CALLABLE);


        Method setIntParamMethod = CallableStatement.class.getMethod("setInt", String.class, int.class);
        Method registerOutParamMethod = CallableStatement.class.getMethod("registerOutParameter", int.class, int.class);

        ParameterSetOperation paramByIndex1 = new ParameterSetOperation(setIntParamMethod, new Object[]{1, 100});
        ParameterSetOperation paramByIndex2 = new ParameterSetOperation(setIntParamMethod, new Object[]{2, 200});
        ParameterSetOperation paramByName1 = new ParameterSetOperation(setIntParamMethod, new Object[]{"foo", 100});
        ParameterSetOperation paramByName2 = new ParameterSetOperation(setIntParamMethod, new Object[]{"bar", 200});
        ParameterSetOperation outParamByIndex1 = new ParameterSetOperation(registerOutParamMethod, new Object[]{10, 1000});
        ParameterSetOperation outParamByIndex2 = new ParameterSetOperation(registerOutParamMethod, new Object[]{20, 2000});
        ParameterSetOperation outParamByName1 = new ParameterSetOperation(registerOutParamMethod, new Object[]{"foo-out", 1000});
        ParameterSetOperation outParamByName2 = new ParameterSetOperation(registerOutParamMethod, new Object[]{"bar-out", 2000});
        List<ParameterSetOperation> params = new ArrayList<ParameterSetOperation>();
        params.addAll(Arrays.asList(paramByIndex1, paramByIndex2, paramByName1, paramByName2, outParamByIndex1, outParamByIndex2, outParamByName1, outParamByName2));


        QueryInfo queryInfo = new QueryInfo();
        queryInfo.setQuery("SELECT id FROM foo");
        queryInfo.getParametersList().add(params);

        List<QueryInfo> queryInfoList = new ArrayList<QueryInfo>();
        queryInfoList.add(queryInfo);

        listener.afterQuery(executionInfo, queryInfoList);

        List<QueryExecution> queryExecutions = listener.getQueryExecutions();
        assertThat(queryExecutions).hasSize(1);
        assertThat(queryExecutions.get(0)).isInstanceOf(CallableExecution.class);
        CallableExecution ce = (CallableExecution) queryExecutions.get(0);
        assertThat(ce.getQuery()).isEqualTo("SELECT id FROM foo");
        assertThat(ce.getParamIndexes()).hasSize(2).contains(1, 2);
        assertThat(ce.getParamsByIndex()).hasSize(2).containsEntry(1, 100).containsEntry(2, 200);
        assertThat(ce.getParamNames()).hasSize(2).contains("foo", "bar");
        assertThat(ce.getParamsByName()).hasSize(2).containsEntry("foo", 100).containsEntry("bar", 200);
        assertThat(ce.getOutParamIndexes()).hasSize(2).contains(10, 20);
        assertThat(ce.getOutParamsByIndex()).hasSize(2).containsEntry(10, 1000).containsEntry(20, 2000);
        assertThat(ce.getOutParamNames()).hasSize(2).contains("foo-out", "bar-out");
        assertThat(ce.getOutParamsByName()).hasSize(2).containsEntry("foo-out", 1000).containsEntry("bar-out", 2000);
    }

    @Test
    public void batchCallable() throws Exception {
        QueryExecutionFactoryListener listener = new QueryExecutionFactoryListener();

        ExecutionInfo executionInfo = new ExecutionInfo();
        executionInfo.setBatch(true);
        executionInfo.setSuccess(true);
        executionInfo.setStatementType(StatementType.CALLABLE);


        Method setIntParamMethod = CallableStatement.class.getMethod("setInt", String.class, int.class);
        Method registerOutParamMethod = CallableStatement.class.getMethod("registerOutParameter", int.class, int.class);

        ParameterSetOperation paramByIndex1 = new ParameterSetOperation(setIntParamMethod, new Object[]{1, 100});
        ParameterSetOperation paramByIndex2 = new ParameterSetOperation(setIntParamMethod, new Object[]{2, 200});
        ParameterSetOperation paramByName1 = new ParameterSetOperation(setIntParamMethod, new Object[]{"foo", 100});
        ParameterSetOperation paramByName2 = new ParameterSetOperation(setIntParamMethod, new Object[]{"bar", 200});
        ParameterSetOperation outParamByIndex1 = new ParameterSetOperation(registerOutParamMethod, new Object[]{10, 1000});
        ParameterSetOperation outParamByIndex2 = new ParameterSetOperation(registerOutParamMethod, new Object[]{20, 2000});
        ParameterSetOperation outParamByName1 = new ParameterSetOperation(registerOutParamMethod, new Object[]{"foo-out", 1000});
        ParameterSetOperation outParamByName2 = new ParameterSetOperation(registerOutParamMethod, new Object[]{"bar-out", 2000});
        List<ParameterSetOperation> params1 = new ArrayList<ParameterSetOperation>();
        List<ParameterSetOperation> params2 = new ArrayList<ParameterSetOperation>();
        params1.addAll(Arrays.asList(paramByIndex1, paramByName1, outParamByIndex1, outParamByName1));
        params2.addAll(Arrays.asList(paramByIndex2, paramByName2, outParamByIndex2, outParamByName2));


        QueryInfo queryInfo = new QueryInfo();
        queryInfo.setQuery("SELECT id FROM foo");
        queryInfo.getParametersList().add(params1);
        queryInfo.getParametersList().add(params2);


        List<QueryInfo> queryInfoList = new ArrayList<QueryInfo>();
        queryInfoList.add(queryInfo);

        listener.afterQuery(executionInfo, queryInfoList);

        List<QueryExecution> queryExecutions = listener.getQueryExecutions();
        assertThat(queryExecutions).hasSize(1);
        assertThat(queryExecutions.get(0)).isInstanceOf(CallableBatchExecution.class);
        CallableBatchExecution cbe = (CallableBatchExecution) queryExecutions.get(0);
        assertThat(cbe.getQuery()).isEqualTo("SELECT id FROM foo");

        assertThat(cbe.getBatchExecutionEntries()).hasSize(2);
        assertThat(cbe.getBatchExecutionEntries().get(0)).isInstanceOf(CallableBatchExecution.CallableBatchExecutionEntry.class);
        assertThat(cbe.getBatchExecutionEntries().get(1)).isInstanceOf(CallableBatchExecution.CallableBatchExecutionEntry.class);

        CallableBatchExecution.CallableBatchExecutionEntry batchEntry1 = (CallableBatchExecution.CallableBatchExecutionEntry) cbe.getBatchExecutionEntries().get(0);
        assertThat(batchEntry1.getParamIndexes()).hasSize(1).contains(1);
        assertThat(batchEntry1.getParamNames()).hasSize(1).contains("foo");
        assertThat(batchEntry1.getParamsByIndex()).hasSize(1).containsEntry(1, 100);
        assertThat(batchEntry1.getParamsByName()).hasSize(1).containsEntry("foo", 100);
        assertThat(batchEntry1.getOutParamIndexes()).hasSize(1).contains(10);
        assertThat(batchEntry1.getOutParamNames()).hasSize(1).contains("foo-out");
        assertThat(batchEntry1.getOutParamsByIndex()).hasSize(1).containsEntry(10, 1000);
        assertThat(batchEntry1.getOutParamsByName()).hasSize(1).containsEntry("foo-out", 1000);


        CallableBatchExecution.CallableBatchExecutionEntry batchEntry2 = (CallableBatchExecution.CallableBatchExecutionEntry) cbe.getBatchExecutionEntries().get(1);
        assertThat(batchEntry2.getParamIndexes()).hasSize(1).contains(2);
        assertThat(batchEntry2.getParamNames()).hasSize(1).contains("bar");
        assertThat(batchEntry2.getParamsByIndex()).hasSize(1).containsEntry(2, 200);
        assertThat(batchEntry2.getParamsByName()).hasSize(1).containsEntry("bar", 200);
        assertThat(batchEntry2.getOutParamIndexes()).hasSize(1).contains(20);
        assertThat(batchEntry2.getOutParamNames()).hasSize(1).contains("bar-out");
        assertThat(batchEntry2.getOutParamsByIndex()).hasSize(1).containsEntry(20, 2000);
        assertThat(batchEntry2.getOutParamsByName()).hasSize(1).containsEntry("bar-out", 2000);

    }

    @Test
    public void reset() {
        QueryExecutionFactoryListener listener = new QueryExecutionFactoryListener();

        QueryExecution qe = mock(QueryExecution.class);
        listener.getQueryExecutions().add(qe);

        listener.reset();

        assertThat(listener.getQueryExecutions()).isEmpty();
    }
}
