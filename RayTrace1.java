


import java.awt.image.*;
import java.awt.*;
import java.io.*;
import javax.imageio.*;
import java.util.*;
import java.io.File;
import java.util.Scanner;
import org.bytedeco.ffmpeg.*;
import org.bytedeco.ffmpeg.avcodec.*;
import org.bytedeco.ffmpeg.avformat.*;
import org.bytedeco.ffmpeg.avutil.*;
import static org.bytedeco.ffmpeg.global.avcodec.*;
import static org.bytedeco.ffmpeg.global.avformat.*;
import static org.bytedeco.ffmpeg.global.avutil.*;
import org.bytedeco.javacv.*;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber.Exception;

import org.bytedeco.javacv.Java2DFrameConverter;

public class raytracing {
	static int WIDTH = 1024;
	static int HEIGHT = 768;


	static FFmpegFrameRecorder recorder;
	static File outfile;
	static Frame frame;
	static BufferedImage buffer;


	static int R[][] = new int[WIDTH][HEIGHT];
	static int G[][] = new int[WIDTH][HEIGHT];
	static int B[][] = new int[WIDTH][HEIGHT];
	static Java2DFrameConverter converter;

	static int eyex = 0;
	static int eyey = 0;
	static int eyez = 0;
	static int viewz = 0;
	
	public static void main(String args[]) throws Exception, IOException {
		ArrayList<Sphere> spheres = new ArrayList<>();

		if (args.length != 1) {
			System.out.println("Usage: java demo <output file name>");
			System.exit(0);
		}

		outfile = new File(args[0]);
		recorder = new FFmpegFrameRecorder(outfile, WIDTH, HEIGHT);

	
		recorder.setVideoCodec(AV_CODEC_ID_WMV2);

	
		recorder.setFrameRate(30);
		recorder.setVideoOption("preset", "ultrafast");
		recorder.setVideoBitrate(35000000);
		recorder.start();
		buffer = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
		converter = new Java2DFrameConverter();



		
		File inputfile = null;
		Scanner fileinput = null;
		Scanner keyinput = new Scanner(System.in);
		String filename;
		String word;
		System.out.println("Enter a file name: ");
		filename = keyinput.next();
		inputfile = new File(filename);
		fileinput = new Scanner(inputfile); 
											

		
		Sphere sphere = null;
		while (fileinput.hasNext()) {
			word = fileinput.next();

			if (word.equals("EYE:")) {
			
				eyex = Integer.parseInt(fileinput.next());
				eyey = Integer.parseInt(fileinput.next());
				eyez = Integer.parseInt(fileinput.next());
			} else if (word.equals("VIEWZ:")) {
			
				viewz = Integer.parseInt(fileinput.next());
			} else if (word.equals("SPHERES:")) {
				
				int sphereNum = Integer.parseInt(fileinput.next());
			} else if (word.equals("SPHERE:")) {
				sphere = new Sphere();
				spheres.add(sphere);
				
				sphere.x = Integer.parseInt(fileinput.next());
				sphere.y = Integer.parseInt(fileinput.next());
				sphere.z = Integer.parseInt(fileinput.next());
				sphere.radius =Integer.parseInt(fileinput.next());
			} else if (word.equals("COLOR:")) {
				sphere.r = Integer.parseInt(fileinput.next());
				sphere.g =  Integer.parseInt(fileinput.next());
				sphere.b= Integer.parseInt(fileinput.next());
			}
		}
		for(Sphere sp : spheres) {
			drawcircle(sp);
		}

		for (int h = 0; h < 30; h++) {
			drawframe();
		}

		recorder.stop();
	}
	public static void drawcircle(Sphere sphere) throws Exception, IOException {
		for (int x = 0; x < WIDTH; x++) {
			for (int y = 0; y < HEIGHT; y++) {
				double dx = x - eyex;
				double dy = y - eyey;
				double dz = viewz - eyez;

				double a = ((Math.pow(dx, 2)) + (Math.pow(dy, 2)) + (Math.pow(dz, 2)));
				double b = (2 * dx * (eyex - sphere.x)) + (2 * (dy) * ((eyey) - sphere.y))
						+ ((2 * dz) * (eyez - sphere.z));
				double c = (Math.pow(sphere.x, 2) + Math.pow(sphere.y, 2) + Math.pow(sphere.z, 2) + Math.pow(eyex, 2)
						+ Math.pow(eyey, 2) + Math.pow(eyez, 2)
						+ -2 * ((sphere.x * eyex) + (sphere.y * eyey) + (sphere.z * eyez)) - Math.pow(sphere.radius, 2));

				double disc;
				disc = (b * b) - (4 * a * c);

				// solving for t

				if (disc >= 0) {

					R[x][y] = sphere.r;
					G[x][y] = sphere.g;
					B[x][y] = sphere.b;

				}
			}
		}

	}

	public static void drawframe() throws Exception, IOException {
		int x, y;
		int pixcolor;
		Color pcolor;

		for (x = 0; x < WIDTH; x++) {
			for (y = 0; y < HEIGHT; y++) {

				buffer.setRGB(x, y, R[x][y] << 16 | G[x][y] << 8 | B[x][y]);
			}
		}
	
		frame = converter.convert(buffer);

		
		recorder.record(frame, AV_PIX_FMT_ARGB);
	}
	
	static class Sphere {
		int x, y, z;
		int radius;
		int r, g, b;
		
	}

}

