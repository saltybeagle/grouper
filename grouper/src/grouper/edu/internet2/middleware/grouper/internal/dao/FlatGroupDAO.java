/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.flat.FlatGroup;


/**
 * 
 */
public interface FlatGroupDAO extends GrouperDAO {

  /**
   * insert or update a flat group object
   * @param flatGroup
   */
  public void saveOrUpdate(FlatGroup flatGroup);
  
  /**
   * insert a batch of flat group objects
   * @param flatGroups
   */
  public void saveBatch(Set<FlatGroup> flatGroups);
  
  /**
   * delete a flat group object
   * @param flatGroup
   */
  public void delete(FlatGroup flatGroup);
  
  /**
   * @param flatGroupId
   * @return flat group
   */
  public FlatGroup findById(String flatGroupId);
  
  /**
   * @param flatGroupId
   */
  public void removeGroupForeignKey(String flatGroupId);
  
  /**
   * find missing flat groups
   * @param page
   * @param batchSize
   * @return set of groups that need flat groups
   */
  public Set<Group> findMissingFlatGroups(int page, int batchSize);
  
  /**
   * find missing flat groups count
   * @return long
   */
  public long findMissingFlatGroupsCount();
  
  /**
   * remove bad flat groups
   * @return set of flat groups that should be removed
   */
  public Set<FlatGroup> findBadFlatGroups();
}