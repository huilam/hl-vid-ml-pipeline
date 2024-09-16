/*
 Copyright (c) 2021 onghuilam@gmail.com
 
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

package hl.vid.pipeline;

import java.io.File;
import org.json.JSONObject;

import hl.common.FileUtil;
import hl.img.imgfilters.IPrivacyMask;
import hl.img.imgfilters.PrivacyMaskUtil;
import hl.img.imgfilters.algo.Blur;
import hl.objml.opencv.objdetection.dnn.plugins.yolo.YoloXDetector;
import hl.objml2.plugin.ObjDetectionBasePlugin;
import hl.opencv.util.OpenCvUtil;
import hl.opencv.video.processor.VideoProcessor;
import hl.vid.pipeline.processor.VideoObjDetectionPlugin;

public class TestVideoObjDetection {
	
	protected static File[] getTestVideoFiles()
	{
		File folderImages = new File("./test/videos/");
		File[] files = new File[] {};;
		
		if(folderImages.isDirectory())
		{
			files = FileUtil.getFilesWithExtensions(
					folderImages, 
					new String[]{
							".mp4"});
		}
		
		if(files.length==0)
		{
			System.out.println("No file found. - "+folderImages.getAbsolutePath());
		}
		return files;
	}
	
	
	public static void main(String args[]) throws Exception
	{
		OpenCvUtil.initOpenCV();
		
		ObjDetectionBasePlugin detector1 = new YoloXDetector();
		//ObjDetectionBasePlugin detector2 = new YunetFaceDetector();
		
		if(detector1.isPluginOK())
		{
			File fileOutputFolder = new File("./test/videos/output");
	
			VideoObjDetectionPlugin vidObjDetectionPlugin = 
					new VideoObjDetectionPlugin(new ObjDetectionBasePlugin[] {detector1}, fileOutputFolder);
			
			vidObjDetectionPlugin.addObjOfInterest(new String[]{"person","face"});
			vidObjDetectionPlugin.addObjOfInterest(new String[]{"car","bus","truck"});

			IPrivacyMask privacyMaskAlgo = 
					PrivacyMaskUtil.getPrivacyMaskInstance("hl.img.imgfilters.algo.Pixelate");
			vidObjDetectionPlugin.setPrivacyMaskAlgo(privacyMaskAlgo);
			
			VideoProcessor vidProcessor = new VideoProcessor();
			for(File fileVid : getTestVideoFiles())
			{
				System.out.println(" Processing "+fileVid.getName()+" ...");
				
				JSONObject jsonData = vidProcessor.processVideoFile(fileVid, vidObjDetectionPlugin);
				
				File fileOutput = new File(fileOutputFolder.getAbsolutePath()+"/"+fileVid.getName()+"_detections.json");
				
				if(VideoObjDetectionPlugin.writeJsonToFile(jsonData, fileOutput))
				{
					System.out.println(" Output Json : "+fileOutput.getAbsolutePath());
				}
			}
		}
	}
}
