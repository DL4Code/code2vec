package edu.lu.uni;

public class Configuration {
	/**
	 * Configuration of the first step: tokenization.
	 */
//	public static final List<String> PROJECTS = new ArrayList<>();  // input
//	public static final List<TokenType> TOKENTYPES = new ArrayList<>();
//	static {
//		PROJECTS.add("../commons-math/.git");
//		PROJECTS.add("../aries/.git");
//		PROJECTS.add("../derby/.git");
//		PROJECTS.add("../mahout/.git");
//		PROJECTS.add("../lucene-solr/.git");
//		PROJECTS.add("../cassandra/.git");
//		
//		TOKENTYPES.add(TokenType.COMBINED_AST_NODE_NAME_AND_RAW_TOKEN);
//		TOKENTYPES.add(TokenType.ONLY_AST_NODE_NAME);
//		TOKENTYPES.add(TokenType.ONLY_RAW_TOKEN);
//		TOKENTYPES.add(TokenType.SEPRATED_AST_NODE_NAME_AND_RAW_TOKEN);
//		TOKENTYPES.add(TokenType.STATEMENT_NODE_NAME_WITH_RAW_TOKEN);
//	}
	public static final String TOKENIZATION_OUTPUT_PATH = "OUTPUT/tokenization/"; // output
	public static final String TOKEN_FILE_EXTENSION = ".list";
	public static final String SIZE_FILE_EXTENSION = ".csv";
	
	
	/**
	 * Configuration of the second step: parsing method names.
	 */
	public static final String TOKEN_FILE_PATH = "OUTPUT/tokenization/tokens/";       // input
	public static final String METHOD_NAME_TOKEN_PATH = "OUTPUT/parse_method_name/";  // output
	
	/**
	 * Configuration of the third step: token encoding / embedding.
	 */
	public static final String SIZE_FILE_PATH = "OUTPUT/tokenization/sizes/";                   // input
//	public static final MaxSizeType maxSizeType = MaxSizeType.ThirdQuarter;
	public static final String ENCODED_METHOD_BODY_FILE_PATH = "OUTPUT/encoding/encoded_method_bodies/";  // output
	public static final String ENCODED_METHOD_NAME_FILE_PATH = "OUTPUT/encoding/encoded_method_name/";    // output
	public static final String SELECTED_METHOD_BODY_PATH = "OUTPUT/encoding/selected_method_body/";  // output
	public static final String SELECTED_METHOD_NAME_PATH = "OUTPUT/encoding/selected_method_name/";  // output
	public static final String SELECTED_METHOD_PATH = "OUTPUT/encoding/selected_source_code/";       // output
	
	public static final String EMBEDDING_TOKENS_FILE_PATH = "OUTPUT/embedding/data_preprocess/";
	public static final int SIZE_OF_VECTOR = 300;
	public static final int MIN_WORD_FREQUENCY = 1;
	public static final String EMBEDDED_TOKENS_FILE_PATH = "OUTPUT/embedding/token_map/";
	public static final String EMBEDDED_TOKENS_VECTOR_FILE_PATH = "OUTPUT/embedding/vectors/";
	
	/**
	 * Configuration of the forth step: feature extracting by deep learning with the CNN algorithm.
	 */
	public static final String DATA_APPZENDED_ZERO = "OUTPUT/data_preprocess/append_zero/";     // file path of output
	public static final String DATA_STANDARDIZED = "OUTPUT/data_preprocess/standardized_data/"; // file path of output
	public static final String DATA_EXTRACTED_FEATURE = "OUTPUT/CNN_extracted_feature/";        // file path of output
}
