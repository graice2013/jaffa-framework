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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jaffa.datatypes.ValidationException;
import org.jaffa.exceptions.ApplicationException;
import org.jaffa.exceptions.ApplicationExceptions;
import org.jaffa.exceptions.DuplicateKeyException;
import org.jaffa.exceptions.FrameworkException;
import org.jaffa.metadata.FieldMetaData;
import org.jaffa.persistence.engines.jdbcengine.IStoredProcedure;
import org.jaffa.persistence.exceptions.AlreadyLockedObjectException;
import org.jaffa.persistence.exceptions.IllegalPersistentStateRuntimeException;
import org.jaffa.persistence.exceptions.PostAddFailedException;
import org.jaffa.persistence.exceptions.PostDeleteFailedException;
import org.jaffa.persistence.exceptions.PostLoadFailedException;
import org.jaffa.persistence.exceptions.PostUpdateFailedException;
import org.jaffa.persistence.exceptions.PreAddFailedException;
import org.jaffa.persistence.exceptions.PreDeleteFailedException;
import org.jaffa.persistence.exceptions.PreUpdateFailedException;
import org.jaffa.persistence.exceptions.ReadOnlyObjectException;
import org.jaffa.persistence.util.PersistentHelper;
import org.jaffa.rules.RulesEngine;
import org.jaffa.rules.fieldvalidators.Validator;
import org.jaffa.util.*;

/** Base class for all persistent objects.
 */
public abstract class Persistent implements IPersistent {

    private static final Logger log = Logger.getLogger(Persistent.class);
    // The UOW has to be transient and cannot be serialized
    // The other flags are closely related to the UOW and hence being made transient too.
    private transient UOW m_uow = null;
    private transient boolean m_modified = false;
    private transient boolean m_databaseOccurence = false;
    private transient int m_locking = Criteria.LOCKING_OPTIMISTIC;
    private transient boolean m_locked = false;
    private transient boolean m_queued = false;
    private transient Map<String, Object> m_modifiedFields = null;
    private transient Validator<Persistent> validator;//do not create a getter for validators, will cause serialization problems

    // handlers to inject custom code around lifecycle events
    private List<ILifecycleHandler> lifecycleHandlers = new ArrayList<>();

    /**
     * Adds a new handler to the beginning of the list of all handlers.
     */
    public void prependLifecycleHandlers(List<ILifecycleHandler> handlers) {
        for (ILifecycleHandler handler : handlers) {
            handler.setTargetBean(this);
        }

        // maintain order of the input list
        lifecycleHandlers.addAll(0, handlers);
    }

    /**
     * Adds a new handler to the end of the list of all handlers.
     */
    public void appendLifecycleHandlers(List<ILifecycleHandler> handlers) {
        for (ILifecycleHandler handler : handlers) {
            handler.setTargetBean(this);
            lifecycleHandlers.add(handler);
        }
    }

    /**
     * Gets the ordered list of transformation handlers to fire when invoking lifecycle events on this handler.
     */
    public List<ILifecycleHandler> getLifecycleHandlers() {
        return lifecycleHandlers;
    }

    /**
     * Sets the target instance of a LifecycleHandler on this instance of the handler.  When we add multiple
     * handlers into a list on the target bean, this will give each handler in the list access to the target
     * instance of the handler itself.
     *
     * @param targetBean the target instance of the lifecycle handler this instance is operating on.
     */
    public void setTargetBean(ILifecycleHandler targetBean) {
    }

