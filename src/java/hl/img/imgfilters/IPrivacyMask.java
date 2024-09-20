package hl.img.imgfilters;

import java.util.List;

import org.json.JSONObject;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;

public interface IPrivacyMask {
	
	public static String PRIVMASK_ALGO_MODE 		= "privmask_algo_mode";
	public static String PRIVMASK_ALGO_THRESHOLD 	= "privmask_algo_threshold";
	
	public abstract void setJsonConfig(JSONObject aJsonConfig);
	public abstract void mask(Mat aImgMat, List<MatOfPoint> aRegionsOfInterest);
	
}