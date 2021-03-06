package com.ddimitroff.projects.dwallet.rest.controllers;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ddimitroff.projects.dwallet.db.entities.CashBalance;
import com.ddimitroff.projects.dwallet.db.entities.CashFlow;
import com.ddimitroff.projects.dwallet.enums.CashFlowCurrencyType;
import com.ddimitroff.projects.dwallet.enums.CashFlowType;
import com.ddimitroff.projects.dwallet.enums.CashFlowsReportType;
import com.ddimitroff.projects.dwallet.managers.CashBalanceManager;
import com.ddimitroff.projects.dwallet.managers.CashFlowManager;
import com.ddimitroff.projects.dwallet.rest.DWalletErrorUtils;
import com.ddimitroff.projects.dwallet.rest.DWalletRestUtils;
import com.ddimitroff.projects.dwallet.rest.cash.CashBalanceRO;
import com.ddimitroff.projects.dwallet.rest.cash.CashFlowRO;
import com.ddimitroff.projects.dwallet.rest.cash.CashRecordRO;
import com.ddimitroff.projects.dwallet.rest.cash.CashReportRO;
import com.ddimitroff.projects.dwallet.rest.response.ErrorResponse;
import com.ddimitroff.projects.dwallet.rest.response.Responsable;
import com.ddimitroff.projects.dwallet.rest.response.Response;
import com.ddimitroff.projects.dwallet.rest.token.Token;
import com.ddimitroff.projects.dwallet.rest.token.TokenRO;
import com.ddimitroff.projects.dwallet.rest.token.TokenWatcher;
import com.ddimitroff.projects.dwallet.xml.exchange.DWalletExchangeRatesParser;

/**
 * Spring REST implementation for cash record operations. It is used as Spring
 * MVC component.
 * 
 * @author Dimitar Dimitrov
 * 
 */
@Controller
public class CashRecordsRestService {

  /** Logger constant */
  private static final Logger logger = Logger.getLogger(CashRecordsRestService.class);

  /** Injected {@link CashBalanceManager} component by Spring */
  @Autowired
  private CashBalanceManager cashBalanceManager;

  /** Injected {@link CashFlowManager} component by Spring */
  @Autowired
  private CashFlowManager cashFlowManager;

  /** Injected {@link TokenWatcher} component by Spring */
  @Autowired
  private TokenWatcher tokenWatcher;

  /** Injected {@link DWalletExchangeRatesParser} component by Spring */
  @Autowired
  private DWalletExchangeRatesParser exchangeRatesParser;

  /** Injected supported API keys component by Spring */
  @Autowired
  private ArrayList<String> apiKeys;

  /**
   * HTTP POST method for getting cash balance by specifying existing token in
   * system<br>
   * Request path: /cash/balance<br>
   * Headers needed:<br>
   * Content-Type: application/json<br>
   * DWallet-API-Key: api-key
   * 
   * @param apiKey
   *          - valid application key, represented as header in HTTP POST
   *          request
   * @param tokenRO
   *          - valid token in system, represented as {@link TokenRO} object
   * @return {@link Responsable} object representing success or error response
   *         as JSON notation<br>
   *         Example: {"currency":"1", "profit":"11.8", "cost":"11.8"}
   */
  @RequestMapping(method = RequestMethod.POST, value = "/cash/balance")
  @ResponseBody
  public Responsable getCashBalance(
      @RequestHeader(value = DWalletRestUtils.DWALLET_REQUEST_HEADER, required = false) String apiKey,
      @RequestBody TokenRO tokenRO) {

    ErrorResponse errorResponse = new ErrorResponse();

    if (DWalletRestUtils.isValidAPIKey(apiKey, apiKeys)) {
      if (null != tokenRO) {
        Token token = tokenWatcher.getTokenById(tokenRO.getTokenId());
        if (null != token) {
          CashBalance cashBalanceEntity = cashBalanceManager.getCashBalanceByUser(token.getOwner());
          if (null != cashBalanceEntity) {
            CashBalanceRO output = cashBalanceManager.convert(cashBalanceEntity);
            return output;
          } else {
            logger.error("Unable to find cash balance for user " + token.getOwner());
            errorResponse.setErrorCode(DWalletErrorUtils.ERR030_CODE);
            errorResponse.setErrorMessage(DWalletErrorUtils.ERR030_MSG);
          }
        } else {
          logger.error("Unable to find token with id " + tokenRO.getTokenId());
          errorResponse.setErrorCode(DWalletErrorUtils.ERR002_CODE);
          errorResponse.setErrorMessage(DWalletErrorUtils.ERR002_MSG);
        }
      } else {
        logger.error("Wrong request body post of cash record");
        errorResponse.setErrorCode(DWalletErrorUtils.ERR010_CODE);
        errorResponse.setErrorMessage(DWalletErrorUtils.ERR010_MSG);
      }
    } else {
      logger.error("Wrong 'd-wallet' API key");
      errorResponse.setErrorCode(DWalletErrorUtils.ERR001_CODE);
      errorResponse.setErrorMessage(DWalletErrorUtils.ERR001_MSG);
    }

    return errorResponse;
  }

