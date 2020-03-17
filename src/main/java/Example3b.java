/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2016 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
 * John Bogovic, Albert Cardona, Barry DeZonia, Christian Dietz, Jan Funke,
 * Aivar Grislis, Jonathan Hale, Grant Harris, Stefan Helfrich, Mark Hiner,
 * Martin Horn, Steffen Jaensch, Lee Kamentsky, Larry Lindsey, Melissa Linkert,
 * Mark Longair, Brian Northan, Nick Perry, Curtis Rueden, Johannes Schindelin,
 * Jean-Yves Tinevez and Michael Zinsmaier.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
import io.scif.img.IO;
import io.scif.img.ImgIOException;

import net.imglib2.img.Img;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.RealSum;

/**
 * Perform a generic computation of average intensity
 *
 * @author Stephan Preibisch
 * @author Stephan Saalfeld
 */
public class Example3b
{
	public < T extends RealType< T > & NativeType< T > > Example3b() throws
		ImgIOException
	{
		// open with ImgOpener
		final Img< T > img = (Img< T >) IO.openImgs( "DrosophilaWing.tif" ).get( 0 );

		// compute average of the image
		final double avg = computeAverage( img );
		System.out.println( "average Value: " + avg );
	}

	/**
	 * Compute the average intensity for an {@link Iterable}.
	 *
	 * @param input - the input data
	 * @return - the average as double
	 */
	public < T extends RealType< T > > double computeAverage( final Iterable< T > input )
	{
		// Count all values using the RealSum class.
		// It prevents numerical instabilities when adding up millions of pixels
		final RealSum realSum = new RealSum();
		long count = 0;

		for ( final T type : input )
		{
			realSum.add( type.getRealDouble() );
			++count;
		}

		return realSum.getSum() / count;
	}

	public static void main( final String[] args ) throws ImgIOException
	{
		// run the example
		new Example3b();
	}
}
