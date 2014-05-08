package guru.nidi.ramltester;

import guru.nidi.ramltester.spring.SpringMockHttpRequest;
import guru.nidi.ramltester.spring.SpringMockHttpResponse;
import org.hamcrest.Matcher;
import org.hamcrest.core.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 *
 */
public class TestBase {
    protected MockHttpServletRequest get(String url) {
        return MockMvcRequestBuilders.get(url).buildRequest(new MockServletContext());
    }

    protected MockHttpServletRequest post(String url) {
        return MockMvcRequestBuilders.post(url).buildRequest(new MockServletContext());
    }

    protected Matcher<String> startsWith(String s) {
        return new StringStartsWith(s);
    }

    protected Matcher<String> contains(String s) {
        return new StringContains(s);
    }

    protected Matcher<String> endsWith(String s) {
        return new StringEndsWith(s);
    }

    @SafeVarargs
    protected final <T> Matcher<T> allOf(Matcher<? super T>... matcher) {
        return new AllOf<>(Arrays.asList(matcher));
    }

    protected MockHttpServletResponse jsonResponse(int code, String json, String contentType) throws UnsupportedEncodingException {
        final MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(code);
        response.setContentType(contentType);
        response.getWriter().print(json);
        return response;
    }

    protected MockHttpServletResponse jsonResponse(int code, String json) throws UnsupportedEncodingException {
        return jsonResponse(code, json, "application/json");
    }

    protected void assertNoViolation(RamlDefinition raml, MockHttpServletRequest request, MockHttpServletResponse response) {
        final RamlViolations violations = new RamlTester()
                .test(raml, new SpringMockHttpRequest("http://nidi.guru/raml/v1", request), new SpringMockHttpResponse(response));
        assertNoViolations(violations);
    }

    protected void assertNoViolations(RamlViolations violations) {
        assertTrue("Expected no violations, but found: " + violations, violations.getViolations().isEmpty());
    }

    protected void assertOneViolationThat(RamlDefinition raml, MockHttpServletRequest request, MockHttpServletResponse response, Matcher<String> matcher) {
        final RamlViolations violations = new RamlTester()
                .test(raml, new SpringMockHttpRequest("http://nidi.guru/raml/v1", request), new SpringMockHttpResponse(response));
        assertOneViolationThat(violations, matcher);
    }

    protected void assertOneViolationThat(RamlViolations violations, Matcher<String> matcher) {
        assertThat("Expected exactly one violation", 1, new IsEqual<>(violations.getViolations().size()));
        assertThat(violations.getViolations().get(0), matcher);
    }

    protected void assertStringArrayMapEquals(Object[] expected, Map<String, String[]> actual) {
        Map<String, String[]> v = stringArrayMapOf(expected);
        assertEquals(v.size(), actual.size());
        for (Map.Entry<String, String[]> entry : v.entrySet()) {
            assertArrayEquals(entry.getValue(), actual.get(entry.getKey()));
        }
    }

    protected Map<String, String[]> stringArrayMapOf(Object... keysAndValues) {
        Map<String, String[]> v = new HashMap<>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            String[] value = keysAndValues[i + 1] instanceof String
                    ? new String[]{(String) keysAndValues[i + 1]}
                    : (String[]) keysAndValues[i + 1];
            v.put((String) keysAndValues[i], value);
        }
        return v;
    }

    protected <T> Map<String, T> mapOf(Object... keysAndValues) {
        Map<String, T> v = new HashMap<>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            v.put((String) keysAndValues[i], (T) keysAndValues[i + 1]);
        }
        return v;
    }
}