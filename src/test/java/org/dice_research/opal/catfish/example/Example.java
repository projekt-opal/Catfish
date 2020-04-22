package org.dice_research.opal.catfish.example;

import java.io.File;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.dice_research.opal.catfish.Catfish;
import org.dice_research.opal.catfish.config.CleaningConfig;
import org.dice_research.opal.common.utilities.FileHandler;
import org.dice_research.opal.common.vocabulary.Opal;

public class Example {

	/**
	 * Cleans data.
	 * 
	 * @param turtleInputFile  A TURTLE file to read
	 * @param turtleOutputFile A TURTLE file to write results
	 * @param datasetUri       A URI of a dcat:Dataset inside the TURTLE data
	 * 
	 * "@see <a href="https://www.w3.org/TR/turtle/"> turtle </a>
	 * "@see <a href="https://www.w3.org/TR/vocab-dcat/"> dcat vocabulary </a>
	 */
	public void cleanMetadata(File turtleInputFile, File turtleOutputFile, String datasetUri) throws Exception {

		// Load TURTLE file into model
		Model model = FileHandler.importModel(turtleInputFile);

		CleaningConfig cleaningConfig = new CleaningConfig();
		// Remove blank nodes, which are not subject of triples
		// (optional method call, default: true)
		cleaningConfig.setCleanEmptyBlankNodes(true);

		// Remove triples with literals as object, which contain no value or unreadable.
		// And also extract Language Tag and DataType if it is mistakenly inside the string
		// (optional method call, default: true)
		cleaningConfig.setCleanLiterals(true);

		// Check dct:format and dcat:mediaType for values and create new triples.
		// (optional method call, default: true)
		cleaningConfig.setCleanFormats(true);

		Catfish catfish = new Catfish(cleaningConfig);

		// Update model
		catfish.processModel(model, datasetUri);

		// Example for requesting formats
		printFormats(model, datasetUri);

		// Write updated model into TURTLE file
		FileHandler.export(turtleOutputFile, model);
	}

/**
 * Example for requesting formats.
 * 
 * Generated formats are of type http://projekt-opal.de/Format.
 */
public void printFormats(Model model, String datasetUri) {

	// Go through Distributions of current Dataset
	StmtIterator distributionIterator = model.getResource(datasetUri).listProperties(DCAT.distribution);
	while (distributionIterator.hasNext()) {
		RDFNode rdfNode = distributionIterator.next().getObject();
		if (rdfNode.isResource()) {
			Resource distribution = rdfNode.asResource();

			// Get formats of current Distribution
			StmtIterator formatIterator = distribution.listProperties(DCTerms.format);
			while (formatIterator.hasNext()) {
				RDFNode format = formatIterator.next().getObject();

				// Check if type is http://projekt-opal.de/Format
				if (format.isResource()) {
					Statement statement = format.asResource().getProperty(RDF.type);
					if (statement != null
							&& statement.getObject().asResource().getURI().equals(Opal.OPAL_FORMAT.getURI())) {

						// Prints, e.g.
						// http://projekt-opal.de/format/pdf
						// http://projekt-opal.de/format/html
						System.out.println(format);
					}
				}
			}
		}
	}
}

}