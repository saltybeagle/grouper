/*
 * Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package edu.internet2.middleware.grouper.shibboleth;

import java.util.Set;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.provider.BasicAttribute;

/**
 * A representation of an attribute consisting of Members.
 */
public class MembersField {

  /** the attribute name */
  private String id;

  /** the underlying field */
  private Field field;

  /** the filter which retrieves members */
  private FieldMemberFilter memberFilter;

  /**
   * Constructor.
   * 
   * @param id
   *          the name of the attribute
   * @param memberFilter
   *          the filter which defines memberships as immediate, effective, or composite
   * @param field
   *          the underlying field
   */
  public MembersField(String id, FieldMemberFilter memberFilter, Field field) {
    this.id = id;
    this.memberFilter = memberFilter;
    this.field = field;
  }

  /**
   * Get the resultant attribute whose values are the Members of the given Group.
   * 
   * @param group
   *          the group
   * @return the attribute consisting of Members or <tt>null</tt> if there are no members
   */
  public BaseAttribute<Member> getAttribute(Group group) {

    Set<Member> members = memberFilter.getMembers(group, field);

    if (!members.isEmpty()) {
      BasicAttribute<Member> attribute = new BasicAttribute<Member>(id);
      attribute.setValues(members);
      return attribute;
    }

    return null;
  }

  /**
   * Get the attribute id.
   * 
   * @return the name of the underlying attribute
   */
  public String getId() {
    return id;
  }
}
