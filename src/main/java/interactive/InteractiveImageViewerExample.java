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

package interactive;

import ij.IJ;

import io.scif.img.IO;
import io.scif.img.ImgIOException;

import net.imagej.ImgPlus;
import net.imagej.space.CalibratedSpace;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.RealARGBConverter;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.ui.overlay.LogoPainter;
import net.imglib2.ui.viewer.InteractiveViewer2D;
import net.imglib2.ui.viewer.InteractiveViewer3D;
import net.imglib2.view.Views;

public class InteractiveImageViewerExample
{
	private static < T extends Type< T > & Comparable< T > > void getMinMax( final IterableInterval< T > source, final T minValue, final T maxValue )
	{
		for ( final T t : source )
			if ( minValue.compareTo( t ) > 0 )
				minValue.set( t );
			else if ( maxValue.compareTo( t ) < 0 )
				maxValue.set( t );
	}

	public static < T extends RealType< T > & NativeType< T > > void show( final RandomAccessibleInterval< T > interval )
	{
		final T min = Views.iterable( interval ).firstElement().copy();
		final T max = min.copy();
		getMinMax( Views.iterable( interval ), min, max );
		show( interval, min, max );
	}

	public static < T extends RealType< T > & NativeType< T > > void show( final RandomAccessibleInterval< T > interval, final long width, final long height )
	{
		final T min = Views.iterable( interval ).firstElement().copy();
		final T max = min.copy();
		getMinMax( Views.iterable( interval ), min, max );
		show( interval, min, max, width, height );
	}

	public static < T extends RealType< T > & NativeType< T > > void show( final RandomAccessibleInterval< T > interval, final T min, final T max )
	{
		show( interval, min, max, interval.dimension( 0 ), interval.dimension( 1 ) );
	}

	public static < T extends RealType< T > & NativeType< T > > void show( final RandomAccessibleInterval< T > interval, final T min, final T max, final long width, final long height )
	{
		final RandomAccessible< T > source = Views.extendValue( interval, min );
		final RealARGBConverter< T > converter = new RealARGBConverter<>( min.getRealDouble(), max.getRealDouble() );
		final int n = interval.numDimensions();
		if ( n == 2 )
		{
			double yScale = 1;
			if ( interval instanceof CalibratedSpace )
			{
				final CalibratedSpace<?> cs = ( CalibratedSpace<?> ) interval;
				// TODO - using averageScale() introduces error for nonlinear axes
				yScale = cs.averageScale(1) / cs.averageScale(0);
				if ( Double.isNaN( yScale ) || Double.isInfinite( yScale ) )
					yScale = 1;
			}

			final AffineTransform2D initial = new AffineTransform2D();
			initial.set(
				1, 0, ( width - interval.dimension( 0 ) ) / 2.0 - interval.min( 0 ),
				0, yScale, ( height - interval.dimension( 1 ) * yScale ) / 2.0 - interval.min( 1 ) * yScale );

			final InteractiveViewer2D< T > viewer = new InteractiveViewer2D<>( ( int ) width, ( int ) height, source, initial, converter );
			viewer.getDisplayCanvas().addOverlayRenderer( new LogoPainter() );
			viewer.requestRepaint();
		}
		else if ( n == 3 )
		{
			double yScale = 1;
			double zScale = 1;
			if ( interval instanceof CalibratedSpace )
			{
				final CalibratedSpace<?> cs = ( CalibratedSpace<?> ) interval;
				yScale = cs.averageScale(1) / cs.averageScale(0);
				zScale = cs.averageScale(2) / cs.averageScale(0);
				if ( Double.isNaN( yScale ) || Double.isInfinite( yScale ) )
					yScale = 1;
				if ( Double.isNaN( zScale ) || Double.isInfinite( zScale ) )
					zScale = 1;
			}

			final AffineTransform3D initial = new AffineTransform3D();
			initial.set(
				1,      0,      0, ( width - interval.dimension( 0 ) ) / 2.0 - interval.min( 0 ),
				0, yScale,      0, ( height - interval.dimension( 1 ) * yScale ) / 2.0 - interval.min( 1 ) * yScale,
				0,      0, zScale, -interval.dimension( 2 ) * zScale / 2.0 );

			final InteractiveViewer3D< T > viewer = new InteractiveViewer3D<>( ( int ) width, ( int ) height, source, interval, initial, converter );
			viewer.getDisplayCanvas().addOverlayRenderer( new LogoPainter() );
			viewer.requestRepaint();
		}
		else
		{
			IJ.error( "Interactive viewer only supports 2D and 3D images" );
		}
	}

	public static void main( final String[] args ) throws ImgIOException
	{
//		final String filename = "DrosophilaWing.tif";
		final String filename = "/Users/pietzsch/workspace/imglib/examples/l1-cns.tif";
		final ImgPlus< FloatType > img = IO.openImgs( filename, new ArrayImgFactory<>( new FloatType() ) ).get( 0 );
		//		show( Views.interval( img, FinalInterval.createMinSize( 200, 10, 200, 200 ) ) );
		show ( img );
	}
}
