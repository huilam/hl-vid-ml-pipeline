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
	public void setThresholds(JSONObject aThresholdJson) {
		int iRed 	= aThresholdJson.optInt("Red",	0);
		int iGreen 	= aThresholdJson.optInt("Green",0);
		int iBlue 	= aThresholdJson.optInt("Blue", 0);
		int iAlpha 	= aThresholdJson.optInt("Alpha",0);
		
		//Blue, Green, Red, Alpha
		this.fill_color = new Scalar(iGreen, iBlue, iRed, iAlpha);
	}
}