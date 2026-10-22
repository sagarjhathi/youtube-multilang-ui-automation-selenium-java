package main.java.yt_multi_lang_ui_validator.lingua;

import com.github.pemistahl.lingua.api.*; 

import main.java.yt_multi_lang_ui_validator.logger.LoggerUtility;


import org.apache.logging.log4j.Logger;

public class LinguaHelper {
	
	
	  private static final Logger log = LoggerUtility.getLogger(LinguaHelper.class);
	  	  
	  private static final LanguageDetector DETECTOR =
			    LanguageDetectorBuilder.fromLanguages(

			        Language.AFRIKAANS,
			        Language.AZERBAIJANI,
			        Language.INDONESIAN,
			        Language.BOSNIAN,
			        Language.CATALAN,
			        Language.DANISH,
			        Language.GERMAN,
			        Language.ESTONIAN,
			        Language.ENGLISH,
			        Language.SPANISH,
			        Language.BASQUE,
			        Language.FRENCH,
			        Language.CROATIAN,
			        Language.ICELANDIC,
			        Language.ITALIAN,
			        Language.LATVIAN,
			        Language.LITHUANIAN,
			        Language.HUNGARIAN,
			        Language.DUTCH,
			        Language.POLISH,
			        Language.PORTUGUESE,
			        Language.ALBANIAN,
			        Language.SLOVENE,
			        Language.FINNISH,
			        Language.SWEDISH,
			        Language.VIETNAMESE,
			        Language.TURKISH,
			        Language.BELARUSIAN,
			        Language.BULGARIAN,
			        Language.RUSSIAN,
			        Language.UKRAINIAN,
			        Language.GREEK,
			        Language.ARMENIAN,
			        Language.HEBREW,
			        Language.ARABIC,
			        Language.PERSIAN,
			        Language.HINDI,
			        Language.BENGALI,
			        Language.PUNJABI,
			        Language.GUJARATI,
			        Language.TAMIL,
			        Language.TELUGU,
			        Language.CHINESE,
			        Language.JAPANESE,
			        Language.KOREAN

			    ).build();


	    private LinguaHelper() {
	        // Prevent instantiation
	    }

	    /**
	     * Detect the language of the given text.
	     * Returns "Unknown" if detection fails or text is blank.
	     */



	   
    
	    public static String detectLanguage(String text) {
	        try {
	            if (text == null || text.trim().isEmpty()) {
	                log.debug("detectLanguage called with empty text.");
	                return "Unknown";
	            }

	            String cleanText = text.trim();

	            // ---------- First attempt ----------
	            Language detected = DETECTOR.detectLanguageOf(cleanText);

	            // ---------- Retry if Unknown ----------
	            if (detected == null) {
	                log.debug("First detection failed. Retrying with sanitized text...");

	                // remove numbers/symbols for second attempt
	                String sanitized = cleanText.replaceAll("[^\\p{L}\\s]", "");

	                detected = DETECTOR.detectLanguageOf(sanitized);
	            }

	            if (detected == null) {
	                log.debug("Language still not detected after retry: {}", cleanText);
	                return "Unknown";
	            }

	            log.info("\n========== LANGUAGE DETECTION ==========\n" +
	                     "Detected Language : {}\n" +
	                     "Input Text        : {}\n" +
	                     "========================================",
	                     detected.name(), cleanText);

	            System.out.println(detected.name()+"    Detected.name();");
	            return detected.name();

	        } catch (Exception e) {
	            log.error("Language detection failed: {}", e.getMessage());
	            return "Unknown, error msg is " + e.getMessage();
	        }
	    }
	    
	    
	    
}
