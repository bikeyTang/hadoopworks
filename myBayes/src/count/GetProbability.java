package count;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Spliterator;
import java.util.function.Consumer;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;

public class GetProbability extends Configuration implements Tool{
	public static class DocMap extends Mapper<Object,Text,Text,Text>{
		private Text word=new Text();
		private IntWritable one=new IntWritable(1);
		public void map(Object key,Text value,Context context) throws IOException,InterruptedException{
			//StringTokenizer itr=new StringTokenizer(value.toString());
			InputSplit inputSplit=context.getInputSplit();
			String fileName=((FileSplit)inputSplit).getPath().toString();
			String[] str_arr=fileName.split("/");
			int str_length=str_arr.length;
			Text c=new Text(str_arr[str_length-2]);
			Text doc_id=new Text(str_arr[str_length-1]);
			//System.out.println(fileName);
			System.out.println(value);
			context.write(c,doc_id);
		}
		
	}
	public static class DOCCombiner extends Reducer<Text,Text,Text,IntWritable>{
		public void reduce (Text key,Iterable<Text> values,Context context) throws IOException ,InterruptedException{
			HashSet hs=new HashSet();
			for (Text val:values){
				hs.add(val);
			}
			context.write(key,new IntWritable(hs.size()));
		}
	}
	public class WholeFileInputFormat extends FileInputFormat<NullWritable,BytesWritable>{
		
		@Override
		protected boolean isSplitable(JobContext context,Path path){
			return false;
		}
		
		@Override
		public RecordReader<NullWritable, BytesWritable> createRecordReader(
				InputSplit split, TaskAttemptContext context) throws IOException,
				InterruptedException {
			// TODO Auto-generated method stub
			WholeFileRecoderReader reader=new WholeFileRecoderReader();
			reader.initialize(split, context);
			
			return reader;
		}
		
	}
	class WholeFileRecoderReader extends RecordReader<NullWritable,BytesWritable>{
		private FileSplit fileSplit;
		private Configuration conf;
		private BytesWritable value=new BytesWritable();
		private boolean processed =false;
		@Override
		public void close() throws IOException {
			// TODO Auto-generated method stub
			
		}
		@Override
		public NullWritable getCurrentKey() throws IOException,
				InterruptedException {
			// TODO Auto-generated method stub
			return NullWritable.get();
		
		}
		@Override
		public BytesWritable getCurrentValue() throws IOException,
				InterruptedException {
			// TODO Auto-generated method stub
			return value;
		}
		@Override
		public float getProgress() throws IOException, InterruptedException {
			// TODO Auto-generated method stub
			return processed? 1.0f:0.0f;
		}
		@Override
		public void initialize(InputSplit split, TaskAttemptContext context)
				throws IOException, InterruptedException {
			// TODO Auto-generated method stub
			this.fileSplit=(FileSplit)split;
			this.conf=context.getConfiguration();
		}
		@Override
		public boolean nextKeyValue() throws IOException, InterruptedException {
			// TODO Auto-generated method stub
			if(!processed){
				byte[] contents=new byte[(int)fileSplit.getLength()];
				Path file=fileSplit.getPath();
				FileSystem fs=file.getFileSystem(conf);
				FSDataInputStream in=null;
				try{
					in =fs.open(file);
					IOUtils.readFully(in, contents,0,contents.length);
					value.set(contents,0,contents.length);
				}finally{
					IOUtils.closeQuietly(in);
				}
				processed=true;
				return true;
			}
			return false;
		}
		
	}
	public static class DocReduce extends Reducer<Text ,Text,Text,IntWritable>{
		public final static IntWritable one=new IntWritable(1);
		
		public void reduce(Text key,Iterable<IntWritable> values,Context context) throws IOException,InterruptedException{
			//System.out.println(key);
			IntWritable  sum=new IntWritable(0);
			for(IntWritable val:values){
				sum.set(val.get()+sum.get());
			}
			context.write(key,sum);
		}
	}
	public static void main(String[] args) throws Exception{
		int res=ToolRunner.run(new Configuration(), new GetProbability(),args);
		System.exit(res);
		
	}
	public Configuration getConf() {
		// TODO Auto-generated method stub
		return null;
	}
	public void setConf(Configuration arg0) {
		// TODO Auto-generated method stub
		
	}
	public void forEach(Consumer<? super Entry<String, String>> action) {
		// TODO Auto-generated method stub
		
	}
	public Spliterator<Entry<String, String>> spliterator() {
		// TODO Auto-generated method stub
		return null;
	}
	public int run(String[] args) throws Exception {
		// TODO Auto-generated method stub
		Configuration conf=new Configuration();
		conf.set("mapreduce.input.fileinputformat.input.dir.recursive","true");
		Path outputPath=new Path(args[1]);
		outputPath.getFileSystem(conf).delete(outputPath,true);
		Job job=Job.getInstance(conf,"getP");
		job.setJarByClass(GetProbability.class);
		
		job.setMapperClass(DocMap.class);
		job.setCombinerClass(DocReduce.class);
		job.setReducerClass(DocReduce.class);
		job.setInputFormatClass(WholeFileInputFormat.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		
		FileInputFormat.setInputPaths(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job,new Path(args[1]));
		System.exit(job.waitForCompletion(true)?  0:1);
		return 0;
	}
}
	