  /**
   * HTTP POST method for posting new cash record to the system<br>
   * Request path: /cash/post<br>
   * Headers needed:<br>
   * Content-Type: application/json<br>
   * DWallet-API-Key: api-key
   * 
   * @param apiKey
   *          - valid application key, represented as header in HTTP POST
   *          request
   * @param cashRecordRO
   *          - valid cash record, represented as {@link CashRecordRO} object
   * @return {@link Responsable} object representing success or error response
   *         as JSON notation
   * 
   */
  @RequestMapping(method = RequestMethod.POST, value = "/cash/post")
  @ResponseBody
  public Responsable postCashRecord(
      @RequestHeader(value = DWalletRestUtils.DWALLET_REQUEST_HEADER, required = false) String apiKey,
      @RequestBody CashRecordRO cashRecordRO) {

    ErrorResponse errorResponse = new ErrorResponse();

    if (DWalletRestUtils.isValidAPIKey(apiKey, apiKeys)) {
      if (null != cashRecordRO) {
        Token token = tokenWatcher.getTokenById(cashRecordRO.getToken().getTokenId());
        if (null != token) {
          long start = System.nanoTime();

          CashBalance cashBalanceEntity = cashBalanceManager.getCashBalanceByUser(token.getOwner());
          if (null == cashBalanceEntity) {
            if (token.getOwner().getStartupBalance() < 0) {
              cashBalanceEntity = new CashBalance(token.getOwner(), token.getOwner().getDefaultCurrency(), 0, token
                  .getOwner().getStartupBalance());
            } else {
              cashBalanceEntity = new CashBalance(token.getOwner(), token.getOwner().getDefaultCurrency(), token
                  .getOwner().getStartupBalance(), 0);
            }
          }

          // iterate over every cash flow from the record
          for (int i = 0; i < cashRecordRO.getCashFlows().size(); i++) {
            CashFlowRO current = cashRecordRO.getCashFlows().get(i);
            CashFlow currentEntity = cashFlowManager.convert(current, token.getOwner());

            manageCashFlowCurrencies(currentEntity);

            if (CashFlowType.PROFIT == currentEntity.getType()) {
              cashBalanceEntity.setDebit(cashBalanceEntity.getDebit() + currentEntity.getSum());
            } else {
              cashBalanceEntity.setCredit(cashBalanceEntity.getCredit() + currentEntity.getSum());
            }

            cashFlowManager.save(currentEntity);
          }

          cashBalanceManager.save(cashBalanceEntity);
          logger.info("Importing cash record from user " + token.getOwner() + " finished in "
              + (System.nanoTime() - start) / 1000000 + " ms.");

          Response successResponse = new Response(true);
          return successResponse;
        } else {
          logger.error("Unable to find token with id " + cashRecordRO.getToken().getTokenId());
          errorResponse.setErrorCode(DWalletErrorUtils.ERR002_CODE);
          errorResponse.setErrorMessage(DWalletErrorUtils.ERR002_MSG);
        }
      } else {
        logger.error("Wrong request body post of cash record");
        errorResponse.setErrorCode(DWalletErrorUtils.ERR012_CODE);
        errorResponse.setErrorMessage(DWalletErrorUtils.ERR012_MSG);
      }
    } else {
      logger.error("Wrong 'd-wallet' API key");
      errorResponse.setErrorCode(DWalletErrorUtils.ERR001_CODE);
      errorResponse.setErrorMessage(DWalletErrorUtils.ERR001_MSG);
    }

    return errorResponse;
  }

  // TODO insert method for getting cash flows reports by time periods - daily,
  // weekly, monthly, year basis

