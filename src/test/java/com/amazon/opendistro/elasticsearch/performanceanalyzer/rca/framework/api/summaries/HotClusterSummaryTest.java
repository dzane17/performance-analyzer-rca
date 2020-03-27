package com.amazon.opendistro.elasticsearch.performanceanalyzer.rca.framework.api.summaries;

import com.amazon.opendistro.elasticsearch.performanceanalyzer.grpc.FlowUnitMessage;
import com.amazon.opendistro.elasticsearch.performanceanalyzer.grpc.ResourceType;
import com.amazon.opendistro.elasticsearch.performanceanalyzer.rca.framework.core.GenericSummary;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.protobuf.GeneratedMessageV3;
import java.util.List;
import org.jooq.Field;
import org.jooq.Record;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class HotClusterSummaryTest {
    private static final int NUM_OF_NODES = 9;
    private static final int NUM_UNHEALTHY = 1;
    private static HotClusterSummary uut;

    @Mock
    private Record testRecord;

    @BeforeClass
    public static void setup() {
        uut = new HotClusterSummary(NUM_OF_NODES, NUM_UNHEALTHY);
    }

    @Test
    public void testBuildSummaryMessage() {
        GeneratedMessageV3 msg = uut.buildSummaryMessage();
        Assert.assertNull(msg);
    }

    @Test
    public void testBuildSummaryMessageAndAddToFlowUnit() {
        // No assertions need to be made here, this function is a noop in the uut
        FlowUnitMessage.Builder msgBuilder = FlowUnitMessage.newBuilder();
        uut.buildSummaryMessageAndAddToFlowUnit(msgBuilder);
    }

    @Test
    public void testToString() {
        Assert.assertEquals(NUM_OF_NODES + " " + NUM_UNHEALTHY + " " + uut.getNestedSummaryList(), uut.toString());
    }

    @Test
    public void testGetTableName() {
        Assert.assertEquals(HotClusterSummary.HOT_CLUSTER_SUMMARY_TABLE, uut.getTableName());
    }

    @Test
    public void testGetSqlSchema() {
        List<Field<?>> schema = uut.getSqlSchema();
        Assert.assertEquals(2, schema.size());
        Assert.assertEquals(HotClusterSummary.ClusterSummaryField.NUM_OF_NODES_FIELD.getField(), schema.get(0));
        Assert.assertEquals(HotClusterSummary.ClusterSummaryField.NUM_OF_UNHEALTHY_NODES_FIELD.getField(), schema.get(1));
    }

    @Test
    public void testGetSqlValue() {
        List<Object> rows = uut.getSqlValue();
        Assert.assertEquals(2, rows.size());
        Assert.assertEquals(NUM_OF_NODES, rows.get(0));
        Assert.assertEquals(NUM_UNHEALTHY, rows.get(1));
    }

    @Test
    public void testToJson() {
        uut.addNestedSummaryList(new HotClusterSummary(NUM_OF_NODES, NUM_UNHEALTHY));
        uut.addNestedSummaryList(new HotResourceSummary(ResourceType.newBuilder().build(), 3.14, 2.71, 0));
        JsonElement elem = uut.toJson();
        Assert.assertTrue(elem.isJsonObject());
        JsonObject json = ((JsonObject) elem);
        Assert.assertEquals(NUM_OF_NODES, json.get(HotClusterSummary.SQL_SCHEMA_CONSTANTS.NUM_OF_NODES_COL_NAME).getAsInt());
        Assert.assertEquals(NUM_UNHEALTHY, json.get(HotClusterSummary.SQL_SCHEMA_CONSTANTS.NUM_OF_UNHEALTHY_NODES_COL_NAME).getAsInt());
        Assert.assertEquals(uut.nestedSummaryListToJson(), json.get(uut.getNestedSummaryList().get(0).getTableName()).getAsJsonArray());
    }

    @Test
    public void testBuildSummary() {
        Assert.assertNull(HotClusterSummary.buildSummary(null));
        MockitoAnnotations.initMocks(this);
        Mockito.when(testRecord.get(HotClusterSummary.ClusterSummaryField.NUM_OF_NODES_FIELD.getField(), Integer.class))
                .thenReturn(NUM_OF_NODES);
        Mockito.when(testRecord.get(
                HotClusterSummary.ClusterSummaryField.NUM_OF_UNHEALTHY_NODES_FIELD.getField(), Integer.class)).thenReturn(NUM_UNHEALTHY);
        GenericSummary summary = HotClusterSummary.buildSummary(testRecord);
        Assert.assertNotNull(summary);
        Assert.assertTrue(summary instanceof HotClusterSummary);
        Assert.assertEquals(NUM_OF_NODES, ((HotClusterSummary) summary).getNumOfNodes());
        Assert.assertEquals(NUM_UNHEALTHY, ((HotClusterSummary) summary).getNumOfUnhealthyNodes());
    }
}