package at.fh.BPMN20OntologyTester.controller;

import at.fh.BPMN20OntologyTester.BPMN20OnologyTesterTest;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class OntologyHandlerTest extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public OntologyHandlerTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( BPMN20OnologyTesterTest.class );
    }

    
    public void testApp()
    {
        assertTrue( true );
    }
}