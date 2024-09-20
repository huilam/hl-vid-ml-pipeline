package hl.img.imgfilters.algo;

import org.json.JSONObject;
import org.opencv.core.Mat;
import hl.img.imgfilters.BasePrivacyMask;
import hl.img.imgfilters.IPrivacyMask;
import hl.opencv.util.OpenCvFilters;

public class Pixelate extends BasePrivacyMask implements IPrivacyMask{
	
	private static double DEF_PRIVACY_THRESHOLD = 0.80;
	private double privacy_threshold 			= DEF_PRIVACY_THRESHOLD;

	@Override
	protected void applyFilter(Mat aImgMat)
	{
		OpenCvFilters.pixelate(aImgMat, this.privacy_threshold);
	}
	
	@Override
	public void setJsonConfig(JSONObject aJsonConfig) {
		
		this.privacy_threshold = aJsonConfig.optDouble(
				IPrivacyMask.PRIVMASK_ALGO_THRESHOLD, 
				DEF_PRIVACY_THRESHOLD);
		
	}
}