    /** This returns the state of the object for diagnostic purposes.
     * @return a String representation of the object.
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("<Persistent>");
        if (m_uow != null)
            buf.append("<uow>").append(m_uow).append("</uow>");
        buf.append("<databaseOccurence>").append(m_databaseOccurence).append("</databaseOccurence>");
        buf.append("<modified>").append(m_modified).append("</modified>");
        if (m_modifiedFields != null)
            buf.append("<modifiedFields>").append(m_modifiedFields).append("</modifiedFields>");
        buf.append("<locked>").append(m_locked).append("</locked>");
        buf.append("<locking>").append(m_locking).append("</locking>");
        buf.append("<queued>").append(m_queued).append("</queued>");
        buf.append("</Persistent>");
        return buf.toString();
    }

    /** Returns a clone of the object.
     * @throws CloneNotSupportedException if cloning is not supported. This should never happen.
     * @return a clone of the object.
     */
    public Object clone() throws CloneNotSupportedException {
        Persistent obj = (Persistent) super.clone();

        // Initialize the fields
        obj.m_uow = null;
        //obj.m_modified = false;
        obj.m_databaseOccurence = false;
        obj.m_locking = Criteria.LOCKING_OPTIMISTIC;
        obj.m_locked = false;
        obj.m_queued = false;
        //obj.m_modifiedFields = null;
        return obj;
    }

    /** Returns the UOW to which this object is associated.
     * @return The UOW
     */
    public UOW getUOW() {
        return m_uow;
    }

    /** Associates this object to a UOW.
     * Note: This method is for internal use by the Persistence Engine only.
     * @param uow The UOW.
     */
    public void setUOW(UOW uow) {
        m_uow = uow;
    }

    /** Returns a true value if the object had any of its fields updated.
     * @return a true value if the object had any of its fields updated.
     */
    public boolean isModified() {
        return m_modified;
    }

    /** Set the modified status of this object.
     * Note: This method is for internal use by the Persistence Engine only.
     * @param modified the modified status.
     */
    public void setModified(boolean modified) {
        m_modified = modified;

        // clear the ModifiedFields map
        if (!modified && m_modifiedFields != null)
            m_modifiedFields.clear();
    }

    /** Returns a true value if the object was loaded from the database.
     * @return a true value if the object was loaded from the database.
     */
    public boolean isDatabaseOccurence() {
        return m_databaseOccurence;
    }

    /** Set the database status of this object.
     * Note: This method is for internal use by the Persistence Engine only.
     * @param databaseOccurence the database status.
     */
    public void setDatabaseOccurence(boolean databaseOccurence) {
        m_databaseOccurence = databaseOccurence;
    }

    /** Returns the locking strategy for this persistent object.
     * @return the locking strategy for this persistent object.
     */
    public int getLocking() {
        return m_locking;
    }

    /** Set the locking strategy for this persistent object.
     * Note: This method is for internal use by the Persistence Engine only.
     * @param locking the locking strategy.
     */
    public void setLocking(int locking) {
        m_locking = locking;
    }

    /** Returns a true value if the underlying database row is locked.
     * @return a true value if the underlying database row is locked.
     */
    public boolean isLocked() {
        return m_locked;
    }

    /** Set the locked status of this object.
     * Note: This method is for internal use by the Persistence Engine only.
     * @param locked the locked status.
     */
    public void setLocked(boolean locked) {
        m_locked = locked;
    }

    /** Returns a true value if this object has been added/updated/deleted and not yet been committed.
     * @return a true value if this object has been added/updated/deleted and not yet been committed.
     */
    public boolean isQueued() {
        return m_queued;
    }

    /** Set the queued status of this object.
     * The Persistence Engine will set the queued status to true on an Add/Update/Delete. Thereafter, any updates on this object will throw a RuntimeException.
     * This flag will be reset after the object actaully gets added/updated/deleted to the database.
     * Note: This method is for internal use by the Persistence Engine only.
     * @param queued the queued status.
     */
    public void setQueued(boolean queued) {
        m_queued = queued;
    }

    /**
     * Sets the validator
     * Sets the validator that is replacing the legacy AOP validation code.  If not null, the validator should be fired
     * first in the validate method of all classes that extend this class.
     *
     * @param validator the validator
     */
    public void setValidator(Validator<Persistent> validator) {
        this.validator = validator;
    }

