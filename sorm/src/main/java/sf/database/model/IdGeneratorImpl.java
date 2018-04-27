package sf.database.model;

import javax.persistence.SequenceGenerator;
import javax.persistence.TableGenerator;

public class IdGeneratorImpl implements IdGenerator {
    /**
     * Extract the IdentifierGeneratorDefinition related to the given TableGenerator annotation
     * @param tableGeneratorAnnotation The annotation
     * @param definitionBuilder        The IdentifierGeneratorDefinition builder to which to apply
     *                                 any interpreted/extracted configuration
     */
    public void interpretTableGenerator(TableGenerator tableGeneratorAnnotation) {

    }

    /**
     * Extract the IdentifierGeneratorDefinition related to the given SequenceGenerator annotation
     * @param sequenceGeneratorAnnotation The annotation
     * @param definitionBuilder           The IdentifierGeneratorDefinition builder to which to apply
     *                                    any interpreted/extracted configuration
     */
    public void interpretSequenceGenerator(SequenceGenerator sequenceGeneratorAnnotation) {

    }
}
