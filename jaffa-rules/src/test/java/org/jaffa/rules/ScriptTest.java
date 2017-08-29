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

package org.jaffa.rules;

import junit.framework.TestCase;
import org.jaffa.config.JaffaRulesConfig;
import org.jaffa.rules.testmodels.ScriptBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author PaulE
 */
public class ScriptTest extends TestCase {

    private ApplicationContext ctx;

    /**
     * Creates new ScriptTest
     *
     * @param name The name of the test case.
     */
    public ScriptTest(String name) {
        super(name);
    }

    /**
     * Runs the test suite.
     *
     * @param args The input args are not used.
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(ScriptTest.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ctx = new AnnotationConfigApplicationContext(JaffaRulesConfig.class);
    }

    public void testInvokeFunctionsOnExternalScript() {
        try {
            ScriptBean obj = new ScriptBean();
            obj.setField1("f1");
            obj.setField2(null);
            obj.setField3(null);
            obj.setField4(null);
            obj.preUpdate(); // this should invoke the scripts to update field1, field2 and field4
            assertEquals("NewField1", obj.getField1());
            assertEquals("NewField2", obj.getField2());
            assertNull(obj.getField3());
            assertEquals("NewField4", obj.getField4());

            obj = new ScriptBean();
            obj.setField1("f1");
            obj.setField2(null);
            obj.setField3(null);
            obj.setField4(null);
            obj.preUpdate(null); // this should invoke the scripts to update field3 and field4
            assertEquals("f1", obj.getField1());
            assertNull(obj.getField2());
            assertEquals("NewField3", obj.getField3());
            assertEquals("NewField4", obj.getField4());

        } catch (Exception e) {
            e.printStackTrace(System.err);
            fail();
        }
    }

}
