package hl.img.imgfilters;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect2d;

public class PrivacyMaskUtil {
	
	public static IPrivacyMask getPrivacyMaskInstance(String aImgFilterClassName)
	{
		try {
			//
			Class<?> classDetector = Class.forName(aImgFilterClassName);
	    	Constructor<?> constructor = classDetector.getDeclaredConstructor();
			return (IPrivacyMask) constructor.newInstance();
			//
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static List<MatOfPoint> rectToMatOfPoint(List<Rect2d> aListRectOfInterest)
	{
		Rect2d[] rects = aListRectOfInterest.toArray(new Rect2d[aListRectOfInterest.size()]);
		return rectToMatOfPoint(rects);
	}
	
	public static List<MatOfPoint> rectToMatOfPoint(Rect2d[] aRectOfInterest)
	{
		List<MatOfPoint> listRegionsOfInterest = new ArrayList<MatOfPoint>();
		
		for(Rect2d r : aRectOfInterest)
		{	
			MatOfPoint matOfPoint = new MatOfPoint();
			
			matOfPoint.fromArray(
					new Point(r.x			, r.y), //top left
					new Point(r.x+r.width	, r.y), //top right
					new Point(r.x+r.width	, r.y+r.height), //bottom right
					new Point(r.x			, r.y+r.height)); //bottom left
			
			listRegionsOfInterest.add(matOfPoint);
		}
		
		return listRegionsOfInterest;
	}
	
}