/*
 * Copyright (C) 2014 Stefan Niederhauser (nidin@gmx.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package guru.nidi.ramltester;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 *
 */
public class ContentNegotiationTest extends HighlevelTestBase {

    private static RamlDefinition simple = RamlLoaders.fromClasspath(ContentNegotiationTest.class).load("simple.raml");

    @Test
    public void noAcceptHeaderIsOk() throws Exception {
        assertNoViolations(test(simple, get("/"), jsonResponse(200)));
    }

    @Test
    public void emptyAcceptHeaderIsOk() throws Exception {
        assertNoViolations(test(simple, get("/").header("Accept", " "), jsonResponse(200)));
    }

    @Test
    public void noResponseContentTypeIsOk() throws Exception {
        assertNoViolations(
                test(simple, get("/mediaType").accept("bla/blu"), response(201, "", null)));
    }

    @Test
    public void simpleMatchingAcceptHeader() throws Exception {
        assertNoViolations(test(simple, get("/schema").accept("application/json"), jsonResponse(200, "\"x\"")));
    }

    @Test
    public void wildcardMatchingAcceptHeader() throws Exception {
        assertNoViolations(test(simple, get("/schema").accept("application/*"), jsonResponse(200, "\"x\"")));
        assertNoViolations(test(simple, get("/schema").accept("*/*"), jsonResponse(200, "\"x\"")));
    }

    @Test
    public void nonMatchingResponse() throws Exception {
        assertOneResponseViolationThat(
                test(simple, get("/schema").accept("application/bla", "x/y"), jsonResponse(200, "\"x\"")),
                equalTo("Response Content-Type 'application/json' is not compatible with Accept header 'application/bla, x/y'"));
    }

    @Test
    public void invalidResponseMimeType() throws Exception {
        assertOneResponseViolationThat(
                test(simple, get("/mediaType").header("Accept", "text/xml"), response(201, "", "text")),
                equalTo("Illegal media type 'text' in response(201): Does not contain '/'"));
    }

    @Test
    public void useQValue() throws Exception {
        assertNoViolations(
                test(simple, get("/mediaType").accept("text/plain;q=.5", "text/xml"), response(201, "", "text/xml")));
        assertOneResponseViolationThat(
                test(simple, get("/mediaType").accept("text/plain;q=.5", "text/xml"), response(201, "", "text/plain")),
                equalTo("Given the Accept header 'text/plain;q=.5, text/xml', the response to action(GET /mediaType) with code 201 should have media type 'text/xml', not 'text/plain'"));
    }

    @Test
    public void invalidQ() throws Exception {
        assertOneRequestViolationThat(
                test(simple, get("/mediaType").header("Accept", "text/xml;q=b,text/plain"), response(201, "", "text/plain")),
                equalTo("Illegal media type 'text/xml;q=b' in Accept header: Invalid quality value 'b': Should be between 0.0 and 1.0"));
    }

}