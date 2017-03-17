package edu.lu.uni;

public class Configuration {
	/**
	 * Configuration of the first step: tokenize and vectorize the Java source code.
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
	public static final String TOKENIZATION_OUTPUT_PATH = "OUTPUT/tokenization/"; // the file path of output
	public static final String STRING_DATA_FILE_EXTENSION = ".list";  // the file extension of string data.
	public static final String DIGITAL_DATA_FILE_EXTENSION = ".csv";  // the file extension of digital data.
	
	
	/**
	 * Configuration of the second step: parse method names.
	 */
	public static final String TOKEN_FILE_PATH = TOKENIZATION_OUTPUT_PATH + "tokens/";    // the file path of input
	public static final String METHOD_NAME_TOKEN_PATH = "OUTPUT/parsed_method_name/";     // the file path of output


	/**
	 * Configuration of the third step: encode/embed tokens of method bodies.
	 */
	public static final String SIZE_FILE_PATH = TOKENIZATION_OUTPUT_PATH + "sizes/";      // input
	/*
	 * the max size will be used to select methods.
	 * The methods, of which the size of token vectors is less than or equals to the max size, will be selected as latter data.
	 */
//	public static final MaxSizeType maxSizeType = MaxSizeType.ThirdQuarter;  
	public static final String ENCODED_METHOD_BODY_FILE_PATH = "OUTPUT/encoding/encoded_method_bodies/";  // output
	@Deprecated public static final String ENCODED_METHOD_NAME_FILE_PATH = "OUTPUT/encoding/encoded_method_name/";    // output
	public static final String SELECTED_METHOD_BODY_PATH = "OUTPUT/encoding/selected_method_body/";       // output
	public static final String SELECTED_METHOD_NAME_PATH = "OUTPUT/encoding/selected_method_name/";       // output
	public static final String SELECTED_SOURCE_CODE_PATH = "OUTPUT/encoding/selected_source_code/";       // output
	public static final String COMBINED_TOKENS_PATH = "OUTPUT/encoding/combined_token_of_method_name/";   // output
	
	// token embedding with word2vec
	public static final String EMBEDDING_TOKENS_FILE_PATH = "OUTPUT/embedding/data_preprocess/";
	public static final int SIZE_OF_VECTOR = 100;
	public static final int MIN_WORD_FREQUENCY = 1;
	public static final String EMBEDDED_TOKENS_FILE_PATH = "OUTPUT/embedding/token_map/";
	public static final String EMBEDDED_TOKENS_VECTOR_FILE_PATH = "OUTPUT/embedding/vectors/";
	
	/**
	 * Configuration of the forth step: extract features of method bodies by deep learning with the CNN algorithm.
	 */
	public static final String DATA_APPZENDED_ZERO = "OUTPUT/data_preprocess/append_zero/";     // file path of output
	public static final String DATA_STANDARDIZED = "OUTPUT/data_preprocess/standardized_data/"; // file path of output
	public static final String DATA_EXTRACTED_FEATURE = "OUTPUT/CNN_extracted_feature/";        // file path of output
}
