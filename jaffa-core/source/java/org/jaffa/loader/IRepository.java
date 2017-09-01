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

package org.jaffa.loader;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interface for storing values in Repository.
 */
public interface IRepository<T> {

    /**
     * registers with given key and value in to the repository with the context provided.
     * assumes "default" when context is not provided.
     * @param repositoryKey which is the key of repository.
     * @param repositoryElement to be stored in repository.
     */
    void register(ContextKey repositoryKey, T repositoryElement);

    /**
     * unregisters the given key from the repository with the context provided.
     * assumes "default" when the context is not provided.
     * @param repositoryKey is the key of repository.
     */
    void unregister(ContextKey repositoryKey);

    /**
     * queries repository with the given key and returns repositoryElement in the natural contextOrder
     * @param repositoryKey is the key of repository.
     * @return repository value
     */
    T query(ContextKey repositoryKey);

    /**
     * queries repository with the given key and returns repositoryElement in the natural contextOrder
     * @param id is the key of ContextKey.
     * @return repository value
     */
    T query(String id);

    /**
     * queries repository with the given key and returns contextKey in the natural contextOrder
     * @param id is the key of ContextKey.
     * @return contextKey
     */
    ContextKey findKey(String id);

    /**
     * retrieves all the keys in the repository
     * @return Set of all keys
     */
    Set<ContextKey> getAllKeys();

    /**
     * retrieves all the key ids in the repository
     * @return Set of all keys
     */
    Set<String> getAllKeyIds();

    /**
     * retrives all the values in the repository based on natural contextOrder
     * @return List of all values
     */
    List<T> getAllValues();

    /**
     * Returns the MapRepository for based on the context of User
     * @return Map
     */
    Map<String, T> getMyRepository();

    /**
     * Returns the repository associated with the variation
     * @param variation
     * @return
     */
    public Map<String, T> getRepositoryByVariation(String variation);


    /**
     * Returns Repository value for the passed Id in the context of variation
     * @param id
     * @param variation
     * @return
     */
    public T queryByVariation(String id, String variation);
}
