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
 * 1.   Redistributions of source code must retain copyright statements and notices.
 *         Redistributions must also contain a copy of this document.
 * 2.   Redistributions in binary form must reproduce the above copyright notice,
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
 * 3.   The name "JAFFA" must not be used to endorse or promote products derived from
 *  this Software without prior written permission. For written permission,
 *  please contact mail to: jaffagroup@yahoo.com.
 * 4.   Products derived from this Software may not be called "JAFFA" nor may "JAFFA"
 *  appear in their names without prior written permission.
 * 5.   Due credit should be given to the JAFFA Project (http://jaffa.sourceforge.net).
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

package org.jaffa.loader.policy;

import org.jaffa.loader.ContextKey;
import org.jaffa.loader.CoreLoaderConfig;
import org.jaffa.security.VariationContext;
import org.jaffa.security.securityrolesdomain.GrantFunctionAccess;
import org.jaffa.security.securityrolesdomain.Role;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * RoleXmlLoadTest - Verifies that we can load a roles.xml file into the repository and use those roles to
 * verify to create, read, update, and delete roles from the repository.
 */
public class RoleXmlLoadTest {

    private static AnnotationConfigApplicationContext xmlLoaderConfig = new AnnotationConfigApplicationContext(CoreLoaderConfig.class);

    /**
     * Verifies that we can load the roles from the roles.xml file into our role repository.
     */
    @Test
    public void testXmlLoad() {
        RoleManager roleManager = xmlLoaderConfig.getBean(RoleManager.class);
        assertNull(roleManager.getRole("CONTRACTOR"));
        assertNotNull(roleManager.getRole("CLERK"));
        assertNotNull(roleManager.getRole("SUPERVISOR"));
        assertEquals(3,roleManager.getRoles().getRole().size());
        assertNull(roleManager.getRole(new String()));
    }

    /**
     * Verifies that we can register and unregsiter specific roles into the role repository.
     */
    @Test
    public void testRoleRegistration() {
        RoleManager roleManager = xmlLoaderConfig.getBean(RoleManager.class);
        assertNull(roleManager.getRole("CONTRACTOR"));
        Role contractorRole = new Role();
        GrantFunctionAccess grantFunctionAccess = new GrantFunctionAccess();
        grantFunctionAccess.setName("Function 1");
        contractorRole.getGrantFunctionAccess().add(grantFunctionAccess);
        ContextKey key = new ContextKey("CONTRACTOR", "roles.xml", VariationContext.NULL_VARIATION, "100-Highest");
        roleManager.registerRole(key, contractorRole);
        assertNotNull(roleManager.getRole("CONTRACTOR"));
        roleManager.unregisterRole(key);
        assertNull(roleManager.getRole("CONTRACTOR"));
    }

    /**
     * Tests the ability of this IManager to retrieve a repository when given its String name
     */
    @Test
    public void testGetRepositoryByName() throws Exception {
        RoleManager roleManager = xmlLoaderConfig.getBean(RoleManager.class);

        String repo = "Role";
        assertEquals(repo, roleManager.getRepositoryByName(repo).getName());
    }

    /**
     * Test the retrieval of the list of repositories managed by this class
     */
    @Test
    public void testGetRepositoryNames() {
        RoleManager roleManager = xmlLoaderConfig.getBean(RoleManager.class);
        for (Object repositoryName : roleManager.getRepositoryNames()) {
            assertEquals("Role", roleManager.getRepositoryByName((String)repositoryName).getName());
        }
    }
}
