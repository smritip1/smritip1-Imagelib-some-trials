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
import ij.ImageJ;

import io.scif.img.IO;
import io.scif.img.ImgIOException;

import java.util.Random;

import net.imglib2.Interval;
import net.imglib2.IterableRealInterval;
import net.imglib2.KDTree;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealInterval;
import net.imglib2.RealPoint;
import net.imglib2.RealPointSampleList;
import net.imglib2.RealRandomAccess;
import net.imglib2.RealRandomAccessible;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.interpolation.neighborsearch.InverseDistanceWeightingInterpolatorFactory;
import net.imglib2.interpolation.neighborsearch.NearestNeighborSearchInterpolatorFactory;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.neighborsearch.KNearestNeighborSearch;
import net.imglib2.neighborsearch.KNearestNeighborSearchOnKDTree;
import net.imglib2.neighborsearch.NearestNeighborSearch;
import net.imglib2.neighborsearch.NearestNeighborSearchOnKDTree;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

/**
 * Working with sparse data, sample an existing image at random locations
 * and render it again using an increasing number of samples
 */
public class Example8b
{
	public Example8b() throws ImgIOException
	{
		// open with SCIFIO using a FloatType
		Img< FloatType > img = IO.openImgs( "DrosophilaWingSmall.tif",
			 new FloatType() ).get( 0 );

		// show the image
		ImageJFunctions.show( img );

		// use linear interpolation to convert the input into a RealRandomAccessible
		RealRandomAccessible< FloatType > realRandomAccessible =
			Views.interpolate( Views.extendMirrorSingle( img ),
				new NLinearInterpolatorFactory< FloatType >() );

		// sample the image with an increasing number of random points and display the result
		for ( int numPoints = 2; numPoints <= 32768; numPoints = numPoints * 4 )
		{
			// the result when we use nearest neighbor interpolation
			ImageJFunctions.show( randomSampling(
				realRandomAccessible, img, numPoints ), numPoints +" points (NN)" );

			// the result when use a distance weighted interpolation of the 20 nearest neighbors
			ImageJFunctions.show( randomSamplingKNearest(
				realRandomAccessible, img, numPoints ), numPoints + " points (KNN)" );
		}
	}

	/**
	 * Sample randomly n points from the input and display the interpolated result
	 * using nearest neighbors
	 *
	 * @param input - the input data
	 * @param interval - the size of the input (where to collect random samples)
	 * @param numPoints - how many points to sample
	 *
	 * @return - a RandomAccessibleInterval of the same size as the input,
	 * rendered from the sparse data
	 */
	public < T extends Type< T > > RandomAccessibleInterval< T > randomSampling(
		RealRandomAccessible< T > input, Interval interval, int numPoints )
	{
		// create an IterableRealInterval
		IterableRealInterval< T > realInterval = sampleRandomPoints( input, interval, numPoints );

		// using nearest neighbor search we will be able to return a value an any position in space
		NearestNeighborSearch< T > search = new NearestNeighborSearchOnKDTree<>(
			new KDTree<>( realInterval ) );

		// make it into RealRandomAccessible using nearest neighbor search
		RealRandomAccessible< T > realRandomAccessible = Views.interpolate( search,
			new NearestNeighborSearchInterpolatorFactory< T >() );

		// convert it into a RandomAccessible which can be displayed
		RandomAccessible< T > randomAccessible = Views.raster( realRandomAccessible );

		// set the initial interval as area to view
		return Views.interval( randomAccessible, interval );
	}

	/**
	 * Sample randomly n points from the input and display the interpolated result using
	 * distance-weighted interpolation of 20 nearest neighbors
	 *
	 * @param input - the input data
	 * @param interval - the size of the input (where to collect random samples)
	 * @param numPoints - how many points to sample
	 *
	 * @return - a RandomAccessibleInterval of the same size as the input,
	 * rendered from the sparse data
	 */
	public < T extends RealType< T > > RandomAccessibleInterval< T > randomSamplingKNearest(
		RealRandomAccessible< T > input, Interval interval, int numPoints )
	{
		// create an IterableRealInterval
		IterableRealInterval< T > realInterval = sampleRandomPoints( input, interval, numPoints );

		// using nearest neighbor search we will be able to return a value an any position in space
		KNearestNeighborSearch< T > search = new KNearestNeighborSearchOnKDTree< >(
			new KDTree<>( realInterval ), Math.min( 20, (int)realInterval.size() ) );

		// make it into RealRandomAccessible using nearest neighbor search
		RealRandomAccessible< T > realRandomAccessible = Views.interpolate( search,
			new InverseDistanceWeightingInterpolatorFactory< T >() );

		// convert it into a RandomAccessible which can be displayed
		RandomAccessible< T > randomAccessible = Views.raster( realRandomAccessible );

		// set the initial interval as area to view
		return Views.interval( randomAccessible, interval );
	}

	/**
	 * Sample a number of n-dimensional random points in a certain interval having a
	 * random intensity 0...1
	 *
	 * @param interval - the interval in which points are created
	 * @param numPoints - the amount of points
	 *
	 * @return a RealPointSampleList (which is an IterableRealInterval)
	 */
	public static < T extends Type< T > > RealPointSampleList< T > sampleRandomPoints(
		RealRandomAccessible< T > input, RealInterval interval, int numPoints )
	{
		// the number of dimensions
		int numDimensions = interval.numDimensions();

		// a random number generator
		Random rnd = new Random( 1332441549191l );

		// a list of Samples with coordinates
		RealPointSampleList< T > elements = new RealPointSampleList<>( numDimensions );

		// a random accessible in the image data to grep the right value
		RealRandomAccess< T > realRandomAccess = input.realRandomAccess();

		for ( int i = 0; i < numPoints; ++i )
		{
			RealPoint point = new RealPoint( numDimensions );

			for ( int d = 0; d < numDimensions; ++d )
				point.setPosition( rnd.nextDouble() *
					( interval.realMax( d ) - interval.realMin( d ) ) + interval.realMin( d ), d );

			realRandomAccess.setPosition( point );

			// add a new element with a random intensity in the range 0...1
			elements.add( point, realRandomAccess.get().copy() );
		}

		return elements;
	}

	public static void main( String[] args ) throws ImgIOException
	{
		// open an ImageJ window
		new ImageJ();

		// run the example
		new Example8b();
	}
}
