package org.bof.geometric;

import java.util.List;

import net.imglib2.FinalInterval;
import net.imglib2.RandomAccess;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

import org.knime.knip.opencv.base.algorithm.AlgorithmParameter;
import org.knime.knip.opencv.base.descriptor.AbstractDescriptor;
import org.opencv.features2d.KeyPoint;

/**
 * @param <T>
 */
public class GeometricBlurDescriptor<T extends RealType<T> & NativeType<T>>
		extends AbstractDescriptor<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public GeometricBlurDescriptor() {
		super(-1, new AlgorithmParameter<>("Max Sigma", "maxsigma", "maxsigma",
				10), new AlgorithmParameter<>("Min Sigma", "minsigma",
				"minsigma", 2), new AlgorithmParameter<>("Num Levels",
				"numlevels", "numlevels", 10));
	}

	@Override
	public String[] getColumnHeaders() {
		// we use ((4*12)+1)*#channels
		// 4 sample rings with 12 samples per ring plus the keypoint itself
		// and then append each edge channel. we want to go for four edge channels
		// like in the paper
		// therefore we get a descriptor size of: ((4 * 12) + 1) * 4 = 196
		String[] columnHeaders = new String[196];
		for (int i = 1; i <= columnHeaders.length; i++) {
			columnHeaders[i - 1] = "Descriptor Value #" + i;
		}

		return columnHeaders;
	}

	private int[][] getSampleCoordinates( int radius ){
		// coordinates[0][?] := x
		// coordinates[1][?] := y
		int[][] coordinates = new int[2][12];
		//int samplesPerCornerCurve = 2;
		
		
		double xConerStepSize = radius/3;
		
		//sampling will start at the top of the circle clockwise		
		//top right
		// first sample
		coordinates[0][0] = 0;		
		coordinates[1][0] = radius;
		//second sample
		coordinates[0][1] = (int)Math.rint(xConerStepSize);		
		coordinates[1][1] = (int)Math.rint(Math.sqrt(Math.pow(radius,2) - Math.pow(xConerStepSize,2)));//pythagoras: y = sqrt(r²-x²)
		//third sample
		coordinates[0][2] = (int)Math.rint(xConerStepSize*2);		
		coordinates[1][2] = (int)Math.rint(Math.sqrt(Math.pow(radius,2) - Math.pow(xConerStepSize*2,2)));
		//fourth sample
		coordinates[0][3] = radius;		
		coordinates[1][3] = 0;
		//now we just mirror the samples
		//bottom right
		coordinates[0][4] = coordinates[0][2];		
		coordinates[1][4] = -coordinates[1][2];
		coordinates[0][5] = coordinates[0][1];		
		coordinates[1][5] = -coordinates[1][1];
		coordinates[0][6] = coordinates[0][0];		
		coordinates[1][6] = -coordinates[1][0];
		//bottom left
		coordinates[0][7] = -coordinates[0][5];		
		coordinates[1][7] = coordinates[1][5];
		coordinates[0][8] = -coordinates[0][4];		
		coordinates[1][8] = coordinates[1][4];
		coordinates[0][9] = -coordinates[0][3];		
		coordinates[1][9] = coordinates[1][3];
		//top left
		coordinates[0][10] = coordinates[0][8];		
		coordinates[1][10] = -coordinates[1][8];
		coordinates[0][11] = coordinates[0][7];		
		coordinates[1][11] = -coordinates[1][7];
		
		return coordinates;
	}
	
	// for each keypoint and the given input image (blurred image) extract the
	// geometric blur descriptor
	@Override
	public double[][] getDescriptorsForKeypoints(Img<T> img, List<KeyPoint> list) {

		// Create the pyramid of images here
		// using linear sigmas

		// you can get the parameters with getParameters() and looping through
		// the parameters with checking for name

		double minSigma = 2.0d;
		double maxSigma = 10.0d;
		int numLevels = 5;

		// yeah
		double stepSize = (maxSigma - minSigma) / numLevels;

		long[] newDims = new long[] { img.dimension(0), img.dimension(1),
				img.dimension(2), numLevels };

		ArrayImg<DoubleType, ?> tmpImg = new ArrayImgFactory<DoubleType>()
				.create(newDims, new DoubleType());

		for (int l = 0; l < numLevels; l++) {
			// Do that for each "edge filter"
			for (int c = 0; c < img.dimension(2); c++) {
				try {
					Gauss3.gauss(minSigma + (l * stepSize), Views.interval(img,
							new long[] { 0, 0, 0 }, new long[] {
									newDims[0] - 1, newDims[1], c }), Views
							.interval(tmpImg, new FinalInterval(new long[] { 0,
									0, 0, 0 }, new long[] { newDims[0],
									newDims[1], c, l })));
				} catch (IncompatibleTypeException e) {
					e.printStackTrace();
				}
			}
		}

		double[][] data = new double[list.size()][196];

		int[][] coordinates = getSampleCoordinates(10);
		System.out.println(coordinates);
		
		// example using a rectangle-sampling in the 2d plane. We use only the
		// first keypoint here
		final RandomAccess<DoubleType> rndAccess = tmpImg.randomAccess();
		for (final KeyPoint key : list) {
			rndAccess.setPosition((int) key.pt.x, 0);
			rndAccess.setPosition((int) key.pt.y, 1);

			// Unfortunately we support only 2D since now. In our case we need
			// the channel (Edge) information. We use another field in the
			// keypoint ( octave is the channel number )
			rndAccess.setPosition(key.octave, 2);

			
			
			// now we have set the random access to the center point on or image
			// from where it was extracted.
			// you guys have to implement the function which samples around the
			// point some other points (as in the paper).
			// use rndAccess.setPosition(X value, 0)
			// use rndAccess.setPosition(Y value, 1) to do so..

			// to access the different gauss levels use
			// rndAccess.setPosition(GAUSS LEVEL,3)
			
			// use rndAccess.get() (returns double because the rndAccess is specified 
			// as double above) to access the pixel the randomAccess is pointing at
		}

		return data;
	}
}
