package com.openweb4.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void overviewReturns200WithPriceAndWhaleKeys() throws Exception {
        mockMvc.perform(get("/api/overview").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.btc").exists())
                .andExpect(jsonPath("$.eth").exists())
                .andExpect(jsonPath("$.usdt").exists())
                .andExpect(jsonPath("$.transactions").isArray())
                .andExpect(jsonPath("$.flowData").isArray());
    }

    @Test
    void newsApiReturns200WithArticlesKey() throws Exception {
        mockMvc.perform(get("/api/news").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.articles").isArray())
                .andExpect(jsonPath("$.page").exists())
                .andExpect(jsonPath("$.size").exists())
                .andExpect(jsonPath("$.total").exists())
                .andExpect(jsonPath("$.totalPages").exists());
    }

    @Test
    void tweetsApiReturns200WithTweetsKey() throws Exception {
        mockMvc.perform(get("/api/tweets/latest").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tweets").isArray())
                .andExpect(jsonPath("$.page").exists())
                .andExpect(jsonPath("$.size").exists())
                .andExpect(jsonPath("$.total").exists())
                .andExpect(jsonPath("$.totalPages").exists());
    }

    @Test
    void debugProxyTestEndpointRemoved() throws Exception {
        mockMvc.perform(get("/api/debug/proxy-test"))
                .andExpect(status().isNotFound());
    }
}
