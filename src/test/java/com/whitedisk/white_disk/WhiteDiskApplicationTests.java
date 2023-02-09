package com.whitedisk.white_disk;

import com.whitedisk.white_disk.service.api.IUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static jdk.nashorn.internal.runtime.regexp.joni.Config.log;


@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class WhiteDiskApplicationTests {

    @Autowired
    IUserService userService;
    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() throws Exception {
        String url = "/user/login";
//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("username", "test");
//        jsonObject.put("telephone", "13919223005");
//        jsonObject.put("password", "123456");

        MvcResult mvcResult=mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .param("telephone","19916943853")
                        .param("password","bj010616")
                        .accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        int status=mvcResult.getResponse().getStatus();
        String content=mvcResult.getResponse().getContentAsString();
        log.print(status);
        log.print(content);
    }
}
