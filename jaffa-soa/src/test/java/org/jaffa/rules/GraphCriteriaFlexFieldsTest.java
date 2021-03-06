package org.jaffa.rules;

import junit.framework.TestCase;
import org.jaffa.beans.factory.config.StaticContext;
import org.jaffa.components.finder.StringCriteriaField;
import org.jaffa.exceptions.ApplicationExceptions;
import org.jaffa.exceptions.FrameworkException;
import org.jaffa.loader.CoreLoaderConfig;
import org.jaffa.persistence.Criteria;
import org.jaffa.rules.testmodels.UserCriteria;
import org.jaffa.rules.testmodels.UserCriteriaNoFlex;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.ArrayList;
import java.util.List;

public class GraphCriteriaFlexFieldsTest extends TestCase {

    private static AnnotationConfigApplicationContext resourceLoaderConfig = new AnnotationConfigApplicationContext(CoreLoaderConfig.class);

    /**
     * Creates new GraphCriteriaFlexFieldsTest
     *
     * @param name The name of the test case.
     */
    public GraphCriteriaFlexFieldsTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        TestHelper.setupRepos();
    }

    public void testNoFlexFields() throws FrameworkException, ApplicationExceptions {
        UserCriteriaNoFlex userCriteriaNoFlex = new UserCriteriaNoFlex();

        StaticContext.initialize(userCriteriaNoFlex);

        assertNull(userCriteriaNoFlex.getFlexCriteriaBean());
    }

    public void testHasFlexFields() throws FrameworkException, ApplicationExceptions {
        UserCriteria userCriteria = new UserCriteria();
        StaticContext.initialize(userCriteria);
        assertNotNull(userCriteria.getFlexCriteriaBean());
    }

    public void testCriteriaClauseDomainMapped() throws FrameworkException, ApplicationExceptions {
        UserCriteria userCriteria = new UserCriteria();
        StaticContext.initialize(userCriteria);
        assertNotNull(userCriteria.getFlexCriteriaBean());

        Criteria c = userCriteria.buildQueryCriteria();

        assertNull(c.getCriteriaEntries());

        userCriteria.getFlexCriteriaBean().set("remarks", new StringCriteriaField("Equals", "nice guy"));

        c = userCriteria.buildQueryCriteria();
        assertNotNull(c.getCriteriaEntries());
        assertEquals(1, c.getCriteriaEntries().size());

        List<Criteria.CriteriaEntry> criteriaEntries = new ArrayList<>(c.getCriteriaEntries());
        assertEquals("nice guy", criteriaEntries.get(0).getValue());
        assertEquals("UserRef15", criteriaEntries.get(0).getName());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        TestHelper.shutdownRepos();
    }
}

