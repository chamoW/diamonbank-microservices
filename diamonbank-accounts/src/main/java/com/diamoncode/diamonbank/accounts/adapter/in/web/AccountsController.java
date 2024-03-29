package com.diamoncode.diamonbank.accounts.adapter.in.web;

import com.diamoncode.auditor.AuditorFactory;
import com.diamoncode.auditor.exception.AuditException;
import com.diamoncode.diamonbank.accounts.adapter.in.web.config.AccountsServiceConfig;
import com.diamoncode.diamonbank.accounts.aplication.port.in.AccountsUseCase;
import com.diamoncode.diamonbank.accounts.aplication.port.out.dto.AccountDto;
import com.diamoncode.diamonbank.accounts.aplication.port.out.dto.PropertiesDto;
import com.diamoncode.i18n.client.I18nFactory;
import com.diamondcode.common.adapter.in.web.model.ResponseDTO;
import com.diamondcode.common.adapter.in.web.model.WebAdapterResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping(GlobalController.ACCOUNTS_REQUEST_MAPPING)
@RequiredArgsConstructor
public class AccountsController extends WebAdapterResponse {
    private static final Logger logger = LoggerFactory.getLogger(AccountsController.class);

    private final AccountsUseCase accountsUseCase;

    private final AccountsServiceConfig accountsServiceConfig;


    @GetMapping("/getHost")
    public ResponseDTO getHost() throws JsonProcessingException {

        Optional<String> podName = Optional.ofNullable(System.getenv("HOSTNAME"));
        Map<String, String> variables = System.getenv();

        String greeting = "Welcome to K8S cluster from " + variables;
        logger.info(greeting);

        return getResponse("", greeting);

    }

    @GetMapping("/properties")
    public ResponseDTO getProperties() throws JsonProcessingException {

        PropertiesDto properties = new PropertiesDto(accountsServiceConfig.getMsg(), accountsServiceConfig.getBuildVersion()
                , accountsServiceConfig.getMailDetails(), accountsServiceConfig.getActiveBranches());

        logger.info("Properties found ", properties.toString());

        return getResponse("", properties);

    }


    @GetMapping("/myAccount/{idAccount}")
    public ResponseDTO getAccountDetails(@PathVariable("idAccount") long idAccount) {
        AccountDto accountDto = accountsUseCase.findById(idAccount);
        logger.info("acount data ", accountDto.toString());
        return getResponse("", accountDto);

    }

    @GetMapping("/testI18n/{withDefault}")
    public ResponseDTO testI18n(@PathVariable("withDefault") boolean withDefault) {
        String headerName = null;
        if (withDefault) {
            headerName = I18nFactory.getInstance().getMessage("header.name", "es", "Valor por defecto");
        } else {
            headerName = I18nFactory.getInstance().getMessage("header.name", "es");
        }

        logger.info("headerName value ", headerName);


        ResponseDTO responseDTO = getResponse("Respuesta obtenida desde I18N", headerName);


        try {
            AuditorFactory.send(responseDTO.toString());
        } catch (AuditException e) {
            logger.error("error sending data to rabbit");
        }

        return responseDTO;

    }


}
