package count;

import java.io.IOException;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;

public class CreateVocVectory {
	public static class DocMap extends Mapper<Object,Text,Text,Text>{
		private Text word=new Text();
		private IntWritable one=new IntWritable(1);
		public void map(Object key,Text value,Context context) throws IOException,InterruptedException{
			//StringTokenizer itr=new StringTokenizer(value.toString());
			String line=value.toString();
			word.set(line);
			//System.out.println(line);
			context.write(word,new Text(""));
		}
		
	}
	
	public static class DocReduce extends Reducer<Text ,Text,Text,Text>{
		public final static IntWritable one=new IntWritable(1); 
		public void reduce(Text key,Iterable<Text> values,Context context) throws IOException,InterruptedException{
			//System.out.println(key);
			
			context.write(key,new Text(""));
		}
	}
	public static void main(String[] args) throws Exception{
		Configuration conf=new Configuration();
		conf.set("mapreduce.input.fileinputformat.input.dir.recursive","true");
		Path outputPath=new Path(args[1]);
		outputPath.getFileSystem(conf).delete(outputPath,true);
		Job job=Job.getInstance(conf,"create voc");
		job.setJarByClass(CreateVocVectory.class);
		job.setMapperClass(DocMap.class);
		job.setCombinerClass(DocReduce.class);
		job.setReducerClass(DocReduce.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		
		FileInputFormat.setInputPaths(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job,new Path(args[1]));
		System.exit(job.waitForCompletion(true)?  0:1);
		
	}

}
