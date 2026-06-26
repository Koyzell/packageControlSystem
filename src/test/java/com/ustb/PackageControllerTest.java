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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.MethodName.class)
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

    // ===== TC-1: Normal Check-in (with pickup code) =====
    @Test
    void test01_checkIn_normal() throws Exception {
        CheckInRequest request = new CheckInRequest();
        request.setTrackingNumber("SF1234567890");
        request.setRecipientPhone("13800138001");
        request.setCourierCompany("顺丰速运");
        request.setShelfLocation("A-01");

        String json = objectMapper.writeValueAsString(request);

        String response = mockMvc.perform(post("/api/admin/packages/check-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
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

    // ===== TC-2: Normal Query by Phone (keyword param) =====
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

    // ===== TC-3: Normal Pickup =====
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

    // ===== TC-4: Overdue Detection =====
    @Test
    void test04_overdue_detection() throws Exception {
        jdbcTemplate.update(
            "INSERT INTO packages (tracking_number, recipient_phone, courier_company, shelf_location, status, check_in_time) "
          + "VALUES (?, ?, ?, ?, ?, DATEADD('HOUR', -50, NOW()))",
            "ZT1111111111", "13900139001", "中通速递", "C-03", "AWAITING_PICKUP");

        mockMvc.perform(get("/api/admin/packages/overdue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data[*].trackingNumber").value(hasItem("ZT1111111111")))
                .andExpect(jsonPath("$.data[*].overdue").value(everyItem(is(true))));
    }

    // ===== TC-5: Duplicate Check-in =====
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

    // ===== TC-6: Invalid Phone Format =====
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

    // ===== TC-7: Missing Required Fields =====
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

    // ===== TC-8: Double Pickup =====
    @Test
    void test08_double_pickup() throws Exception {
        mockMvc.perform(put("/api/user/packages/" + packageId + "/pickup"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("该包裹已被取走"));
    }

    // ===== TC-9: Query without keyword =====
    @Test
    void test09_query_without_keyword() throws Exception {
        mockMvc.perform(get("/api/user/packages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    // ===== TC-10: Pickup non-existent =====
    @Test
    void test10_pickup_nonexistent() throws Exception {
        mockMvc.perform(put("/api/user/packages/99999/pickup"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("包裹不存在"));
    }

    // ===== New Tests for Expansion Features =====

    // TC-11: Pickup code format
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

    // TC-12: Query by pickup code (use non-picked-up package from TC-11)
    @Test
    void test12_query_by_pickup_code() throws Exception {
        mockMvc.perform(get("/api/user/packages")
                        .param("keyword", awaitingPickupCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].pickupCode").value(awaitingPickupCode));
    }

    // TC-13: Query by tracking number
    @Test
    void test13_query_by_tracking_number() throws Exception {
        mockMvc.perform(get("/api/user/packages")
                        .param("keyword", "SF1234567890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        // SF1234567890 was picked up, so should be empty
        mockMvc.perform(get("/api/user/packages")
                        .param("keyword", "YT9876543210"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    // TC-14: Dashboard stats
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

    // TC-15: Operation logs
    @Test
    void test15_operation_logs() throws Exception {
        mockMvc.perform(get("/api/admin/logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(greaterThanOrEqualTo(1)));
    }

    // TC-16: Login success (admin)
    @Test
    void test16_login_admin() throws Exception {
        // Seed admin user
        jdbcTemplate.update(
            "INSERT INTO users (username, password, role) VALUES (?, ?, ?)",
            "admintest", "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh", "ADMIN");

        // The password hash above is for "admin123" - this won't match.
        // Let's just test with the DataInitializer-created admin account.
        // Actually the DataInitializer runs at startup, so admin/admin123 should exist.
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

    // TC-17: Login fail (wrong password)
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

    // TC-18: Pagination page 1
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
}
