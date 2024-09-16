package hl.img.imgfilters;

import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class BasePrivacyMask {
	
	protected void applyFilter(Mat aImgMat)
	{
	}
	
	public void mask(Mat aImgMat, List<MatOfPoint> aRegionsOfInterest)
	{
		Mat matMaskedImg 	= null;
		Mat matMask 		= null;
		
		try {
			//Img with Privacy 
			matMaskedImg = aImgMat.clone();
			applyFilter(matMaskedImg);
			
			//Create Mask for region of interest
			matMask = new Mat(aImgMat.size(), CvType.CV_8UC1, Scalar.all(0));
			Imgproc.fillPoly(matMask, aRegionsOfInterest, new Scalar(255));
			
			//Copy privacy masked regions to original
			Core.copyTo(matMaskedImg, aImgMat, matMask);
			
		}
		finally
		{
			if(matMask!=null)
				matMask.release();
			
			if(matMaskedImg!=null)
				matMaskedImg.release();
		}
		
	}
}