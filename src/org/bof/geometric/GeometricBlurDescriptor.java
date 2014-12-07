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
		// TODO: Length of descriptor. Now 32.
		String[] columnHeaders = new String[32];
		for (int i = 1; i <= columnHeaders.length; i++) {
			columnHeaders[i - 1] = "Descriptor Value #" + i;
		}

		return columnHeaders;
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
		double maxSigma = 10.d;
		int numLevels = 5;

		// yeah
		double stepSize = (minSigma - maxSigma) / numLevels;

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

		double[][] data = new double[list.size()][32];

		// example using a rectangle-sampling in the 2d plane. We use only the
		// first keypoint here
		final RandomAccess<DoubleType> rndAccess = tmpImg.randomAccess();
		for (final KeyPoint key : list) {
			rndAccess.setPosition((int) key.pt.x, 0);
			rndAccess.setPosition((int) key.pt.y, 1);

			// Unfortunately we support only 2D since now. In our case we need
			// the channel (Edge) information. We use another field in the
			// keypoint
			rndAccess.setPosition((int) key.octave, 2);

			// now we have set the random access to the center point on or image
			// from where it was extracted.
			// you guys have to implement the function which samples around the
			// point some other points (as in the paper).
			// use rndAccess.setPosition(X value, 0)
			// use rndAccess.setPosition(Y value, 1) to do so..

			// to access the different gauss levels use
			// rndAccess.setPosition(GAUSS LEVEL,3)
		}

		return data;
	}
}
