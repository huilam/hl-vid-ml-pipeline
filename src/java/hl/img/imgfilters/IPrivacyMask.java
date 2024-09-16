package hl.img.imgfilters;

import java.util.List;

import org.json.JSONObject;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;

public interface IPrivacyMask {
	
	public abstract void setThresholds(JSONObject aThresholdJson);
	public abstract void mask(Mat aImgMat, List<MatOfPoint> aRegionsOfInterest);
	
}