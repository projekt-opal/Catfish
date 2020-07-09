package org.dice_research.opal.catfish;

import java.util.LinkedList;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.dice_research.opal.catfish.cleaner.DateFormatEqualizer;
import org.dice_research.opal.catfish.cleaner.EmptyBlankNodeCleaner;
import org.dice_research.opal.catfish.cleaner.FormatCleaner;
import org.dice_research.opal.catfish.cleaner.LiteralCleaner;
import org.dice_research.opal.catfish.cleaner.TitleLanguageFilter;
import org.dice_research.opal.catfish.cleaner.UriRewriter;
import org.dice_research.opal.catfish.config.CleaningConfig;
import org.dice_research.opal.common.interfaces.JenaModelProcessor;
import org.dice_research.opal.common.interfaces.ModelProcessor;

/**
 * OPAL RDF and DCAT cleaning component.
 *
 * @author Adrian Wilke
 */
@SuppressWarnings("deprecation")
public class Catfish implements ModelProcessor, JenaModelProcessor {

	private final CleaningConfig cleaningConfig;
	private String newDatasetUri;

	public Catfish(CleaningConfig cleaningConfig) {
		this.cleaningConfig = cleaningConfig == null ? new CleaningConfig() : cleaningConfig;
	}

	private List<ModelProcessor> getModelProcessors() {
		List<ModelProcessor> modelProcessors = new LinkedList<>();

		if (cleaningConfig.isRemovingNonDeEnTitleDatasets())
			modelProcessors.add(new TitleLanguageFilter());

		if (cleaningConfig.isCleanEmptyBlankNodes())
			modelProcessors.add(new EmptyBlankNodeCleaner());

		if (cleaningConfig.isCleanFormats())
			modelProcessors.add(new FormatCleaner());

		if (cleaningConfig.isCleanLiterals())
			modelProcessors.add(new LiteralCleaner());

		if (cleaningConfig.isEqualizingDateFormats())
			modelProcessors.add(new DateFormatEqualizer());

		if (cleaningConfig.getCatalogIdToReplaceUris() != null) {
			modelProcessors.add(new UriRewriter(cleaningConfig.getCatalogIdToReplaceUris()));
		}

		return modelProcessors;
	}

	/**
	 * @param model      Apache Jena Model that needs to be cleaned
	 * @param datasetUri unused
	 */
	@Override
	public void processModel(Model model, String datasetUri) {
		List<ModelProcessor> modelProcessors = getModelProcessors();
		modelProcessors.forEach(modelProcessor -> {
			try {
				modelProcessor.processModel(model, datasetUri);

				// Provide new dataset URI, if replaced
				if (modelProcessor instanceof UriRewriter) {
					newDatasetUri = ((UriRewriter) modelProcessor).getNewDatasetUri();
				}

			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}

	/**
	 * @deprecated Replaced by {@link #processModel(Model, String)}.
	 */
	@Deprecated
	@Override
	public Model process(Model model, String datasetUri) {
		processModel(model, datasetUri);
		return model;
	}

	/**
	 * Gets the new dataset URI, if URIs are rewritten.
	 * 
	 * If URIs are not rewritten, this returns null.
	 */
	public String getNewDatasetUri() {
		return newDatasetUri;
	}

}