  /**
   * HTTP POST method for getting specified cash report by posting existing
   * token in system<br>
   * Request path: /cash/report/*cash-flows-report-type*<br>
   * Headers needed:<br>
   * Content-Type: application/json<br>
   * DWallet-API-Key: api-key
   * 
   * @param apiKey
   *          - valid application key, represented as header in HTTP POST
   *          request
   * @param type
   *          - valid URL path parameter describing cash flows report type
   * @param tokenRO
   *          - valid token in system, represented as {@link TokenRO} object
   * @return {@link Responsable} object representing success or error response
   *         as JSON notation
   * 
   */
  @RequestMapping(method = RequestMethod.POST, value = "/cash/report/{type}")
  @ResponseBody
  public Responsable getCashReport(
      @RequestHeader(value = DWalletRestUtils.DWALLET_REQUEST_HEADER, required = false) String apiKey,
      @PathVariable int type, @RequestBody TokenRO tokenRO) {

    ErrorResponse errorResponse = new ErrorResponse();

    if (DWalletRestUtils.isValidAPIKey(apiKey, apiKeys)) {
      if (null != tokenRO) {
        Token token = tokenWatcher.getTokenById(tokenRO.getTokenId());
        if (null != token) {
          CashFlowsReportType cashFlowsReportType = CashFlowsReportType.getType(type);
          CashReportRO output = cashFlowManager.generateCashReportByUserAndType(token.getOwner(), cashFlowsReportType);
          if (null != output) {
              return output;
          } else {
              logger.error("Unable to generate " + cashFlowsReportType.toString() + " cash report for user " + token.getOwner());
              errorResponse.setErrorCode(DWalletErrorUtils.ERR040_CODE);
              errorResponse.setErrorMessage(DWalletErrorUtils.ERR040_MSG);
          }
        } else {
          logger.error("Unable to find token with id " + tokenRO.getTokenId());
          errorResponse.setErrorCode(DWalletErrorUtils.ERR002_CODE);
          errorResponse.setErrorMessage(DWalletErrorUtils.ERR002_MSG);
        }
      } else {
        logger.error("Wrong request body get of cash report");
        errorResponse.setErrorCode(DWalletErrorUtils.ERR010_CODE);
        errorResponse.setErrorMessage(DWalletErrorUtils.ERR010_MSG);
      }
    } else {
      logger.error("Wrong 'd-wallet' API key");
      errorResponse.setErrorCode(DWalletErrorUtils.ERR001_CODE);
      errorResponse.setErrorMessage(DWalletErrorUtils.ERR001_MSG);
    }

    return errorResponse;
  }

  /**
   * A method for checking every cash flow and transforming each currency to
   * BGN. Round double sum value to second sign after dot
   * 
   * @param cashflow
   *          - cash flow entity to manage its currencies
   */
  private void manageCashFlowCurrencies(CashFlow cashflow) {
    double factor = 0.0;
    double refactoredSum = cashflow.getSum();

    switch (cashflow.getOwner().getDefaultCurrency()) {
    case BGN:
      factor = currencyExchangeFactor(cashflow.getCurrencyType(), CashFlowCurrencyType.BGN);
      refactoredSum *= factor;
      cashflow.setSum(DWalletRestUtils.roundTwoDecimals(refactoredSum));
      cashflow.setCurrencyType(CashFlowCurrencyType.BGN);
      break;
    case USD:
      factor = currencyExchangeFactor(cashflow.getCurrencyType(), CashFlowCurrencyType.USD);
      refactoredSum *= factor;
      cashflow.setSum(DWalletRestUtils.roundTwoDecimals(refactoredSum));
      cashflow.setCurrencyType(CashFlowCurrencyType.USD);
      break;
    case EUR:
      factor = currencyExchangeFactor(cashflow.getCurrencyType(), CashFlowCurrencyType.EUR);
      refactoredSum *= factor;
      cashflow.setSum(DWalletRestUtils.roundTwoDecimals(refactoredSum));
      cashflow.setCurrencyType(CashFlowCurrencyType.EUR);
      break;
    }
  }

  /**
   * A helper method for determining currency exchange rate factor.
   * 
   * @param from
   *          - currency to change from
   * @param to
   *          - currency to change to
   * 
   * @return determined currency exchange rate factor
   */
  private double currencyExchangeFactor(CashFlowCurrencyType from, CashFlowCurrencyType to) {
    double exchangeRateFrom = 1.0;
    if (CashFlowCurrencyType.BGN != from) {
      exchangeRateFrom = exchangeRatesParser.getExchangeRateByCurrency(from.name());
    }

    double exchangeRateTo = 1.0;
    if (CashFlowCurrencyType.BGN != to) {
      exchangeRateTo = exchangeRatesParser.getExchangeRateByCurrency(to.name());
    }

    return exchangeRateFrom / exchangeRateTo;
  }

}