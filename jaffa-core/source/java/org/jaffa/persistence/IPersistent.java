/*
 * ====================================================================
 * JAFFA - Java Application Framework For All
 *
 * Copyright (C) 2002 JAFFA Development Group
 *
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 2.1 of the License, or (at your option) any later version.
 *
 *     This library is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *     Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public
 *     License along with this library; if not, write to the Free Software
 *     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Redistribution and use of this software and associated documentation ("Software"),
 * with or without modification, are permitted provided that the following conditions are met:
 * 1.	Redistributions of source code must retain copyright statements and notices.
 *         Redistributions must also contain a copy of this document.
 * 2.	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 * 3.	The name "JAFFA" must not be used to endorse or promote products derived from
 * 	this Software without prior written permission. For written permission,
 * 	please contact mail to: jaffagroup@yahoo.com.
 * 4.	Products derived from this Software may not be called "JAFFA" nor may "JAFFA"
 * 	appear in their names without prior written permission.
 * 5.	Due credit should be given to the JAFFA Project (http://jaffa.sourceforge.net).
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 */
package org.jaffa.persistence;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Interface for all persistent objects.
 */
public interface IPersistent extends ILifecycleHandler, Cloneable, Serializable {

    /**
     * Returns the UOW to which this object is associated.
     *
     * @return The UOW
     */
    UOW getUOW();

    /**
     * Associates this object to a UOW.
     * Note: This method is for internal use by the Persistence Engine only.
     *
     * @param uow The UOW.
     */
    void setUOW(UOW uow);

    /**
     * Returns a true value if the object had any of its fields updated.
     *
     * @return a true value if the object had any of its fields updated.
     */
    boolean isModified();

    /**
     * Set the modified status of this object.
     * Note: This method is for internal use by the Persistence Engine only.
     *
     * @param modified the modified status.
     */
    void setModified(boolean modified);

    /**
     * Returns a true value if the object was loaded from the database.
     *
     * @return a true value if the object was loaded from the database.
     */
    boolean isDatabaseOccurence();

    /**
     * Set the database status of this object.
     * Note: This method is for internal use by the Persistence Engine only.
     *
     * @param databaseOccurence the database status.
     */
    void setDatabaseOccurence(boolean databaseOccurence);

    /**
     * Returns the locking strategy for this persistent object.
     *
     * @return the locking strategy for this persistent object.
     */
    int getLocking();

    /**
     * Set the locking strategy for this persistent object.
     * Note: This method is for internal use by the Persistence Engine only.
     *
     * @param locking the locking strategy.
     */
    void setLocking(int locking);

    /**
     * Returns a true value if the underlying database row is locked.
     *
     * @return a true value if the underlying database row is locked.
     */
    boolean isLocked();

    /**
     * Set the locked status of this object.
     * Note: This method is for internal use by the Persistence Engine only.
     *
     * @param locked the locked status.
     */
    void setLocked(boolean locked);

    /**
     * Returns a true value if this object has been added/updated/deleted and not yet been committed.
     *
     * @return a true value if this object has been added/updated/deleted and not yet been committed.
     */
    boolean isQueued();

    /**
     * Set the queued status of this object.
     * The Persistence Engine will set the queued status to true on an Add/Update/Delete. Thereafter, any updates on this object will throw a RuntimeException.
     * This flag will be reset after the object actually gets added/updated/deleted to the database.
     * Note: This method is for internal use by the Persistence Engine only.
     *
     * @param queued the queued status.
     */
    void setQueued(boolean queued);

    /**
     * Returns a true value if the field has been updated.
     *
     * @param fieldName the field to check.
     * @return a true value if the field has been updated.
     */
    boolean isModified(String fieldName);

    /**
     * Returns the initial value for a field; i.e. before it was modified.
     * A null will be returned if the field was never modified. Use the isModified() method to determine if the field was modified.
     * A null can also be returned if the field had a null value to begin with.
     *
     * @param fieldName the field.
     * @return the initial value for a field; i.e. before it was modified.
     */
    Object returnInitialValue(String fieldName);

    /**
     * Returns a map of modified fields.
     * The map contains the name of the modified field and it's initial value.
     * NOTE: The returned map is a read-only view, and attempts to modify the returned map will result in an UnsupportedOperationException.
     *
     * @return a map of modified fields.
     */
    Map<String, Object> getModifiedFields();

    /**
     * Gets all life cycle handlers set on a concrete persistent handler.  This allows custom code to be
     * injected before or after lifecycle methods.
     */
    List<ILifecycleHandler> getLifecycleHandlers();
}
