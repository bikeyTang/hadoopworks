
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
public class Test2 {
	static ArrayList<String > path_list=new ArrayList<String>();
	static Map<String,Integer> st=new HashMap<String,Integer>();
	static Map<String,Integer> cnt=new HashMap<String,Integer>();
	/*
	 * 读文件，按行读取，然后放入map 中，把行号作为key，把行字符串作为值
	 */
	public static Map<Integer,String> readFileByLine(String path) throws IOException{
		File file=new File(path);
		BufferedReader reader=null;
		Map<Integer,String> mp=new HashMap<Integer,String>();
		try {
			reader=new BufferedReader(new FileReader(file));
			String tempString =null;
			int line=1;
			
			while((tempString=reader.readLine())!=null){
				mp.put(line, tempString);
				line++;
			}
			reader.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}finally{
			if(reader!=null){
				try {
					reader.close();
				} catch (Exception e2) {
					// TODO: handle exception
					e2.printStackTrace();
				}
			}
		}
		return mp;
	}
	/*
	 *把map的value映射成一个 set
	 */
	public static HashSet<String> getVectory(Map<Integer,String> map){
		Iterator<String> itr=map.values().iterator();
		HashSet<String> set=new HashSet<String>();
		//Vector<String> v=new Vector<String>();
		while(itr.hasNext()){
			set.add(itr.next().toString());
		}
		return set;
	}
	/*
	 * 把从文件中读取到的map转化成相应的map
	 */
	public static Map<String,Integer> transMap(Map<Integer, String> cn_map){
		Map<String,Integer> m=new HashMap<String,Integer>();
		for(Map.Entry<Integer, String> val:cn_map.entrySet()){
			StringTokenizer st=new StringTokenizer(val.getValue());
			String ikey=st.nextToken();
			String ivalue="";
			if(st.countTokens()>=2){
				String tvalue=st.nextToken();
				ivalue=st.nextToken();
				m.put(ikey+" "+tvalue, Integer.parseInt(ivalue));
			}else{
				ivalue=st.nextToken();
				m.put(ikey, Integer.parseInt(ivalue));
			}
		}
		return m;
	}
	public static Map<String,Double> transMapD(Map<Integer, String> cn_map){
		Map<String,Double> m=new HashMap<String,Double>();
		for(Map.Entry<Integer, String> val:cn_map.entrySet()){
			StringTokenizer st=new StringTokenizer(val.getValue());
			String ikey=st.nextToken();
			String ivalue="";
			ivalue=st.nextToken();
			m.put(ikey, Double.parseDouble(ivalue));
		}
		return m;
	}
	/**
	 * 计算概率
	 * @param cwn
	 * @param cn
	 * @param set
	 * @return
	 */
	public static Map<String,Double> getCondLogProbability(Map<String,Integer> cwn,Map<String,Integer>  cn,HashSet<String> set){
		int v_length=set.size();
		Map<String,Double> mp=new HashMap<String,Double>();
		
		for(Map.Entry<String, Integer> val:cwn.entrySet()){
			String[] s=val.getKey().split(" ");
			int vt=val.getValue();
			int vt_sum=cn.get(s[0]); //类别词数+词典向量长度
			double p=Math.log(vt)-Math.log(vt_sum);
			mp.put(val.getKey(),p);
		}
		System.out.println("changed:"+mp.size());
		return mp;
	}
	public static String predictP(Map<String,Double>  priorP,Map<String,Double> condp,Map<String,Integer> doc,HashSet<String> set,Map<String,Integer> cn){
		String c=null;
		int set_length=set.size();
		double p_max=Double.NEGATIVE_INFINITY;
		for(Map.Entry<String, Integer> cn_val:cn.entrySet()){
			double p=0;
			for(Map.Entry<String, Integer> doc_val:doc.entrySet()){
				String word=doc_val.getKey();
				String c_key=cn_val.getKey()+" "+word;
				
				if(condp.containsKey(c_key)){
					double cp=condp.get(c_key);
					p+=cp;
				}else{
					//p+=Double.NEGATIVE_INFINITY;
					//p+=Math.log(1)-Math.log(cn_val.getValue()*2);
					p+=-Math.log(cn_val.getValue()+set_length);
				}
				
			}
			p+=priorP.get(cn_val.getKey());
//			System.out.println(p);
//			System.out.println(cn_val.getKey());
			if(p>p_max){
				p_max=p;
				c=cn_val.getKey();
			}
		
		}
	//	System.out.println(p_max);
		return c;
	}
	public static void main(String[] args) throws IOException {
		String root_dir="/home/hadoop/桌面/";
		String cn_file=root_dir+"C-N";
		String cwn_file=root_dir+"((c,w),n)";
		String v_file=root_dir+"v";
		String priorP_file=root_dir+"log(prior p )";
		Map<Integer,String> cn_map=readFileByLine(cn_file);
		Map<Integer,String> cwn_map=readFileByLine(cwn_file);
		Map<Integer,String> v_map=readFileByLine(v_file);
		Map<Integer,String> priorP_map=readFileByLine(priorP_file);
		HashSet<String> v_set=getVectory(v_map);
		Map<String,Integer> cn_o=null,cwn_o=null;
		Map<String,Double> priorP=transMapD(priorP_map);
		cn_o=transMap(cn_map);
		cwn_o=transMap(cwn_map);
		Map<String,Double> imp=new HashMap<String,Double>();
		imp=getCondLogProbability(cwn_o, cn_o, v_set);
		int rp=0,fp=0;
		getPaths("/home/hadoop/下载/NB_test/Country");
		Iterator<String> it=path_list.iterator();
		while(it.hasNext()){
			boolean r=getResult(it.next(), priorP, imp, v_set, cn_o);
			if(r){
				rp++;
			}else{
				fp++;
			}
		}
		
		for(Map.Entry<String, Integer> val:cnt.entrySet()){
			String cnt_key=val.getKey();
			int total=val.getValue();
			int fps=0;
			if(st.containsKey(cnt_key)){
				fps=st.get(cnt_key);
			}
			int tp=total-fps;
			double p=tp/total;
			System.out.println(cnt_key+" total: "+total+"  :  tp:"+tp+"  fp:"+fps);
		}
		System.out.println(rp);
		System.out.println(fp);
	}
	public static void  getPaths(String path){
		
		File file=new File(path);
		if(!file.isDirectory()){
			path_list.add(file.toString());
		}else if(file.isDirectory()){
			String[] filelist=file.list();
			for(int i=0;i<filelist.length;i++){
				File readfile=new File(file.getPath()+"/"+filelist[i]);
				if(!readfile.isDirectory()){
					path_list.add(readfile.toString());
				}else{
					getPaths(readfile.toString());
				}
				
			}
		}
	}
	public static Map<String,Integer> readFiletoMap(String path){
		File file=new File(path);
		BufferedReader reader=null;
		Map<String,Integer> mp=new HashMap<String,Integer>();
		try {
			reader=new BufferedReader(new FileReader(file));
			String tempString =null;
			
			while((tempString=reader.readLine())!=null){
				String ikey=tempString;
				Integer ivalue=1;
				if(mp.containsKey(ikey)){
					ivalue+=mp.get(ikey);
				}
				mp.put(ikey,ivalue);
			}
			reader.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}finally{
			if(reader!=null){
				try {
					reader.close();
				} catch (Exception e2) {
					// TODO: handle exception
					e2.printStackTrace();
				}
			}
		}
		return mp;
	}
	public static boolean getResult(String path,Map<String,Double> priorP,Map<String,Double> condP,HashSet<String> set,Map<String,Integer> cn_o) throws IOException{
		String[] str_path=path.split("/");
		String true_class=str_path[str_path.length-2];
		if(cnt.containsKey(true_class)){
			cnt.put(true_class, 1+cnt.get(true_class));
		}else{
			cnt.put(true_class, 1);
		}
		Map<String,Integer> t_o=readFiletoMap(path);;
		String result=predictP(priorP,condP,t_o,set,cn_o);
		//System.out.println(result+"-----"+true_class);
		if(result.equals(true_class)){
			return true;
		}else{
			if(st.containsKey(true_class)){
				st.put(true_class, st.get(true_class)+1);
			}else{
				st.put(true_class, 1);
			}
			
			return false;
		}
		
	}

}