    /** Returns a true value if the field has been updated.
     * @param fieldName the field to check.
     * @return a true value if the field has been updated.
     */
    public boolean isModified(String fieldName) {
        return m_modifiedFields != null && m_modifiedFields.containsKey(fieldName) ? true : false;
    }

    /** Returns the initial value for a field; i.e. before it was modified.
     * A null will be returned if the field was never modified. Use the isModified() method to determine if the field was modified.
     * A null will also be returned, if the field had a null value to begin with.
     * @param fieldName the field.
     * @return the initial value for a field; i.e. before it was modified.
     */
    public Object returnInitialValue(String fieldName) {
        return m_modifiedFields != null ? m_modifiedFields.get(fieldName) : null;
    }

    /** Returns a map of modified fields.
     * The map contains the name of the modified field and it's initial value.
     * NOTE: The returned map is a read-only view, and attempts to modify the returned map will result in an UnsupportedOperationException.
     * @return a map of modified fields.
     */
    public Map<String, Object> getModifiedFields() {
        return m_modifiedFields != null ? Collections.unmodifiableMap(m_modifiedFields) : null;
    }

    /** This method is triggered by the UOW, before adding this object to the Add-Store, but after a UOW has been associated to the object.
     * This will perform the following tasks:
     *    Will invoke PersistentHelper.checkMandatoryFields() to perform mandatory field checks.
     *    Will invoke the Rules Engine to perform mandatory field checks.
     * @throws PreAddFailedException if any error occurs during the process.
     */
    public void preAdd() throws PreAddFailedException {
        doPreAdd();
    }

    /**
     * Check for existence prior to adding to the database.
     */
    protected void verifyEntityDoesNotExist() throws PreAddFailedException {
        if (log.isDebugEnabled())
            log.debug("Invoking PersistentHelper.exists() to ensure unique-ness of the primary-key");
        boolean keyExists = false;
        try {
            keyExists = PersistentHelper.exists(getUOW(), this);
        } catch (NoSuchMethodException e) {
            // It is possible that the Persistent class does not have the 'exists()' method, or the meta class does not have the 'getKeyFields()' method.
            // In that case, we'll not perform the check, and leave it to the database to raise an error in case the key is a duplicate
            if (log.isDebugEnabled())
                log.debug("The exists check could not be performed since either the exists() method is missing or the meta class does not have the getKeyFields method: " + this.getClass());
        } catch (Exception e) {
            String str = "Exception thrown while checking the unique-ness of the primary-key: " + this;
            log.error(str, e);
            throw new PreAddFailedException(null, e);
        }
        if (keyExists) {
            String str = "The primary-key is not unique: " + this;
            log.error(str);
            String labelToken = null;
            try {
                labelToken = PersistentHelper.getLabelToken(this.getClass().getName());
            } catch (Exception e) {
                // don't do anything.. just return the domainClassName
            }
            if (labelToken == null)
                labelToken = MessageHelper.tokenize(this.getClass().getName());

            throw new PreAddFailedException(null, new DuplicateKeyException(labelToken));
        }
    }

    /**
     * Check for existence prior to adding to the database. This method should be overridden to
     * check for existence in cases where the normal fail-on-flush is insufficient.
     */
    protected void performPreAddExistenceCheck() throws PreAddFailedException {
      // Override to call verifyEntityDoesNotExist() if this required
    }

    /**
     * Method to separate the point cuts
     * @throws PreAddFailedException
     */
    protected void doPreAdd() throws PreAddFailedException {
        performPreAddExistenceCheck();
        if (log.isDebugEnabled())
            log.debug("Invoking validate() to validate the domain object");
        try {
            validateRules();
            validate();
        } catch (Exception e) {
            ApplicationExceptions appExps = ExceptionHelper.extractApplicationExceptions(e);
            if (appExps != null) {
                if (log.isDebugEnabled())
                    log.debug("Exception thrown while validating the domain object", appExps);
                throw new PreAddFailedException(null, appExps);
            } else {
                log.error("Exception thrown while validating the domain object", e);
                throw new PreAddFailedException(null, e);
            }
        }
    }

