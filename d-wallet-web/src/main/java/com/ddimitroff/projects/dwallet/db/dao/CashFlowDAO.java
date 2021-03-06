package com.ddimitroff.projects.dwallet.db.dao;

import java.util.Date;
import java.util.List;

import com.ddimitroff.projects.dwallet.db.entities.CashFlow;
import com.ddimitroff.projects.dwallet.db.entities.User;

/**
 * Interface for {@link CashFlow} entity object. Used to specify methods related
 * only to {@link CashFlow} entity object.
 * 
 * @author Dimitar Dimitrov
 * 
 */
public interface CashFlowDAO extends BaseDAO<CashFlow> {

  /**
   * A method for getting a list of {@link CashFlow} entity objects by
   * specifying their owner
   * 
   * @param owner
   *          - owner of {@link CashFlow} entity objects
   * 
   * @return a list of {@link CashFlow} entity objects of specified user,
   *         {@code null} otherwise
   */
  public List<CashFlow> getCashFlowsByUser(User owner);

  /**
   * A method for getting a list of {@link CashFlow} entity objects by specifying their owner and cash flow report start date
   * 
   * @param ownerArg - owner of {@link CashFlow} entity objects
   * @param reportStartDateArg - start date for cash flow report
   * 
   * @return a list of {@link CashFlow} entity objects of specified user and cash flow report start date, {@code null} otherwise
   */
  public List<CashFlow> getCashFlowsByUserAndDate(User ownerArg, Date reportStartDateArg);

}
