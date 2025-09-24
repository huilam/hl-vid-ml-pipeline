/*
 Copyright (c) 2024 onghuilam@gmail.com
 
 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:
 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.
 The Software shall be used for Good, not Evil.
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 
 */

package hl.vid.pipeline.processor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect2d;

import hl.img.imgfilters.IPrivacyMask;
import hl.img.imgfilters.PrivacyMaskUtil;
import hl.objml2.common.DetectedObj;
import hl.objml2.common.DetectedObjUtil;
import hl.objml2.common.FrameDetectedObj;
import hl.objml2.plugin.MLPluginFrameOutput;
import hl.objml2.plugin.ObjDetBasePlugin;
import hl.opencv.video.plugins.VideoFileReEncodingPlugin;


public class VideoObjDetectionPlugin extends VideoFileReEncodingPlugin {

	private IPrivacyMask privacymask 			= null;
	private ObjDetBasePlugin[] detectors 		= null;
	private FrameDetectedObj prevObjs			= null;
	private JSONObject jsonDetections 			= new JSONObject();
	private List<String> objClassOfInterest 	= new ArrayList<String>();
	//
	public VideoObjDetectionPlugin(ObjDetBasePlugin[] aDetectors, File aOutputVidFolder)
	{
		for(ObjDetBasePlugin d : aDetectors)
		{
			d.isPluginOK();
		}
		this.detectors = aDetectors;
		setOutputFolder(aOutputVidFolder);
		setQuietMode(true);
	}
	
	public void setPrivacyMaskAlgo(IPrivacyMask aPrivacyMaskAlgo)
	{
		this.privacymask = aPrivacyMaskAlgo;
	}
	
	public void addObjOfInterest(String[] aObjClassesOfInterest)
	{
		if(this.detectors!=null)
		{
			for(ObjDetBasePlugin detector : this.detectors)
			{
				detector.addObjClassOfInterest(aObjClassesOfInterest);
			}
		}
		else if (aObjClassesOfInterest.length>0)
		{
			this.objClassOfInterest.addAll(Arrays.asList(aObjClassesOfInterest));
		}
	}
	