    /** This method is triggered by the UOW, after adding this object to the Add-Store.
     * A concrete persistent object should provide the implementation, if its necessary.
     * @throws PostAddFailedException if any error occurs during the process.
     */
    public void postAdd() throws PostAddFailedException {
    }

    /** This method is triggered by the UOW, before adding this object to the Update-Store.
     * This will perform the following tasks:
     *    Will invoke the performForeignKeyValidations() method to ensure no invalid foreign-keys are set.
     *    Will invoke PersistentHelper.checkMandatoryFields() to perform mandatory field checks.
     *    Will invoke the Rules Engine to perform mandatory field checks.
     * @throws PreUpdateFailedException if any error occurs during the process.
     */
    public void preUpdate() throws PreUpdateFailedException {
        if (log.isDebugEnabled())
            log.debug("Invoking validate() to validate the domain object");
        try {
            validateRules();
            validate();
        } catch (Exception e) {
            ApplicationExceptions appExps = ExceptionHelper.extractApplicationExceptions(e);
            if (appExps != null) {
                if (log.isDebugEnabled())
                    log.debug("Exception thrown while validating the domain object", appExps);
                throw new PreUpdateFailedException(null, appExps);
            } else {
                log.error("Exception thrown while validating the domain object", e);
                throw new PreUpdateFailedException(null, e);
            }
        }
    }

    /** This method is triggered by the UOW, after adding this object to the Update-Store.
     * A concrete persistent object should provide the implementation, if its necessary.
     * @throws PostUpdateFailedException if any error occurs during the process.
     */
    public void postUpdate() throws PostUpdateFailedException {
    }

    /** This method is triggered by the UOW, before adding this object to the Delete-Store.
     * This will perform the following tasks:
     *    Will invoke the performPreDeleteReferentialIntegrity() method.
     * @throws PreDeleteFailedException if any error occurs during the process.
     */
    public void preDelete() throws PreDeleteFailedException {
        performPreDeleteReferentialIntegrity();
    }

    /** This method is triggered by the UOW, after adding this object to the Delete-Store.
     * A concrete persistent object should provide the implementation, if its necessary.
     * @throws PostDeleteFailedException if any error occurs during the process.
     */
    public void postDelete() throws PostDeleteFailedException {
    }

    /** This method is triggered by the UOW after a query loads this object.
     * A concrete persistent object should provide the implementation, if its necessary.
     * @throws PostLoadFailedException if any error occurs during the process.
     */
    public void postLoad() throws PostLoadFailedException {
    }

    /** This method is invoked before adding/updating a domain object.
     * This will perform the following tasks:
     *    Will invoke the performForeignKeyValidations() method to ensure no invalid foreign-keys are set.
     *    Will invoke PersistentHelper.checkMandatoryFields() to perform mandatory field checks.
     *    Will invoke the Rules Engine to perform mandatory field checks.
     * @throws ApplicationExceptions if any application error occurs.
     * @throws FrameworkException Indicates some system error
     */
    public void validate() throws ApplicationExceptions, FrameworkException {
        doValidate();
    }

    /**
     * This method must be called by the class that extends Persistent in it's validate method.
     * This method should be called at the beginning of the validate method as it take the place
     * of the legacy AOP pointcut that happened before the Domain Model validate method was called.
     *
     * @throws ApplicationExceptions if any validation error occurs.
     * @throws FrameworkException    indicates some system error
     */
    private void validateRules() throws ApplicationExceptions, FrameworkException {
        if (validator != null) {
            try {
                validator.validate(this);
            } catch (ApplicationException exception) {
                //wrap all Application Exception into an Application Exceptions
                throw new ApplicationExceptions(exception);
            }
        }
    }

