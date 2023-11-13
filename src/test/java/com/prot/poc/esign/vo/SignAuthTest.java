package com.prot.poc.esign.vo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.prot.poc.common.JSONUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-11-07T23:01 Tuesday
 */
class SignAuthTest {
    private static final String EXPECT_KEVIN_JSON_STR = """
            {"name":"Kevin","phone":"647-878-6767","signAuth":{"__type":"None"}}""";
    private static final String EXPECT_DAVID_JSON_STR = """
{"name":"David","phone":"416-336-9093","signAuth":{"__type":"Phone","phoneNumbers":["6278978710"]}}""";
    private static final String EXPECT_JASON_JSON_STR = """
{"name":"Jason","phone":"416-336-9093","signAuth":{"__type":"SMS","phoneNumbers":["6278978710"]}}""";
    private Signer kevin;
    private Signer david;
    private Signer jason;
    private List<Signer> signers;

    @BeforeEach
    void setUp() {
        kevin = new Signer("Kevin", "647-878-6767", SignAuth.NONE);
        david = new Signer("David", "416-336-9093", new SignAuth.Phone("6278978710"));
        jason = new Signer("Jason", "416-336-9093", new SignAuth.SMS("6278978710"));
        signers = Arrays.asList(david, jason, kevin);
    }

    @Test
    void testKevin() {
        String jsonString = JSONUtils.toJSONString(kevin);
        System.out.println(jsonString);
        assertEquals(EXPECT_KEVIN_JSON_STR, jsonString);
        Signer kevin2 = JSONUtils.fromJSONString(jsonString, Signer.class);
        String jsonString2 = JSONUtils.toJSONString(kevin2);
        assertEquals(EXPECT_KEVIN_JSON_STR, jsonString2);
        assertEquals(kevin, kevin2);
    }

    @Test
    void testDavid() {
        String jsonString = JSONUtils.toJSONString(david);
        System.out.println(jsonString);
        assertEquals(EXPECT_DAVID_JSON_STR, jsonString);
        Signer david2 = JSONUtils.fromJSONString(jsonString, Signer.class);
        String jsonString2 = JSONUtils.toJSONString(david2);
        assertEquals(EXPECT_DAVID_JSON_STR, jsonString2);
        assertEquals(david, david2);
    }

    @Test
    void testJason() {
        String jsonString = JSONUtils.toJSONString(jason);
        System.out.println(jsonString);
        assertEquals(EXPECT_JASON_JSON_STR, jsonString);
        Signer jason2 = JSONUtils.fromJSONString(jsonString, Signer.class);
        String jsonString2 = JSONUtils.toJSONString(jason2);
        assertEquals(EXPECT_JASON_JSON_STR, jsonString2);
        assertEquals(jason, jason2);
    }

    @Test
    void testAll() {
        String jsonString = JSONUtils.toJSONString(signers);
        System.out.println(jsonString);
        List<Signer> signers2 = JSONUtils.fromJSONString(jsonString, new TypeReference<List<Signer>>() {
            @Override
            public Type getType() {
                return super.getType();
            }
        });
        assertEquals("[" + EXPECT_DAVID_JSON_STR + ","
                + EXPECT_JASON_JSON_STR + ","
                + EXPECT_KEVIN_JSON_STR + "]", jsonString);
        assertEquals(signers, signers2);
    }

    @AfterEach
    void tearDown() {
    }

    record Signer(String name, String phone, SignAuth signAuth) {
    }
}