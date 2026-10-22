package main.java.yt_multi_lang_ui_validator.utilities;

import java.io.File;
import java.io.IOException;

import net.coobird.thumbnailator.Thumbnails;

public class ImageCompressor {

	public static void compressImage(File src , String outPutPath, double outPutQuality) {
		
		try {
			Thumbnails.of(src)
			.scale(1)
			.outputQuality(outPutQuality)
			.toFile(new File(outPutPath));
		} catch (IOException e) {
			System.err.println("Failed to compress image " + src + ": " + e.getMessage());
			e.printStackTrace();
		}
		
	}
}