    /**
     *
     * @throws ApplicationExceptions
     * @throws FrameworkException
     */
    public void doValidate() throws ApplicationExceptions, FrameworkException {
        if (log.isDebugEnabled())
            log.debug("Invoking performForeignKeyValidations() to ensure valid foreign-keys are used");
        performForeignKeyValidations();

        if (log.isDebugEnabled())
            log.debug("Invoking checks for all the mandatory fields of the persistent object");
        try {
            PersistentHelper.checkMandatoryFields(this);
        } catch (FrameworkException e) {
            if (e.getCause() != null && e.getCause() instanceof ClassNotFoundException) {
                // It is possible that the Peristent meta class does not exist.
                // In that case, we'll not perform the check, and leave it to the database to raise an error in case any mandatory field is missing.
                if (log.isDebugEnabled())
                    log.debug("The mandatory field check could not be performed since the meta class does not exist for " + this.getClass());
            } else if (e.getCause() != null && e.getCause() instanceof NoSuchMethodException) {
                // It is possible that the Persistent meta class does not have the 'getMandatoryFields()' method.
                // In that case, we'll not perform the check, and leave it to the database to raise an error in case any mandatory field is missing.
                if (log.isDebugEnabled())
                    log.debug("The mandatory field check could not be performed since the meta class does not have the getMandatoryFields method: " + this.getClass());
            } else
                throw e;
        } catch (ApplicationExceptions e) {
            throw e;
        }

        try {
            if (log.isDebugEnabled())
                log.debug("Invoking the Dynamic Rules Engine to perform the mandatory rules on the Persistent object");
            RulesEngine.doMandatoryValidationsForDomainObject(this, this.getUOW());
        } catch (ValidationException e) {
            throw new ApplicationExceptions(e);
        }
    }

    /** This method ensures that the modified foreign-keys are valid.
     * A concrete persistent object should provide the implementation, if its necessary.
     * @throws ApplicationExceptions if an invalid foreign key is set.
     * @throws FrameworkException Indicates some system error
     */
    public void performForeignKeyValidations() throws ApplicationExceptions, FrameworkException {
    }

    /** This method will perform referential integrity checks before this object is deleted.
     * This will cascade delete all composite objects.
     * This will raise an exception if any associated/aggregated objects exist.
     * A concrete persistent object should provide the implementation, if its necessary.
     * @throws PreDeleteFailedException if any error occurs during the process.
     */
    public void performPreDeleteReferentialIntegrity() throws PreDeleteFailedException {
    }

    /** This method should be invoked by every updateXxx() method of the persistent class, before setting the value.
     * It ensures that a readonly object cannot be updated. It acquires a lock for the pessimistic locking strategy.
     * Finally, it sets the modified flag.
     * @throws ReadOnlyObjectException if the object has been marked as ReadOnly, and hence cannot be updated.
     * @throws AlreadyLockedObjectException if the object has been locked by another process.
     * @throws IllegalPersistentStateRuntimeException this RuntimeException will be thrown if the domain object has been submitted to the UOW for an Add/Update/Delete and commit hasnt yet been performed.
     */
    protected void update()
            throws ReadOnlyObjectException, AlreadyLockedObjectException, IllegalPersistentStateRuntimeException {
        if (isQueued()) {
            String str = "The domain object has already been submitted to the UOW for an Add/Update/Delete. No more updates can be performed until after a commit";
            log.error(str);
            throw new IllegalPersistentStateRuntimeException(str);
        }

        if (!m_modified) {
            if (getLocking() == Criteria.LOCKING_READ_ONLY) {
                String str = "Cannot update the instance as it has been marked as ReadOnly";
                log.error(str);
                throw new ReadOnlyObjectException(new Object[]{actualInstance()});
            } else if (!isLocked() && getLocking() != Criteria.LOCKING_OPTIMISTIC && isDatabaseOccurence()) {
                getUOW().acquireLock(actualInstance());
            }
            setModified(true);
        }
    }

