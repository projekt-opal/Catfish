package org.dice_research.opal.catfish.service;

import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.DCTerms;
import org.dice_research.opal.catfish.service.impl.EmptyBlankNodeCleaner;
import org.dice_research.opal.test_cases.OpalTestCases;
import org.dice_research.opal.test_cases.TestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link EmptyBlankNodeCleaner}.
 *
 * @author Adrian Wilke
 */
public class EmptyBlankNodeCleanerTest {

    /**
     * Test model contains empty literals (with language tags).
     */
    TestCase testCaseA;

    /**
     * Two distributions have property DCTerms.rights with empty blank nodes as
     * objects.
     */
    TestCase testCaseB;

    @Before
    public void setUp() throws Exception {
        testCaseA = OpalTestCases.getTestCase("edp-2019-12-17", "med-kodierungshandbuch");
        testCaseB = OpalTestCases.getTestCase("edp-2019-12-17", "mittenwalde");
    }

    Model getModelCopy(TestCase testCase) {
        Model model = ModelFactory.createDefaultModel();
        model.add(testCase.getModel());
        return model;
    }

    /**
     * Tests, if empty blank nodes are removed.
     */
    @Test
    public void testEmptyBlankNodes() {
        String distWithEbnUri = "https://europeandataportal.eu/set/distribution/1007b442-bf33-407f-ab74-534ca67d79e3";
        Model testModel;

        testModel = getModelCopy(testCaseB);
        new EmptyBlankNodeCleaner().clean(testModel);
        Assert.assertTrue("EmptyBlankNodeCleaner removes empty blank node", testModel.size() < testCaseB.getModel().size());

        StmtIterator stmtIterator = testCaseB.getModel().getResource(distWithEbnUri).listProperties(DCTerms.rights);
        Assert.assertTrue("Empty blank node in source", stmtIterator.hasNext());

        stmtIterator = testModel.getResource(distWithEbnUri).listProperties(DCTerms.rights);
        Assert.assertFalse("No empty blank node", stmtIterator.hasNext());

        // Manual checks
        if (Boolean.FALSE) {
            System.out.println("EBN: Source " + testCaseB.getModel().size() + ", cleaned " + testModel.size());
        }
        if (Boolean.FALSE) {
            testCaseB.getModel().write(System.out, "TURTLE");
            System.out.println("---");
            testModel.write(System.out, "TURTLE");
        }
    }

}