package me.doubledutch.phat.streams;

import java.nio.file.StandardOpenOption;
import java.nio.channels.*;
import java.nio.ByteBuffer;
import java.util.concurrent.*;
import java.util.*;
import java.io.*;

public class Block{
	private final static int BUFFER_SIZE=512*1024;
	// private FileDescriptor fd;
	private FileChannel fc;
	private FileOutputStream fout;
	private OutputStream out;
	// private RandomAccessFile in;
	private AsynchronousFileChannel in;
	private long offset;
	private int write_mode;

	public Block(String filename,int write_mode) throws IOException{
		fout=new FileOutputStream(filename,true);
		out=new BufferedOutputStream(fout,BUFFER_SIZE);
		fc=fout.getChannel();
		// fd=fout.getFD();
		// in=new RandomAccessFile(filename,"r");
		in=AsynchronousFileChannel.open(new File(filename).toPath(),StandardOpenOption.READ);
		offset=new File(filename).length();
		this.write_mode=write_mode;
	}

	public long getSize(){
		synchronized(out){
			return offset;
		}
	}

	public byte[] read(IndexEntry indexEntry) throws IOException{
		return read(indexEntry.getOffset(),indexEntry.getSize());
	}

	public byte[] read(long readOffset,int size) throws IOException{
		ByteBuffer buffer=ByteBuffer.allocate(size);
		Future<Integer> f=in.read(buffer,readOffset);
		try{
			int i=f.get();
			if(i==-1)return null;
			return buffer.array();
		}catch(InterruptedException ie){

		}catch(ExecutionException ee){

		}
		return null;
	}

	public long write(byte[] data) throws IOException{
		synchronized(out){
			long currentOffset=offset;
			out.write(data,0,data.length);
			offset+=data.length;
			return currentOffset;
		}
	}

	public void commit() throws IOException{
		if(write_mode<Stream.NONE){
			synchronized(out){		
				if(write_mode==Stream.FLUSH){
					out.flush();
				}else if(write_mode<Stream.FLUSH){
					out.flush();
					fc.force(true);
					// fd.sync();
				}
			}
		}
	}

	public void close(){
		synchronized(out){
			try{
				commit();
			}catch(Exception e){
				e.printStackTrace();
			}
			try{
				in.close();
			}catch(Exception e){
				e.printStackTrace();
			}
			try{
				out.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}