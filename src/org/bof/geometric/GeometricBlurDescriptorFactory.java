package org.bof.geometric;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import org.knime.knip.opencv.base.descriptor.AbstractDescriptorNodeFactory;

/**
 * @param <T extends RealType<T> & NativeType<T>>
 */
public class GeometricBlurDescriptorFactory<T extends RealType<T> & NativeType<T>> extends
		AbstractDescriptorNodeFactory<T> {

	public GeometricBlurDescriptorFactory() {
		super(new GeometricBlurDescriptor<T>());
	}

}
