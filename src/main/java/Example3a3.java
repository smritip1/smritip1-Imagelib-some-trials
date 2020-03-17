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

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.Point;
import net.imglib2.img.Img;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.RealType;

/**
 * Perform a generic min/max search.
 *
 * @author Stephan Preibisch
 * @author Stephan Saalfeld
 */
public class Example3a3
{
	public < T extends RealType< T > & NativeType< T > > Example3a3()
		throws ImgIOException
	{
		// open with SCIFIO (it will decide which Img is best)
		Img< T > img = ( Img< T > ) IO.openImgs( "DrosophilaWing.tif" ).get( 0 );

		// create two location objects
		Point locationMin = new Point( img.numDimensions() );
		Point locationMax = new Point( img.numDimensions() );

		// compute location of min and max
		computeMinMaxLocation( img, locationMin, locationMax );

		System.out.println( "location of minimum Value (img): " + locationMin );
		System.out.println( "location of maximum Value (img): " + locationMax );
	}

	/**
	 * Compute the location of the minimal and maximal intensity for any IterableInterval,
	 * like an {@link Img}.
	 *
	 * The functionality we need is to iterate and retrieve the location. Therefore we need a
	 * Cursor that can localize itself.
	 * Note that we do not use a LocalizingCursor as localization just happens from time to time.
	 *
	 * @param input - the input that has to just be {@link IterableInterval}
	 * @param minLocation - the location for the minimal value
	 * @param maxLocation - the location of the maximal value
	 */
	public < T extends Comparable< T > & Type< T > > void computeMinMaxLocation(
		final IterableInterval< T > input, final Point minLocation, final Point maxLocation )
	{
		// create a cursor for the image (the order does not matter)
		final Cursor< T > cursor = input.cursor();

		// initialize min and max with the first image value
		T type = cursor.next();
		T min = type.copy();
		T max = type.copy();

		// loop over the rest of the data and determine min and max value
		while ( cursor.hasNext() )
		{
			// we need this type more than once
			type = cursor.next();

			if ( type.compareTo( min ) < 0 )
			{
				min.set( type );
				minLocation.setPosition( cursor );
			}

			if ( type.compareTo( max ) > 0 )
			{
				max.set( type );
				maxLocation.setPosition( cursor );
			}
		}
	}

	public static void main( String[] args ) throws ImgIOException
	{
		// run the example
		new Example3a3();
	}
}
