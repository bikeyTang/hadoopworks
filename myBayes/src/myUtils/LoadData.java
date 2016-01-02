package myUtils;

import java.io.File;
import java.util.ArrayList;

public class LoadData {
	public static ArrayList<String > filelist=new ArrayList<String>();
	public static void main(String[] args) {
		
	}
	static void getFiles(String dir) throws Exception{
		File root=new File(dir); 
		File[] files=root.listFiles();
		for(File file:files){
			if(file.isDirectory()){
				getFiles(file.getAbsolutePath());
				
			}else{
				filelist.add(file.getAbsolutePath());
				
			}
					
		}
	}
}
