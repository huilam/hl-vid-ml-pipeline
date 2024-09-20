package hl.img.imgfilters.algo;

import org.json.JSONObject;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import hl.img.imgfilters.BasePrivacyMask;
import hl.img.imgfilters.IPrivacyMask;
import hl.opencv.util.OpenCvFilters;

public class SolidFill extends BasePrivacyMask implements IPrivacyMask{
	
	private static Scalar DEF_COLOR = new Scalar(0,0,0,0);
	private Scalar fill_color 		= DEF_COLOR;

	@Override
	protected void applyFilter(Mat aImgMat)
	{
		OpenCvFilters.solidfill(aImgMat, this.fill_color);
	}
	
	
	@Override
	public void setJsonConfig(JSONObject aJsonConfig) {
		
		int iRed 	= aJsonConfig.optInt("red",	0);
		int iGreen 	= aJsonConfig.optInt("green",0);
		int iBlue 	= aJsonConfig.optInt("blue", 0);
		int iAlpha 	= aJsonConfig.optInt("alpha",0);
		
		//Blue, Green, Red, Alpha
		this.fill_color = new Scalar(iGreen, iBlue, iRed, iAlpha);
		
	}
}