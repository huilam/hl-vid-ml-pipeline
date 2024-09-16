package hl.vid.pipeline;

import java.io.File;
import java.util.Map;

import org.json.JSONObject;
import org.opencv.core.Mat;

import hl.common.FileUtil;
import hl.objml2.common.DetectedObj;
import hl.objml2.plugin.IObjDetectionPlugin;
import hl.objml2.plugin.ObjDetectionBasePlugin;
import hl.opencv.util.OpenCvUtil;

public class BaseTester {
	
	protected static File[] getTestImageFiles()
	{
		File folderImages = new File("./test/images/");
		
		if(folderImages.isDirectory())
		{
			return FileUtil.getFilesWithExtensions(folderImages, 
					new String[]{
							".jpg",
							".png"});
		}
		else
		{
			return new File[] {};
		}
		
	}
	
	protected static String saveImage(
			String aPluginName,
			Mat aMatImage, File aOutputFolder, String aOrigImgFileName)
	{
		if(!aOutputFolder.exists()) 
			aOutputFolder.mkdirs();
		
		String sOutputFileName = aPluginName+"_"+aOrigImgFileName;
	
		boolean isSaved = OpenCvUtil.saveImageAsFile(aMatImage, aOutputFolder.getAbsolutePath()+"/"+sOutputFileName);
		
		if(isSaved)
			return aOutputFolder.getName()+"/"+sOutputFileName;
		return null;
	}
	
	public static void testDetector(ObjDetectionBasePlugin aDetector)
	{
		OpenCvUtil.initOpenCV();
		
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		aDetector.isPluginOK();
		
		File fileFolder = new File("./test/images/output/"+System.currentTimeMillis());
		
		int i = 1;
		
		for(File fImg : getTestImageFiles())
		{
			System.out.println();
			System.out.print(" "+(i++)+". Perform test on "+fImg.getName()+" ... ");
			
			Mat matImg = ObjDetectionBasePlugin.getCvMatFromFile(fImg);
			OpenCvUtil.removeAlphaChannel(matImg);
			
			Map<String, Object> mapResult = aDetector.detect(matImg, null);
			
			System.out.println("     - Result : "+(mapResult!=null?mapResult.size():0));
			
			if(mapResult!=null)
			{
				DetectedObj objs = new DetectedObj();
				objs.addAll((JSONObject) mapResult.get(IObjDetectionPlugin._KEY_OUTPUT_DETECTION_JSON));
				
				System.out.println("     - Total Count : "+objs.getTotalDetectionCount());
				StringBuffer sb = new StringBuffer();
				for(String sClassName : objs.getObjClassNames())
				{
					long count = objs.getDetectionCount(sClassName);
					
					if(sb.length()>0)
						sb.append(",");
					
					sb.append(sClassName).append("(").append(count).append(")");
					
				}
				System.out.println("     - Detection : "+sb.toString());
				
				
				Mat matOutput = (Mat) mapResult.get(IObjDetectionPlugin._KEY_OUTPUT_ANNOTATED_MAT);
				
				if(matOutput!=null && !matOutput.empty())
				{
					String savedFileName = 
							saveImage(aDetector.getPluginName(), 
							matOutput, 
							fileFolder, fImg.getName());
					
					if(savedFileName!=null)
						System.out.println("     - [saved] "+savedFileName);
				}
				else
				{
					int idx = 0;
					for(String key : mapResult.keySet())
					{
						System.out.println("    ["+idx+"] "+key+" - "+mapResult.get(key));
						idx++;
					}
				}
			}
			else
			{
				System.out.println("     - Result : "+mapResult);
			}
			
		}		
	}
	
	public void main(String args[])throws Exception
	{
		
	}
	
}