//package org.example.board_cafe_kiosk_2603.mapper.admin;
//
//import lombok.extern.log4j.Log4j2;
//import org.example.board_cafe_kiosk_2603.admin.point.model.Customer;
//import org.example.board_cafe_kiosk_2603.admin.point.mapper.CustomerMapper;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.transaction.annotation.Transactional;
//
//@Log4j2
//@SpringBootTest
//@Transactional
//class CustomerMapperTest {
//    @Autowired
//    private CustomerMapper customerMapper;
//
//    @Test
//    void insertCustomerTest() {
//        String phone = "010" + System.currentTimeMillis();
//        Customer customer = Customer.builder()
//                .phone(phone)
//                .isActive(true)
//                .build();
//
//        customerMapper.insertCustomer(customer);
//        log.info("등록된 고객: {}", customer);
//
//    }
//
//    @Test
//    void selectByPhoneTest() {
//        String phone = "010" + System.currentTimeMillis();
//        customerMapper.insertCustomer(Customer.builder().phone(phone).isActive(true).build());
//        Customer found = customerMapper.selectByPhone(phone);
//        log.info("조회된 고객: {}", found);
//    }
//
//}
