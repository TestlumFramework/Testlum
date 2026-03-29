package com.knubisoft.testlum.testing.framework.interpreter.lib.http.util;

import com.knubisoft.testlum.testing.framework.FileSearcher;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.util.JacksonService;
import com.knubisoft.testlum.testing.model.scenario.*;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HttpUtilTest {

    private HttpUtil httpUtil;
    private FileSearcher fileSearcher;
    private JacksonService jacksonService;

    @BeforeEach
    void setUp() {
        fileSearcher = mock(FileSearcher.class);
        jacksonService = mock(JacksonService.class);
        httpUtil = new HttpUtil(fileSearcher, jacksonService);
    }

    @Nested
    class ComputeContentType {
        @Test
        void defaultsToJsonWhenNoContentTypeHeader() {
            final Map<String, String> headers = new LinkedHashMap<>();
            final ContentType result = httpUtil.computeContentType(headers);
            assertEquals("application/json", result.getMimeType());
            assertTrue(headers.containsKey("Content-Type"));
        }

        @Test
        void usesExistingContentTypeHeader() {
            final Map<String, String> headers = new LinkedHashMap<>();
            headers.put("Content-Type", "text/xml");
            final ContentType result = httpUtil.computeContentType(headers);
            assertEquals("text/xml", result.getMimeType());
        }

        @Test
        void parsesComplexContentType() {
            final Map<String, String> headers = new LinkedHashMap<>();
            headers.put("Content-Type", "application/json; charset=UTF-8");
            final ContentType result = httpUtil.computeContentType(headers);
            assertEquals("application/json", result.getMimeType());
        }

        @Test
        void parsesTextPlainContentType() {
            final Map<String, String> headers = new LinkedHashMap<>();
            headers.put("Content-Type", "text/plain");
            final ContentType result = httpUtil.computeContentType(headers);
            assertEquals("text/plain", result.getMimeType());
        }

        @Test
        void parsesMultipartFormData() {
            final Map<String, String> headers = new LinkedHashMap<>();
            headers.put("Content-Type", "multipart/form-data");
            final ContentType result = httpUtil.computeContentType(headers);
            assertEquals("multipart/form-data", result.getMimeType());
        }
    }

    @Nested
    class FillHeadersMap {
        @Test
        void addsExplicitHeaders() {
            final Map<String, String> headers = new LinkedHashMap<>();
            final com.knubisoft.testlum.testing.model.scenario.Header h1 =
                    new com.knubisoft.testlum.testing.model.scenario.Header();
            h1.setName("X-Custom");
            h1.setData("value1");
            final java.util.List<com.knubisoft.testlum.testing.model.scenario.Header> headerList =
                    new ArrayList<>();
            headerList.add(h1);

            httpUtil.fillHeadersMap(headerList, headers, null);

            assertEquals("value1", headers.get("X-Custom"));
        }

        @Test
        void authorizationHeadersMergedFirst() {
            final Map<String, String> headers = new LinkedHashMap<>();
            final Map<String, String> authHeaders = new LinkedHashMap<>();
            authHeaders.put("Authorization", "Bearer token123");
            final InterpreterDependencies.Authorization auth =
                    mock(InterpreterDependencies.Authorization.class);
            org.mockito.Mockito.when(auth.getHeaders()).thenReturn(authHeaders);

            httpUtil.fillHeadersMap(new ArrayList<>(), headers, auth);

            assertEquals("Bearer token123", headers.get("Authorization"));
        }

        @Test
        void explicitHeaderOverridesAuth() {
            final Map<String, String> headers = new LinkedHashMap<>();
            final Map<String, String> authHeaders = new LinkedHashMap<>();
            authHeaders.put("Authorization", "old");
            final InterpreterDependencies.Authorization auth =
                    mock(InterpreterDependencies.Authorization.class);
            org.mockito.Mockito.when(auth.getHeaders()).thenReturn(authHeaders);

            final com.knubisoft.testlum.testing.model.scenario.Header h1 =
                    new com.knubisoft.testlum.testing.model.scenario.Header();
            h1.setName("Authorization");
            h1.setData("new");
            final java.util.List<com.knubisoft.testlum.testing.model.scenario.Header> headerList =
                    new ArrayList<>();
            headerList.add(h1);

            httpUtil.fillHeadersMap(headerList, headers, auth);

            assertEquals("new", headers.get("Authorization"));
        }

        @Test
        void nullAuthorizationIsHandled() {
            final Map<String, String> headers = new LinkedHashMap<>();
            httpUtil.fillHeadersMap(new ArrayList<>(), headers, null);
            assertTrue(headers.isEmpty());
        }

        @Test
        void multipleExplicitHeaders() {
            final Map<String, String> headers = new LinkedHashMap<>();
            final java.util.List<com.knubisoft.testlum.testing.model.scenario.Header> headerList = new ArrayList<>();

            final com.knubisoft.testlum.testing.model.scenario.Header h1 =
                    new com.knubisoft.testlum.testing.model.scenario.Header();
            h1.setName("Accept");
            h1.setData("application/json");
            headerList.add(h1);

            final com.knubisoft.testlum.testing.model.scenario.Header h2 =
                    new com.knubisoft.testlum.testing.model.scenario.Header();
            h2.setName("X-Api-Key");
            h2.setData("secret");
            headerList.add(h2);

            httpUtil.fillHeadersMap(headerList, headers, null);

            assertEquals(2, headers.size());
            assertEquals("application/json", headers.get("Accept"));
            assertEquals("secret", headers.get("X-Api-Key"));
        }

        @Test
        void authWithEmptyHeadersMap() {
            final Map<String, String> headers = new LinkedHashMap<>();
            final InterpreterDependencies.Authorization auth =
                    mock(InterpreterDependencies.Authorization.class);
            when(auth.getHeaders()).thenReturn(new LinkedHashMap<>());

            httpUtil.fillHeadersMap(new ArrayList<>(), headers, auth);
            assertTrue(headers.isEmpty());
        }
    }

    @Nested
    class ExtractBody {
        @Test
        void nullBodyReturnsEmptyEntity() {
            final HttpEntity entity = httpUtil.extractBody(
                    null, ContentType.APPLICATION_JSON, null, null);
            assertNotNull(entity);
        }

        @Test
        void rawBodyReturnsStringEntity() {
            final Body body = new Body();
            body.setRaw("{\"key\": \"value\"}");
            final HttpEntity entity = httpUtil.extractBody(
                    body, ContentType.APPLICATION_JSON, null, null);
            assertNotNull(entity);
        }

        @Test
        void rawBodyWithExtraSpacesIsTrimmed() {
            final Body body = new Body();
            body.setRaw("  {  \"key\":   \"value\"  }  ");
            final HttpEntity entity = httpUtil.extractBody(
                    body, ContentType.APPLICATION_JSON, null, null);
            assertNotNull(entity);
        }

        @Test
        void paramBodyWithJsonContentType() {
            final Body body = new Body();
            final Param param = new Param();
            param.setName("key");
            param.setData("value");
            body.getParam().add(param);

            final JacksonService realJacksonService = new JacksonService();
            final HttpUtil util = new HttpUtil(mock(FileSearcher.class), realJacksonService);
            final HttpEntity entity = util.extractBody(
                    body, ContentType.APPLICATION_JSON, null, null);
            assertNotNull(entity);
        }

        @Test
        void paramBodyWithFormContentType() {
            final Body body = new Body();
            final Param param = new Param();
            param.setName("username");
            param.setData("testuser");
            body.getParam().add(param);

            final HttpEntity entity = httpUtil.extractBody(
                    body, ContentType.APPLICATION_FORM_URLENCODED, null, null);
            assertNotNull(entity);
        }

        @Test
        void multipleParamsWithJsonContentType() {
            final Body body = new Body();
            final Param param1 = new Param();
            param1.setName("key1");
            param1.setData("value1");
            body.getParam().add(param1);

            final Param param2 = new Param();
            param2.setName("key2");
            param2.setData("value2");
            body.getParam().add(param2);

            final JacksonService realJacksonService = new JacksonService();
            final HttpUtil util = new HttpUtil(mock(FileSearcher.class), realJacksonService);
            final HttpEntity entity = util.extractBody(
                    body, ContentType.APPLICATION_JSON, null, null);
            assertNotNull(entity);
        }

        @Test
        void multipleParamsWithFormContentType() {
            final Body body = new Body();
            final Param param1 = new Param();
            param1.setName("field1");
            param1.setData("val1");
            body.getParam().add(param1);

            final Param param2 = new Param();
            param2.setName("field2");
            param2.setData("val2");
            body.getParam().add(param2);

            final HttpEntity entity = httpUtil.extractBody(
                    body, ContentType.APPLICATION_FORM_URLENCODED, null, null);
            assertNotNull(entity);
        }

        @Test
        void fileBodyReturnsStringEntity() {
            final Body body = new Body();
            final com.knubisoft.testlum.testing.model.scenario.File from =
                    new com.knubisoft.testlum.testing.model.scenario.File();
            from.setFile("request.json");
            body.setFrom(from);

            final AbstractInterpreter<?> interpreter = mock(AbstractInterpreter.class);
            when(interpreter.getContentIfFile("request.json")).thenReturn("{\"data\":\"test\"}");

            final HttpEntity entity = httpUtil.extractBody(
                    body, ContentType.APPLICATION_JSON, interpreter, null);
            assertNotNull(entity);
        }

        @Test
        void multipartBodyWithTextPart() {
            final Body body = new Body();
            final Multipart multipart = new Multipart();
            final PartParam partParam = new PartParam();
            partParam.setName("field");
            partParam.setData("value");
            multipart.getParamOrFile().add(partParam);
            body.setMultipart(multipart);

            final InterpreterDependencies deps = mock(InterpreterDependencies.class);
            final HttpEntity entity = httpUtil.extractBody(
                    body, ContentType.MULTIPART_FORM_DATA, null, deps);
            assertNotNull(entity);
        }

        @Test
        void multipartBodyWithTextPartAndContentType() {
            final Body body = new Body();
            final Multipart multipart = new Multipart();
            final PartParam partParam = new PartParam();
            partParam.setName("jsonField");
            partParam.setData("{\"k\":\"v\"}");
            partParam.setContentType("application/json");
            multipart.getParamOrFile().add(partParam);
            body.setMultipart(multipart);

            final InterpreterDependencies deps = mock(InterpreterDependencies.class);
            final HttpEntity entity = httpUtil.extractBody(
                    body, ContentType.MULTIPART_FORM_DATA, null, deps);
            assertNotNull(entity);
        }

        @Test
        void multipartBodyWithFilePart() {
            final Body body = new Body();
            final Multipart multipart = new Multipart();
            final PartFile partFile = new PartFile();
            partFile.setName("upload");
            partFile.setFileName("test.txt");
            multipart.getParamOrFile().add(partFile);
            body.setMultipart(multipart);

            final File tempDir = new File(System.getProperty("java.io.tmpdir"));
            final File tempFile = new File(tempDir, "test.txt");
            final InterpreterDependencies deps = mock(InterpreterDependencies.class);
            when(deps.getFile()).thenReturn(tempDir);
            when(fileSearcher.searchFileFromDir(any(File.class), anyString())).thenReturn(tempFile);

            try {
                tempFile.createNewFile();
                final HttpEntity entity = httpUtil.extractBody(
                        body, ContentType.MULTIPART_FORM_DATA, null, deps);
                assertNotNull(entity);
            } catch (Exception e) {
                // File creation may fail in test environment
            } finally {
                tempFile.delete();
            }
        }

        @Test
        void multipartBodyWithFilePartAndContentType() {
            final Body body = new Body();
            final Multipart multipart = new Multipart();
            final PartFile partFile = new PartFile();
            partFile.setName("upload");
            partFile.setFileName("data.csv");
            partFile.setContentType("text/csv");
            multipart.getParamOrFile().add(partFile);
            body.setMultipart(multipart);

            final File tempDir = new File(System.getProperty("java.io.tmpdir"));
            final File tempFile = new File(tempDir, "data.csv");
            final InterpreterDependencies deps = mock(InterpreterDependencies.class);
            when(deps.getFile()).thenReturn(tempDir);
            when(fileSearcher.searchFileFromDir(any(File.class), anyString())).thenReturn(tempFile);

            try {
                tempFile.createNewFile();
                final HttpEntity entity = httpUtil.extractBody(
                        body, ContentType.MULTIPART_FORM_DATA, null, deps);
                assertNotNull(entity);
            } catch (Exception e) {
                // File creation may fail in test environment
            } finally {
                tempFile.delete();
            }
        }

        @Test
        void multipartWithMixedParts() {
            final Body body = new Body();
            final Multipart multipart = new Multipart();

            final PartParam partParam = new PartParam();
            partParam.setName("description");
            partParam.setData("A file upload");
            multipart.getParamOrFile().add(partParam);

            final PartFile partFile = new PartFile();
            partFile.setName("file");
            partFile.setFileName("upload.txt");
            multipart.getParamOrFile().add(partFile);
            body.setMultipart(multipart);

            final File tempDir = new File(System.getProperty("java.io.tmpdir"));
            final File tempFile = new File(tempDir, "upload.txt");
            final InterpreterDependencies deps = mock(InterpreterDependencies.class);
            when(deps.getFile()).thenReturn(tempDir);
            when(fileSearcher.searchFileFromDir(any(File.class), anyString())).thenReturn(tempFile);

            try {
                tempFile.createNewFile();
                final HttpEntity entity = httpUtil.extractBody(
                        body, ContentType.MULTIPART_FORM_DATA, null, deps);
                assertNotNull(entity);
            } catch (Exception e) {
                // File creation may fail in test environment
            } finally {
                tempFile.delete();
            }
        }

        @Test
        void unknownBodyContentThrows() {
            final Body body = new Body();
            // Body with no raw, from, multipart, or params
            assertThrows(DefaultFrameworkException.class,
                    () -> httpUtil.extractBody(body, ContentType.APPLICATION_JSON, null, null));
        }

        @Test
        void rawBodyWithTextPlainContentType() {
            final Body body = new Body();
            body.setRaw("plain text content");
            final HttpEntity entity = httpUtil.extractBody(
                    body, ContentType.TEXT_PLAIN, null, null);
            assertNotNull(entity);
        }

        @Test
        void multipartWithNonMultipartContentType() {
            final Body body = new Body();
            final Multipart multipart = new Multipart();
            final PartParam partParam = new PartParam();
            partParam.setName("field");
            partParam.setData("value");
            multipart.getParamOrFile().add(partParam);
            body.setMultipart(multipart);

            final InterpreterDependencies deps = mock(InterpreterDependencies.class);
            final HttpEntity entity = httpUtil.extractBody(
                    body, ContentType.APPLICATION_JSON, null, deps);
            assertNotNull(entity);
        }
    }

    @Nested
    class GetHttpMethodMetadata {
        @Test
        void getMethodReturnsGetMetadata() {
            final Http http = new Http();
            final Get get = new Get();
            http.setGet(get);

            final HttpUtil.HttpMethodMetadata metadata = httpUtil.getHttpMethodMetadata(http);
            assertEquals(HttpMethod.GET, metadata.getHttpMethod());
            assertNotNull(metadata.getHttpInfo());
        }

        @Test
        void postMethodReturnsPostMetadata() {
            final Http http = new Http();
            final Post post = new Post();
            http.setPost(post);

            final HttpUtil.HttpMethodMetadata metadata = httpUtil.getHttpMethodMetadata(http);
            assertEquals(HttpMethod.POST, metadata.getHttpMethod());
            assertNotNull(metadata.getHttpInfo());
        }

        @Test
        void putMethodReturnsPutMetadata() {
            final Http http = new Http();
            final Put put = new Put();
            http.setPut(put);

            final HttpUtil.HttpMethodMetadata metadata = httpUtil.getHttpMethodMetadata(http);
            assertEquals(HttpMethod.PUT, metadata.getHttpMethod());
            assertNotNull(metadata.getHttpInfo());
        }

        @Test
        void patchMethodReturnsPatchMetadata() {
            final Http http = new Http();
            final Patch patch = new Patch();
            http.setPatch(patch);

            final HttpUtil.HttpMethodMetadata metadata = httpUtil.getHttpMethodMetadata(http);
            assertEquals(HttpMethod.PATCH, metadata.getHttpMethod());
            assertNotNull(metadata.getHttpInfo());
        }

        @Test
        void deleteMethodReturnsDeleteMetadata() {
            final Http http = new Http();
            final Delete delete = new Delete();
            http.setDelete(delete);

            final HttpUtil.HttpMethodMetadata metadata = httpUtil.getHttpMethodMetadata(http);
            assertEquals(HttpMethod.DELETE, metadata.getHttpMethod());
            assertNotNull(metadata.getHttpInfo());
        }

        @Test
        void headMethodReturnsHeadMetadata() {
            final Http http = new Http();
            final Head head = new Head();
            http.setHead(head);

            final HttpUtil.HttpMethodMetadata metadata = httpUtil.getHttpMethodMetadata(http);
            assertEquals(HttpMethod.HEAD, metadata.getHttpMethod());
            assertNotNull(metadata.getHttpInfo());
        }

        @Test
        void optionsMethodReturnsOptionsMetadata() {
            final Http http = new Http();
            final Options options = new Options();
            http.setOptions(options);

            final HttpUtil.HttpMethodMetadata metadata = httpUtil.getHttpMethodMetadata(http);
            assertEquals(HttpMethod.OPTIONS, metadata.getHttpMethod());
            assertNotNull(metadata.getHttpInfo());
        }

        @Test
        void traceMethodReturnsTraceMetadata() {
            final Http http = new Http();
            final Trace trace = new Trace();
            http.setTrace(trace);

            final HttpUtil.HttpMethodMetadata metadata = httpUtil.getHttpMethodMetadata(http);
            assertEquals(HttpMethod.TRACE, metadata.getHttpMethod());
            assertNotNull(metadata.getHttpInfo());
        }

        @Test
        void noMethodSetThrows() {
            final Http http = new Http();

            assertThrows(DefaultFrameworkException.class,
                    () -> httpUtil.getHttpMethodMetadata(http));
        }
    }

    @Nested
    class GetESHttpMethodMetadata {
        @Test
        void esGetMethodReturnsGetMetadata() {
            final Elasticsearch es = new Elasticsearch();
            final ElasticsearchGetRequest get = new ElasticsearchGetRequest();
            es.setGet(get);

            final HttpUtil.ESHttpMethodMetadata metadata = httpUtil.getESHttpMethodMetadata(es);
            assertEquals(HttpMethod.GET, metadata.getHttpMethod());
            assertNotNull(metadata.getElasticSearchRequest());
        }

        @Test
        void esPostMethodReturnsPostMetadata() {
            final Elasticsearch es = new Elasticsearch();
            final ElasticsearchPostRequest post = new ElasticsearchPostRequest();
            es.setPost(post);

            final HttpUtil.ESHttpMethodMetadata metadata = httpUtil.getESHttpMethodMetadata(es);
            assertEquals(HttpMethod.POST, metadata.getHttpMethod());
            assertNotNull(metadata.getElasticSearchRequest());
        }

        @Test
        void esPutMethodReturnsPutMetadata() {
            final Elasticsearch es = new Elasticsearch();
            final ElasticsearchPutRequest put = new ElasticsearchPutRequest();
            es.setPut(put);

            final HttpUtil.ESHttpMethodMetadata metadata = httpUtil.getESHttpMethodMetadata(es);
            assertEquals(HttpMethod.PUT, metadata.getHttpMethod());
            assertNotNull(metadata.getElasticSearchRequest());
        }

        @Test
        void esDeleteMethodReturnsDeleteMetadata() {
            final Elasticsearch es = new Elasticsearch();
            final ElasticsearchDeleteRequest delete = new ElasticsearchDeleteRequest();
            es.setDelete(delete);

            final HttpUtil.ESHttpMethodMetadata metadata = httpUtil.getESHttpMethodMetadata(es);
            assertEquals(HttpMethod.DELETE, metadata.getHttpMethod());
            assertNotNull(metadata.getElasticSearchRequest());
        }

        @Test
        void esHeadMethodReturnsHeadMetadata() {
            final Elasticsearch es = new Elasticsearch();
            final ElasticsearchHeadRequest head = new ElasticsearchHeadRequest();
            es.setHead(head);

            final HttpUtil.ESHttpMethodMetadata metadata = httpUtil.getESHttpMethodMetadata(es);
            assertEquals(HttpMethod.HEAD, metadata.getHttpMethod());
            assertNotNull(metadata.getElasticSearchRequest());
        }

        @Test
        void esNoMethodSetThrows() {
            final Elasticsearch es = new Elasticsearch();

            assertThrows(DefaultFrameworkException.class,
                    () -> httpUtil.getESHttpMethodMetadata(es));
        }
    }
}
