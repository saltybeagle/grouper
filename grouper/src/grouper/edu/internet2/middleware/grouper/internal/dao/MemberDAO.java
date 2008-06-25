/*
  Copyright (C) 2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2007 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper.internal.dao;
import java.util.Set;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberNotFoundException;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;

/** 
 * Basic <code>Member</code> DAO interface.
 * @author  blair christensen.
 * @version $Id: MemberDAO.java,v 1.6 2008-06-25 05:46:05 mchyzer Exp $
 * @since   1.2.0
 */
public interface MemberDAO extends GrouperDAO {

  /**
   * @since   1.2.0
   */
  void create(Member _m) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  boolean exists(String uuid) 
    throws  GrouperDAOException;

  /**
   * @since   1.3.0
   */
  Set<Member> findAll() 
    throws  GrouperDAOException;
  
  /**
   * @since   1.3.0
   */
  Set<Member> findAll(Source source) 
    throws  GrouperDAOException;
  
  /**
   * @since   1.2.0
   */
  Member findBySubject(Subject subj)
    throws  GrouperDAOException,
            MemberNotFoundException
            ;

  /**
   * @since   1.2.0
   */
  Member findBySubject(String id, String src, String type) 
    throws  GrouperDAOException,
            MemberNotFoundException
            ;

  /**
   * @since   1.2.0
   */
  Member findByUuid(String uuid) 
    throws  GrouperDAOException,
            MemberNotFoundException
            ;

  /**
   * @since   1.2.0
   */
  void update(Member _m) 
    throws  GrouperDAOException;

  /**
   * update the exists cache
   * @param uuid
   * @param exists
   */
  public void existsCachePut(String uuid, boolean exists);

  /**
   * remove from cache
   * @param uuid
   */
  public void uuid2dtoCacheRemove(String uuid);

} 

