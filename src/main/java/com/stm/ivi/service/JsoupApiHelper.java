package com.stm.ivi.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stm.ivi.exception.RetryException;
import com.stm.ivi.retrying.RetryCount;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JsoupApiHelper {

    private static final String BODY = "body";
    private static final String LOG_IF_FAIL = "\n\tUrl:\n\t\t%s\n\tBODY:\n%s\n\tHEADER:\n%s\n\tCOOKIE:\n%s";

    @Autowired
    private ObjectMapper objectMapper;

    public <T> Optional<T> postForObject(
        @NonNull String url,
        @NonNull TypeReference<T> typeReference,
        Map<String, String> body,
        Map<String, String> headers,
        Map<String, String> cookies,
        String logIfFail
    ) {
        return receiveObjectFromApiWithRetry(url, Connection.Method.GET, body, headers, cookies, typeReference, logIfFail, null);
    }

    public <T> Optional<T> getForObject(
            @NonNull String url,
            @NonNull TypeReference<T> typeReference,
            Map<String, String> cookies,
            String logIfFail
    ) {
        return receiveObjectFromApiWithRetry(url, Connection.Method.GET, null, null, cookies, typeReference, logIfFail, null);
    }

    public  <T> Optional<T> postForObject(
            @NonNull String url,
            @NonNull TypeReference<T> typeReference,
            Map<String, String> body,
            Predicate<T> predicate,
            String logIfFail
    ) {
        return receiveObjectFromApiWithRetry(url, Connection.Method.POST, body, null, null, typeReference, logIfFail, predicate);
    }

    public <T> Optional<T> getForObject(
            @NonNull String url,
            @NonNull TypeReference<T> typeReference,
            Predicate<T> predicate,
            String logIfFail
    ) {
        return receiveObjectFromApiWithRetry(url, Connection.Method.GET, null, null, null, typeReference, logIfFail, predicate);
    }

    public <T> Optional<T> postForObject(
            @NonNull String url,
            @NonNull TypeReference<T> typeReference,
            Map<String, String> body,
            String logIfFail
    ) {
        return receiveObjectFromApiWithRetry(url, Connection.Method.POST, body, null, null, typeReference, logIfFail, null);
    }

    public <T> Optional<T> getForObject(
            @NonNull String url,
            @NonNull TypeReference<T> typeReference,
            String logIfFail
    ) {
        return receiveObjectFromApiWithRetry(url, Connection.Method.GET, null, null, null, typeReference, logIfFail, null);
    }

    public <T> Optional<T> receiveObjectFromApiWithRetry(
            @NonNull String url,
            @NonNull Connection.Method method,
            Map<String, String> body,
            Map<String, String> headers,
            Map<String, String> cookies,
            @NonNull TypeReference<T> typeReference,
            String logIfFail,
            Predicate<T> tPredicate) {
        try {
            return Optional.of(new RetryCount(10).<T>createStrategy()
                    .setTimeOutAfterFailCallFunction(1000)
                    .retryIfException(IOException.class)
                    .retryIfException(SocketTimeoutException.class)
                    .setFunction(() -> {
                        Connection.Response response = Jsoup.connect(url)
                                .followRedirects(true)
                                .ignoreContentType(true)
                                .maxBodySize(0)
                                .timeout(1000)
                                .method(method)
                                .data(body != null ? body : Collections.emptyMap())
                                .cookies(cookies != null ? cookies : Collections.emptyMap())
                                .headers(headers != null ? headers : Collections.emptyMap())
                                .execute();
                        setCookie(cookies, response.cookies());
                        String responseJson = response.parse().body().text();
                        if(responseJson != null) {
                            return objectMapper.readValue(responseJson, typeReference);
                        }
                        return null;
                    })
                    .successIf(res -> tPredicate == null || tPredicate.test(res))
                    .run());
        } catch (RetryException e) {
            log.warn(String.format(
                        LOG_IF_FAIL,
                        url,
                        receiveStringMap(body),
                        receiveStringMap(headers),
                        receiveStringMap(cookies)),
                    e);
            return Optional.empty();
        }
    }

    private void setCookie(Map<String, String> cookieReq, Map<String, String> cookieRes) {
        if(cookieReq == null || cookieRes == null || cookieRes.isEmpty()) {
            return;
        }
        if(cookieReq.isEmpty()) {
            cookieReq.putAll(cookieRes);
        }
        cookieRes.forEach(cookieReq::putIfAbsent);
    }

    private String receiveStringMap(Map<String,String> map) {
        return Optional.ofNullable(map)
                .map(m -> m.entrySet().stream()
                    .map(entry -> "\t\t" + entry.getKey() + ": " + entry.getValue())
                    .collect(Collectors.joining(StringUtils.LF)))
                .orElse(StringUtils.EMPTY);
    }

}