	private void initDetectors()
	{
		System.out.println("initDetectors();");
		if(detectors!=null)
		{
			//
			if(this.objClassOfInterest.size()>0)
			{
				String[] objClasses = (String[]) objClassOfInterest.toArray();
				this.objClassOfInterest.clear();
				for(ObjDetBasePlugin detector : this.detectors)
				{
					detector.addObjClassOfInterest(objClasses);
				}
			}
		}
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Mat decodedVideoFrame(String aVideoSourceName, Mat matFrame, long aCurFrameNo, long aCurFrameMs,
			double aProgressPercentage) {
		
		if(detectors!=null)
		{
			if(aCurFrameNo==0)
				initDetectors();
			
			Map<String, Map<String, Object>> mapDetectorResult = new HashMap<String, Map<String, Object>>();
			for(ObjDetBasePlugin detector : this.detectors)
			{
				//need to combine
				MLPluginFrameOutput output = detector.detect(matFrame, null);
				Map<String, Object> mapCurDetections = output.getDetectionOutputMap();
				
				mapDetectorResult.put(detector.getPluginMLModelFileName(), mapCurDetections);
			}
						
			Map<String, Object> mapDetections = new HashMap<String, Object>();
			if(this.detectors.length==1)
			{
				mapDetections = (Map<String, Object>)mapDetectorResult.values().toArray()[0];
			}
			else
			{
				//TODO Merge and NMS multi-detections
			}

			if(mapDetections==null)
				mapDetections = new HashMap<>();
			
			long lTotalDetection = 0;
			FrameDetectedObj frameObjs = (FrameDetectedObj) mapDetections.getOrDefault(ObjDetBasePlugin._KEY_OUTPUT_FRAME_DETECTIONS, null);
			StringBuffer sbDetectedObj = new StringBuffer();
			
			if(frameObjs!=null)
			{	
				lTotalDetection = frameObjs.getFrame_total_detection();
				
				for(String sClassName : frameObjs.getObjClassNames())
				{
					List<DetectedObj> listClassObjs = frameObjs.getDetectedObjByObjClassName(sClassName);
					if(listClassObjs==null)
						listClassObjs = new ArrayList<>();
					
					long count = listClassObjs.size();
					 
					if(sbDetectedObj.length()>0)
						sbDetectedObj.append(",");
					
					sbDetectedObj.append(sClassName).append("(").append(count).append(")");
					
					// List of area of interest
					
					List<Rect2d> listRect2d = new ArrayList<Rect2d>();
					
					// Assign Tracking Id
					int iSeqNo = 0;
					for(DetectedObj curObj : listClassObjs)
					{
						if(privacymask!=null)
						{
							Rect2d objBox = curObj.getObj_bounding_box();
							if(objBox!=null)
							{
								listRect2d.add(objBox);
							}
						}
						
						iSeqNo++;
						curObj.setObj_tmp_trackingid(curObj.getObj_trackingid());
						curObj.setObj_trackingid(sClassName+"_"+aCurFrameNo+"_"+iSeqNo);
						if(prevObjs!=null)
						{
							DetectedObjUtil.updTrackingIdWithPrevDetections(curObj, prevObjs, 0.70);
						}
					}
					
					if(privacymask!=null)
					{
						List<MatOfPoint> listMatOfPoint = PrivacyMaskUtil.rectToMatOfPoint(listRect2d);
						privacymask.mask(matFrame,listMatOfPoint);
					}
					
				}
				
				frameObjs.setFrame_id(aCurFrameNo);
				frameObjs.setFrame_timestamp_ms(aCurFrameMs);
				jsonDetections.put(String.valueOf(aCurFrameNo), frameObjs.toJson());
				
				prevObjs = frameObjs;
				Mat matOutput = DetectedObjUtil.annotateImage(matFrame, frameObjs);
				matFrame.release();
				matFrame = matOutput;
			}
			
			if(sbDetectedObj.length()>0)
			{
				sbDetectedObj.insert(0, " - ");
			}
			
			System.out.println("  Frame #"+aCurFrameNo+" Detection:"+lTotalDetection+" "+sbDetectedObj.toString());
			//
		}
		
		//Re-encoding
		super.decodedVideoFrame(aVideoSourceName, matFrame, aCurFrameNo, aCurFrameMs, aProgressPercentage);

		return matFrame;
	}
	
	@Override
	public boolean initPlugin(JSONObject aMetaJson) 
	{
		return super.initPlugin(aMetaJson);
	}
	
	@Override
	public JSONObject processEnded(String aVideoSourceName, 
			long aAdjSelFrameMsFrom, long aAdjSelFrameMsTo,
			long aTotalProcessed, long aTotalSkipped, long aElpasedMs) {
		
		JSONObject json = super.processEnded(aVideoSourceName, aAdjSelFrameMsFrom, aAdjSelFrameMsTo, 
						aTotalProcessed, aTotalSkipped, aElpasedMs);
		
		json.put("VideoSourceName", aVideoSourceName);
		json.put("ElpasedMs", aElpasedMs);
		
		if(this.detectors!=null)
		{
			StringBuffer sbDetectorNames = new StringBuffer();
			StringBuffer sbDetectorModels = new StringBuffer();
			for(ObjDetBasePlugin detector : this.detectors)
			{
				if(sbDetectorNames.length()>0)
					sbDetectorNames.append(",");
				sbDetectorNames.append(detector.getPluginName());
				//
				if(sbDetectorModels.length()>0)
					sbDetectorModels.append(",");
				File fileModel = new File(detector.getPluginMLModelFileName());
				sbDetectorModels.append(fileModel.getName());
			}
			json.put("DetectorName", sbDetectorNames.toString());
			json.put("DetectorModel", sbDetectorModels.toString());
		}
		
		
		if(jsonDetections!=null && jsonDetections.length()>0)
		{
			json.put("Detections", jsonDetections);
		}
		return json;
	}
	
	
	public static boolean writeJsonToFile(JSONObject jsonData, File fileOutputFile) throws IOException
	{
		boolean isDataWritten = false;
		
		if(jsonData!=null && jsonData.length()>0)
		{
			String sVideoSourceName 	= jsonData.optString("VideoSourceName", null);
			//String sDetectorName 		= jsonData.optString("DetectorName", null);
			String sDetectorModel 		= jsonData.optString("DetectorModel", null);
			JSONObject jsonDetections 	= jsonData.optJSONObject("Detections");
			long lTotalProcessed 		= jsonData.optLong("TotalProcessed", 0);
			long lElapsedMs 			= jsonData.optLong("ElpasedMs", 0);
			
			
			if(lTotalProcessed==0)
				lTotalProcessed = jsonDetections.length();

			String sPadding 	= "0000000000".substring(0, String.valueOf(lTotalProcessed).length());
			
			if(!fileOutputFile.getParentFile().exists())
				fileOutputFile.getParentFile().mkdirs();
			
			BufferedWriter wrt = null;
			try {
				wrt = new BufferedWriter(new FileWriter(fileOutputFile));

				StringBuffer sbFrameData = new StringBuffer();
				wrt.write("{");
				wrt.write("\n \"DataSource\":\""+sVideoSourceName+"\"");
				wrt.write("\n,\"ProcessTimestamp\":"+System.currentTimeMillis());
				wrt.write("\n,\"TotalElapsedMs\":"+lElapsedMs);
				wrt.write("\n,\"MLModel\":\""+sDetectorModel+"\"");
				wrt.write("\n,\"Detections\":");
				wrt.write(" [\n");
				
				if(jsonDetections==null)
					jsonDetections = new JSONObject();
				
				isDataWritten = true;
				for(int iFrameIdx=0; iFrameIdx<lTotalProcessed; iFrameIdx++)
				{
					String sFrameId = String.valueOf(iFrameIdx);
					JSONObject jsonFrameDetections = jsonDetections.optJSONObject(sFrameId);
					
					if(jsonFrameDetections==null)
						continue;
					
					if(iFrameIdx>0)
						sbFrameData.append("\n ,");
					else
						sbFrameData.append("  ");
					
					sbFrameData.append(" {");
					sbFrameData.append("\"").append(sPadding.substring(sFrameId.length())).append(sFrameId).append("\"");
					sbFrameData.append(":").append(jsonFrameDetections.toString());
					sbFrameData.append("}");
					
					wrt.write(sbFrameData.toString());
					sbFrameData.setLength(0);
				}
				wrt.write("\n ]");
				wrt.write("\n}");
			}finally
			{
				 if(wrt!=null)
					 wrt.close();
			}
		}	
		return isDataWritten && fileOutputFile.isFile();
	}
		
}
