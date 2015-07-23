package net.ttddyy.dsproxy.test.hamcrest;

import net.ttddyy.dsproxy.test.PreparedExecution;
import org.hamcrest.Matcher;
import org.junit.Test;

import static net.ttddyy.dsproxy.test.hamcrest.PreparedExecutionMatcher.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author Tadaya Tsuyukubo
 * @since 1.4
 */
public class PreparedExecutionMatcherTest {

    @Test
    public void testQuery() {
        PreparedExecution pe = new PreparedExecution();
        pe.setQuery("foo");

        assertThat(pe, query(is("foo")));
        assertThat(pe, query(startsWith("fo")));
        assertThat(pe.getQuery(), is("foo"));
    }

    @Test
    public void testParams() {
        PreparedExecution pe = new PreparedExecution();
        pe.getParams().put("foo", "FOO");
        pe.getParams().put("bar", "BAR");
        pe.getParams().put("baz", "BAZ");
        pe.getParams().put("number", 100);
        assertThat(pe, params(hasEntry("bar", (Object) "BAR")));
        assertThat(pe, params(hasEntry("number", (Object) 100)));
    }

    @Test
    public void testParamKeys() {
        PreparedExecution pe = new PreparedExecution();
        pe.getParams().put("foo", "FOO");
        pe.getParams().put("bar", "BAR");
        pe.getParams().put("baz", "BAZ");
        assertThat(pe, paramKeys(hasItem("foo")));
        assertThat(pe, paramKeys(hasSize(3)));
    }

    @Test
    public void testParamValue() {
        PreparedExecution pe = new PreparedExecution();
        pe.getParams().put("foo", "FOO");
        pe.getParams().put("number", 100);

        assertThat(pe, paramValue("foo", is((Object) "FOO")));

        assertThat(pe, paramValue("foo", (Matcher) startsWith("FOO")));
        assertThat(pe, paramValue("number", is((Object) 100)));
    }

    @Test
    public void testParamValueWithClass() {
        PreparedExecution pe = new PreparedExecution();
        pe.getParams().put("foo", "FOO");
        pe.getParams().put("number", 100);

        assertThat(pe, paramValue("foo", String.class, is("FOO")));
        assertThat(pe, paramValue("foo", String.class, startsWith("FOO")));
        assertThat(pe, paramValue("number", Integer.class, is(100)));
    }
}