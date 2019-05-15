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

package org.jaffa.rules.variation;

import org.jaffa.rules.commons.MetaData;

import java.util.regex.Pattern;

/**
 * Defines a URI MetaData, instances of which are added to the VariationRepository.
 * <p/>
 * It has the following properties:
 * <ul>regex: Mandatory. A regular expression to match the URI of MetaData files.
 * <ul>variation: Mandatory. A comma-separated list of variations.
 * <ul>Check {@link MetaData} for more properties
 */
public class UriMetaData extends MetaData {

    private String m_regex;
    private String m_variation;
    private Pattern m_pattern;

    /**
     * Getter for property regex.
     *
     * @return Value of property regex.
     */
    public String getRegex() {
        return m_regex;
    }

    /**
     * Setter for property regex.
     *
     * @param regex Value of property regex.
     */
    public void setRegex(String regex) {
        m_regex = regex;
        updatePattern();
    }

    /**
     * Get the compiled pattern
     *
     * @return the compiled pattern, based on the regex property
     */
    public Pattern getPattern() {
        return m_pattern;
    }

    /**
     * Getter for property variation.
     *
     * @return Value of property variation.
     */
    public String getVariation() {
        return m_variation;
    }

    /**
     * Setter for property variation.
     *
     * @param variation Value of property variation.
     */
    public void setVariation(String variation) {
        m_variation = variation;
    }

    /**
     * Returns debug info.
     *
     * @return debug info.
     */
    public String toString() {
        StringBuilder buf = new StringBuilder("<uri");
        if (getRegex() != null)
            buf.append(" regex='").append(getRegex()).append('\'');
        if (getVariation() != null)
            buf.append(" variation='").append(getVariation()).append('\'');
        buf.append(super.toString());
        buf.append("/>");
        return buf.toString();
    }

    /**
     * Update the compiled pattern
     */
    private void updatePattern() {
        if (m_regex == null) {
            m_pattern = null;
        }
        else {
            m_pattern = Pattern.compile(m_regex);
        }
    }
}
