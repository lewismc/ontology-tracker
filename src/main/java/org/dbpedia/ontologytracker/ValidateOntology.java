package org.dbpedia.ontologytracker;

import com.google.gson.Gson;
import org.aksw.rdfunit.model.interfaces.results.ShaclTestCaseResult;
import org.aksw.rdfunit.model.interfaces.results.TestCaseResult;
import org.aksw.rdfunit.model.interfaces.results.TestExecution;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ValidateOntology {
    private static long startTime = System.currentTimeMillis();

    private static Logger L = Logger.getLogger(ValidateOntology.class);


    //private static final String DBO_MANUAL_TESTS = "/org/aksw/rdfunit/tests/Manual/dbpedia.org/ontology/dbo.tests.Manual.ttl";
    private static final String DBPEDIA_ONTOLOGY = "http://rawgit.com/gcpdev/ontology-tracker/master/ontology/dbpedia_2016-10.ttl";
    private static OntModel model = null;
    private static String outdir = "/home/gpublio/ontology-tracker/";

    private static List<DBpediaTest> tests = new ArrayList<>();

    private static void readOntology(File file) throws IOException {

        String baseUri = file.getParentFile().getCanonicalFile().toURI() + file.getName() + "#";
        model = ModelFactory.createOntologyModel();
        //Metadata m = new Metadata(file, baseUri, model);

        try {
            RDFDataMgr.read(model, file.toURI().toString(), baseUri, Lang.TURTLE);
        } catch (Exception e) {
            L.error(e.getMessage());
            //m.issues.add( Issue.create("ERROR", "Error when parsing " + m.reponame + "/" + m.nicename + "/metadata.ttl, skipping",L,e));
            //return m;
        }

    }

    private static Collection<TestCaseResult> runTests(RDFUnitValidate rval) {

        TestExecution te = rval.checkModelWithRdfUnit(model);
        Collection<TestCaseResult> tcrs = te.getTestCaseResults();
        tcrs.forEach(tcr ->
                tests.add(DBpediaTest.create(tcr.getSeverity().name(), tcr.getMessage()+" "+((ShaclTestCaseResult)tcr).getFailingResource(),L,null)));
        return tcrs;
    }

    public static void main(String[] args) throws IOException {

        RDFUnitValidate rval = new RDFUnitValidate();
        File file = new File(DBPEDIA_ONTOLOGY);
        readOntology(file);
        Collection<TestCaseResult> tcrs = runTests(rval);

        tests.stream().forEach((DBpediaTest t) -> {
            t.prepareJSON(model);
        });

        FileWriter fw = new FileWriter(outdir + File.separator + "data.json");
        new Gson().toJson(tests, fw);
        //new Gson().toJson(tcrs, fw);

        fw.close();
        L.info("wrote json to " + outdir + File.separator + "data.json");
        long endTime   = System.currentTimeMillis();
        long totalTime = (endTime - startTime)/1000;
        L.info("Program execution ended. Total execution time of Java code: " + totalTime);


    }

}