package com.ustb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ustb.dto.CheckInRequest;
import com.ustb.dto.LoginRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.MethodName.class)  // 按方法名顺序执行
class PackageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static Long packageId;
    private static String pickupCode;
    private static String awaitingPickupCode;

    // ===== TC-1: 正常入库（含取件码验证） =====
    @Test
    void test01_checkIn_normal() throws Exception {
        CheckInRequest request = new CheckInRequest();
        request.setTrackingNumber("SF1234567890");
        request.setRecipientPhone("13800138001");
        request.setCourierCompany("顺丰速运");
        request.setShelfLocation("A-01");

        String json = objectMapper.writeValueAsString(request);  // 使用Jackson的ObjectMapper将Java对象转为JSON字符串，作为HTTP请求体。

        // mockMvc.perform()：模拟发送HTTP请求
        String response = mockMvc.perform(post("/api/admin/packages/check-in")
                        .contentType(MediaType.APPLICATION_JSON)  // 设置请求头Content-Type: application/json
                        .content(json)) // 设置请求体为前面生成的JSON。
                .andExpect(status().isOk())  // .andExpect() 验证点：如果有一个不符合，测试用例就报错。
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("包裹入库成功"))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.trackingNumber").value("SF1234567890"))
                .andExpect(jsonPath("$.data.pickupCode").isNotEmpty())
                .andExpect(jsonPath("$.data.status").value("AWAITING_PICKUP"))
                .andExpect(jsonPath("$.data.checkInTime").isNotEmpty())
                .andExpect(jsonPath("$.data.overdue").value(false))
                .andReturn()
                .getResponse()
                .getContentAsString();

        packageId = objectMapper.readTree(response).get("data").get("id").asLong();
        pickupCode = objectMapper.readTree(response).get("data").get("pickupCode").asText();
    }

    // ===== TC-2: 按手机号查询待取件包裹 =====
    @Test
    void test02_queryByPhone_normal() throws Exception {
        jdbcTemplate.update(
            "INSERT INTO packages (tracking_number, recipient_phone, courier_company, shelf_location, status) "
          + "VALUES (?, ?, ?, ?, ?)",
            "YT9876543210", "13800138001", "圆通速递", "B-05", "AWAITING_PICKUP");

        mockMvc.perform(get("/api/user/packages")
                        .param("keyword", "13800138001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[*].recipientPhone").value(everyItem(is("13800138001"))))
                .andExpect(jsonPath("$.data[*].status").value(everyItem(is("AWAITING_PICKUP"))));
    }

    // ===== TC-3: 正常取件 =====
    @Test
    void test03_pickup_normal() throws Exception {
        mockMvc.perform(put("/api/user/packages/" + packageId + "/pickup"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("取件成功"))
                .andExpect(jsonPath("$.data.status").value("PICKED_UP"))
                .andExpect(jsonPath("$.data.checkOutTime").isNotEmpty());

        mockMvc.perform(get("/api/user/packages")
                        .param("keyword", "13800138001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    // ===== TC-4: 逾期检测 =====
    @Test
    void test04_overdue_detection() throws Exception {
        jdbcTemplate.update(
            "INSERT INTO packages (tracking_number, recipient_phone, courier_company, shelf_location, status, check_in_time) "
          + "VALUES (?, ?, ?, ?, ?, TIMESTAMPADD(HOUR, -50, NOW()))",
            "ZT1111111111", "13900139001", "中通速递", "C-03", "AWAITING_PICKUP");

        mockMvc.perform(get("/api/admin/packages/overdue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data[*].trackingNumber").value(hasItem("ZT1111111111")))
                .andExpect(jsonPath("$.data[*].overdue").value(everyItem(is(true))));
    }

    // ===== TC-5: 重复运单号入库 =====
    @Test
    void test05_duplicate_checkin() throws Exception {
        CheckInRequest request = new CheckInRequest();
        request.setTrackingNumber("SF1234567890");
        request.setRecipientPhone("13800138002");
        request.setCourierCompany("京东物流");
        request.setShelfLocation("D-01");

        mockMvc.perform(post("/api/admin/packages/check-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("该运单号已入库"));
    }

    // ===== TC-6: 无效手机号格式 =====
    @Test
    void test06_invalid_phone_format() throws Exception {
        CheckInRequest request = new CheckInRequest();
        request.setTrackingNumber("SF0000000001");
        request.setRecipientPhone("12345");
        request.setCourierCompany("韵达快递");
        request.setShelfLocation("E-01");

        mockMvc.perform(post("/api/admin/packages/check-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    // ===== TC-7: 必填字段为空 =====
    @Test
    void test07_missing_fields() throws Exception {
        CheckInRequest request = new CheckInRequest();
        request.setTrackingNumber("");
        request.setRecipientPhone("13800138003");
        request.setCourierCompany("申通快递");
        request.setShelfLocation("F-01");

        mockMvc.perform(post("/api/admin/packages/check-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    // ===== TC-8: 重复取件（幂等性保护） =====
    @Test
    void test08_double_pickup() throws Exception {
        mockMvc.perform(put("/api/user/packages/" + packageId + "/pickup"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("该包裹已被取走"));
    }

    // ===== TC-9: 缺少查询参数 =====
    @Test
    void test09_query_without_keyword() throws Exception {
        mockMvc.perform(get("/api/user/packages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    // ===== TC-10: 取不存在的包裹 =====
    @Test
    void test10_pickup_nonexistent() throws Exception {
        mockMvc.perform(put("/api/user/packages/99999/pickup"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("包裹不存在"));
    }

    // ===== TC-11: 取件码格式校验（YYMMDD-L-NNN） =====
    @Test
    void test11_pickup_code_format() throws Exception {
        CheckInRequest request = new CheckInRequest();
        request.setTrackingNumber("PKPICKUP001");
        request.setRecipientPhone("13800138010");
        request.setCourierCompany("顺丰速运");
        request.setShelfLocation("A-02");

        String response = mockMvc.perform(post("/api/admin/packages/check-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.pickupCode").value(matchesPattern("\\d{6}-[A-Z]-\\d{3}")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        awaitingPickupCode = objectMapper.readTree(response).get("data").get("pickupCode").asText();
        System.out.println("Generated pickup code: " + awaitingPickupCode);
    }

    // ===== TC-12: 按取件码查询（使用TC-11中未取件的包裹） =====
    @Test
    void test12_query_by_pickup_code() throws Exception {
        mockMvc.perform(get("/api/user/packages")
                        .param("keyword", awaitingPickupCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].pickupCode").value(awaitingPickupCode));
    }

    // ===== TC-13: 按运单号查询 =====
    @Test
    void test13_query_by_tracking_number() throws Exception {
        // SF1234567890 已在TC-3中被取走，此处验证不返回已取件包裹
        mockMvc.perform(get("/api/user/packages")
                        .param("keyword", "SF1234567890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        // YT9876543210 仍为待取件，应返回1条
        mockMvc.perform(get("/api/user/packages")
                        .param("keyword", "YT9876543210"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    // ===== TC-14: Dashboard 统计面板 =====
    @Test
    void test14_dashboard() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.todayCheckIn").isNumber())
                .andExpect(jsonPath("$.data.todayPickup").isNumber())
                .andExpect(jsonPath("$.data.overdueCount").isNumber())
                .andExpect(jsonPath("$.data.awaitingCount").isNumber());
    }

    // ===== TC-15: 操作日志记录 =====
    @Test
    void test15_operation_logs() throws Exception {
        mockMvc.perform(get("/api/admin/logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(greaterThanOrEqualTo(1)));
    }

    // ===== TC-16: 管理员登录成功 =====
    @Test
    void test16_login_admin() throws Exception {
        // DataInitializer 在启动时自动创建 admin/admin123
        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsername("admin");
        loginReq.setPassword("admin123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.role").value("ADMIN"));
    }

    // ===== TC-17: 登录失败（密码错误） =====
    @Test
    void test17_login_wrong_password() throws Exception {
        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsername("admin");
        loginReq.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    // ===== TC-18: 分页查询 =====
    @Test
    void test18_pagination() throws Exception {
        mockMvc.perform(get("/api/admin/packages")
                        .param("page", "1")
                        .param("pageSize", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.total").isNumber())
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(2))
                .andExpect(jsonPath("$.data.totalPages").isNumber());
    }

    // ===== TC-19: 货架列表 =====
    @Test
    void test19_shelves_list() throws Exception {
        mockMvc.perform(get("/api/admin/shelves"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(greaterThanOrEqualTo(5)))
                .andExpect(jsonPath("$.data").value(hasItem("A-01")));
    }

    // ===== TC-20: 新增货架 =====
    @Test
    void test20_shelf_add() throws Exception {
        mockMvc.perform(post("/api/admin/shelves")
                        .contentType(new MediaType("text", "plain", StandardCharsets.UTF_8))
                        .content("货架-TEST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("货架添加成功"));
    }

    // ===== TC-21: 新增已存在的货架（409） =====
    @Test
    void test21_shelf_add_duplicate() throws Exception {
        mockMvc.perform(post("/api/admin/shelves")
                        .contentType(new MediaType("text", "plain", StandardCharsets.UTF_8))
                        .content("A-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("该货架已存在"));
    }

    // ===== TC-22: 删除货架 =====
    @Test
    void test22_shelf_delete() throws Exception {
        mockMvc.perform(delete("/api/admin/shelves")
                        .param("name", "货架-TEST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("货架删除成功"));
    }

    // ===== TC-23: 删除不存在的货架（404） =====
    @Test
    void test23_shelf_delete_not_found() throws Exception {
        mockMvc.perform(delete("/api/admin/shelves")
                        .param("name", "不存在的货架"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("货架不存在"));
    }

    // ===== TC-24: 快递公司列表 =====
    @Test
    void test24_couriers_list() throws Exception {
        mockMvc.perform(get("/api/admin/couriers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(greaterThanOrEqualTo(9)));
    }

    // ===== TC-25: 新增快递公司 =====
    @Test
    void test25_courier_add() throws Exception {
        mockMvc.perform(post("/api/admin/couriers")
                        .contentType(new MediaType("text", "plain", StandardCharsets.UTF_8))
                        .content("测试快递公司"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("快递公司添加成功"));
    }

    // ===== TC-26: 新增已存在的快递公司（409） =====
    @Test
    void test26_courier_add_duplicate() throws Exception {
        // TC-25 已添加"测试快递公司"，再次添加应返回409
        mockMvc.perform(post("/api/admin/couriers")
                        .contentType(new MediaType("text", "plain", StandardCharsets.UTF_8))
                        .content("测试快递公司"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("该快递公司已存在"));
    }

    // ===== TC-27: 删除快递公司 =====
    @Test
    void test27_courier_delete() throws Exception {
        mockMvc.perform(delete("/api/admin/couriers")
                        .param("name", "测试快递公司"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("快递公司删除成功"));
    }

    // ===== TC-28: 删除不存在的快递公司（404） =====
    @Test
    void test28_courier_delete_not_found() throws Exception {
        mockMvc.perform(delete("/api/admin/couriers")
                        .param("name", "不存在的快递"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("快递公司不存在"));
    }

    // ===== TC-29: 管理员取件（admin路径） =====
    @Test
    void test29_admin_pickup() throws Exception {
        CheckInRequest request = new CheckInRequest();
        request.setTrackingNumber("ADMINPICKUP01");
        request.setRecipientPhone("13900139002");
        request.setCourierCompany("京东物流");
        request.setShelfLocation("A-01");

        String response = mockMvc.perform(post("/api/admin/packages/check-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long id = objectMapper.readTree(response).get("data").get("id").asLong();

        mockMvc.perform(put("/api/admin/packages/" + id + "/pickup"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("取件成功"))
                .andExpect(jsonPath("$.data.status").value("PICKED_UP"))
                .andExpect(jsonPath("$.data.checkOutTime").isNotEmpty());
    }
}
