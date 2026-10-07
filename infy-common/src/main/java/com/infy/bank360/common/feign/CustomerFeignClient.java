package com.infy.bank360.common.feign;

import com.infy.bank360.common.dto.ApiResponse;
import com.infy.bank360.common.dto.CustomerSummaryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "customer-service", path = "/infybank/customers")
public interface CustomerFeignClient {

    @GetMapping("/{customerId}")
    ApiResponse<CustomerSummaryDTO> getCustomer(@PathVariable("customerId") Long customerId);
}
