package com.infy.bank360.common.feign;

import com.infy.bank360.common.dto.AccountSummaryDTO;
import com.infy.bank360.common.dto.ApiResponse;
import com.infy.bank360.common.dto.BalanceOperationDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "account-service", path = "/infybank/accounts")
public interface AccountFeignClient {

    @GetMapping("/{accountNumber}")
    ApiResponse<AccountSummaryDTO> getAccount(@PathVariable("accountNumber") String accountNumber);

    @PostMapping("/{accountNumber}/debit")
    ApiResponse<AccountSummaryDTO> executeDebit(
            @PathVariable("accountNumber") String accountNumber,
            @RequestBody BalanceOperationDTO debitRequest
    );

    @PostMapping("/{accountNumber}/credit")
    ApiResponse<AccountSummaryDTO> executeCredit(
            @PathVariable("accountNumber") String accountNumber,
            @RequestBody BalanceOperationDTO creditRequest
    );
}
