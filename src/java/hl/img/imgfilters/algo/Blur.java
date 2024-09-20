package hl.img.imgfilters.algo;

import org.json.JSONObject;
import org.opencv.core.Mat;
import hl.img.imgfilters.BasePrivacyMask;
import hl.img.imgfilters.IPrivacyMask;
import hl.opencv.util.OpenCvFilters;

public class Blur extends BasePrivacyMask implements IPrivacyMask{
	
	enum MODE
	{
		NORMAL_BLUR
		, MEDIAN_BLUR
		, GAUSSIAN_BLUR
	}
	
	private static double DEF_PRIVACY_THRESHOLD = 0.80;
	private static MODE   DEF_PRIVACY_MODE 		= MODE.NORMAL_BLUR;
	
	private double privacy_threshold 			= DEF_PRIVACY_THRESHOLD;
	private MODE privacy_blur_mode 				= DEF_PRIVACY_MODE;

	@Override
	protected void applyFilter(Mat aImgMat)
	{
		switch(this.privacy_blur_mode)
		{
			case MEDIAN_BLUR:;
				OpenCvFilters.medianBlur(aImgMat, this.privacy_threshold);
				break;
			case GAUSSIAN_BLUR:
			OpenCvFilters.gaussianBlur(aImgMat, this.privacy_threshold);	
				break;
			default:
				OpenCvFilters.blur(aImgMat, this.privacy_threshold);
		}
	}
	
	@Override
	public void setJsonConfig(JSONObject aJsonConfig) {
		//
		this.privacy_threshold = aJsonConfig.optDouble(
				IPrivacyMask.PRIVMASK_ALGO_THRESHOLD, 
				DEF_PRIVACY_THRESHOLD);
		//
		String sPrivacyBlurMode = aJsonConfig.optString(
				PRIVMASK_ALGO_MODE, null);
		switch(sPrivacyBlurMode.toLowerCase())
		{
			case "median":;
				this.privacy_blur_mode = MODE.MEDIAN_BLUR;
				break;
			case "gaussian":
				this.privacy_blur_mode = MODE.GAUSSIAN_BLUR;
				break;
			default:
				this.privacy_blur_mode = MODE.NORMAL_BLUR;
		}
		//
	}
}