    /** Adds an initial value for a field whenever it is modified.
     * This method is typically invoked by the updateXyz() method of the extending class.
     * Note: The value will not be added, if the field has already been modified.
     * @param fieldName the field.
     * @param initialValue the initial value.
     */
    protected void addInitialValue(String fieldName, Object initialValue) {
        if (m_modifiedFields == null)
            m_modifiedFields = new HashMap();

        if (!isModified(fieldName))
            m_modifiedFields.put(fieldName, initialValue);
    }

    /** This method simply returns the 'this' object.
     * A proxy implementation will override this method and return the proxy instance.
     * @return the persistent instance.
     */
    protected IPersistent actualInstance() {
        return this;
    }

    /** Compares this object with another Persistent object.
     * Returns a true if both the objects have the same primary key.
     * @param obj the other Persistent object.
     * @return a true if both the objects have the same primary key.
     */
    public boolean equals(Object obj) {
        boolean result = false;
        if (obj != null && this.getClass() == obj.getClass()) {
            try {
                FieldMetaData[] keyFields = PersistentHelper.getKeyFields(this.getClass().getName());
                if (keyFields != null && keyFields.length > 0) {
                    for (FieldMetaData keyField : keyFields) {
                        String keyFieldName = keyField.getName();
                        Object thisValue = BeanHelper.getField(this, keyFieldName);
                        Object targetValue = BeanHelper.getField(obj, keyFieldName);
                        result = thisValue == null ? targetValue == null : thisValue.equals(targetValue);
                        if (log.isDebugEnabled())
                            log.debug(keyFieldName + ": thisValue=" + thisValue + ", targetValue=" + targetValue + ", comparison=" + result);
                        if (!result)
                            break;
                    }
                    if (log.isDebugEnabled())
                        log.debug("Output=" + result);
                    return result;
                } else {
                    if (log.isDebugEnabled())
                        log.debug("Will invoke super.equals() since key fields cannot be determined for the class '" + this.getClass() + '\'');
                }
            } catch (Exception e) {
                log.warn("Will invoke super.equals() since an error occurred while comparing the key fields", e);
            }
        } else {
            if (log.isDebugEnabled())
                log.debug("Will invoke super.equals() since the " + (obj == null ? "argument is null" : "argument's class '" + obj.getClass() + "' does not match the current class '" + this.getClass() + '\''));
        }
        result = super.equals(obj);
        if (log.isDebugEnabled())
            log.debug("Output from super.equals() is " + result);
        return result;
    }

    /** Returns the hashCode of this object based on it's primary key.
     * @return the hashCode of this object based on it's primary key.
     */
    public int hashCode() {
        //return (getAssetTk() != null ? getAssetTk().hashCode() : 0);
        int result = 0;
        // IStoredProcedure implementations does not have hashCode or key fields, so just return the super.hashCode().
        if(IStoredProcedure.class.isAssignableFrom(this.getClass())){
            return super.hashCode();
        }
        try {
            FieldMetaData[] keyFields = PersistentHelper.getKeyFields(this.getClass().getName());
            if (keyFields != null && keyFields.length > 0) {
                for (FieldMetaData keyField : keyFields) {
                    String keyFieldName = keyField.getName();
                    Object thisValue = BeanHelper.getField(this, keyFieldName);
                    if (thisValue != null)
                        result += thisValue.hashCode();
                }
                if (log.isDebugEnabled())
                    log.debug("Output=" + result);
                return result;
            } else {
                if (log.isDebugEnabled())
                    log.debug("Will invoke super.hashCode() since key fields cannot be determined for the class '" + this.getClass() + '\'');
            }
        } catch (Exception e) {
            log.warn("Will invoke super.hashCode() since an error occurred while evaluating the key fields", e);
        }
        result = super.hashCode();
        if (log.isDebugEnabled())
            log.debug("Output from super.hashCode() is " + result);
        return result;
    }
}
