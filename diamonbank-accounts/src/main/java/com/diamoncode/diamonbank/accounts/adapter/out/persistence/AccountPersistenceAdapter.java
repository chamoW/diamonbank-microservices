package com.diamoncode.diamonbank.accounts.adapter.out.persistence;

import com.diamoncode.diamonbank.accounts.adapter.in.web.feing.cards.CardsFeingClient;
import com.diamoncode.diamonbank.accounts.adapter.in.web.feing.loans.LoansFeingClient;
import com.diamoncode.diamonbank.accounts.adapter.out.persistence.mapper.AccountMapper;
import com.diamoncode.diamonbank.accounts.adapter.out.persistence.model.JpaEntityAccount;
import com.diamoncode.diamonbank.accounts.adapter.out.persistence.repository.AccountsRepository;
import com.diamoncode.diamonbank.accounts.aplication.port.out.AccountPort;
import com.diamoncode.diamonbank.accounts.aplication.port.out.dto.*;
import com.diamoncode.diamonbank.accounts.domain.Account;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class AccountPersistenceAdapter implements AccountPort {

    private final AccountsRepository accountsRepository;


    private final LoansFeingClient loansFeingClient;

    private final CardsFeingClient cardsFeingClient;

    private final AccountMapper accountMapper;


    @Override
    public List<AccountDto> getAccountsByUser(Long userId) {
        List<JpaEntityAccount> accounts = accountsRepository.findByCustomerId(userId);
        return accountMapper.mapToDomain(accounts);
    }

    @Override
    public ConsolidatePositionDto getConsolidatePosition(Long accountId) {
        CustomerDto customerDto = new CustomerDto(accountId);
        List<JpaEntityAccount> accounts = accountsRepository.findByCustomerId(accountId);
        List<LoansDto> loans = loansFeingClient.getLoans(customerDto);
        List<CardsDto> cards = cardsFeingClient.getCardDetails(customerDto);

        log.info("acounts ", accounts.size());
        log.info("loans ", loans.size());
        log.info("cards ", cards.size());

        return ConsolidatePositionDto.builder()
                .accounts(accountMapper.mapToDomain(accounts))
                .loans(loans)
                .cards(cards)
                .build();
    }


    @Override
    public Account loadAccount(Long accountId, LocalDateTime now) {
        return null;
    }

    @Override
    public AccountDto getAccountByUser(long idAccount) {
        Optional<JpaEntityAccount> account = accountsRepository.findByAccountId(idAccount);

        if (account.isPresent()) {
            log.error("The account for found");
            return accountMapper.mapToDomain(account.get());
        }

        log.error("account by user is null");
        return null;
    }
}
