package org.example.board_cafe_kiosk_2603.admin.point.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.board_cafe_kiosk_2603.admin.point.model.Customer;

@Mapper
public interface CustomerMapper {

    // 신규 고객 등록
    void insertCustomer(Customer customer);

    // 전화번호로 고객 조회
    Customer selectByPhone(String phone);
}
