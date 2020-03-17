package interactive;
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

import net.imagej.ImgPlus;
import net.imglib2.converter.Converter;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.ui.overlay.LogoPainter;
import net.imglib2.ui.viewer.InteractiveViewer2D;
import net.imglib2.view.Views;
import net.imglib2.view.composite.CompositeView;
import net.imglib2.view.composite.NumericComposite;

public class InteractiveCompositeViewer
{
	final static public void main( final String[] args ) throws ImgIOException
	{
		final String filename = "bike2-composite.tif";
		final ImgPlus< FloatType > img = IO.openImgs( filename, new ArrayImgFactory<>( new FloatType() ) ).get( 0 );

		final CompositeView< FloatType, NumericComposite< FloatType > > compositeView =
				Views.collapseNumeric( Views.extendZero( img ), ( int )img.dimension( 2 ) );

		final Converter< NumericComposite< FloatType >, ARGBType > converter =
				new Converter< NumericComposite< FloatType >, ARGBType >()
		{
			@Override
			public void convert( final NumericComposite< FloatType > input, final ARGBType output )
			{
				int r = (int) input.get( 0 ).getRealDouble();
				r = r > 255 ? 255 : r;
				int g = (int) input.get( 1 ).getRealDouble();
				g = g > 255 ? 255 : g;
				int b = (int) input.get( 2 ).getRealDouble();
				b = b > 255 ? 255 : b;

				output.set( ( ( ( ( r << 8 ) | g ) << 8 ) | b ) | 0xff000000 );
			}
		};


		final int w = 720, h = 405;

		final AffineTransform2D initial = new AffineTransform2D();

		System.out.println( compositeView.numDimensions() );

		final InteractiveViewer2D< NumericComposite< FloatType > > viewer = new InteractiveViewer2D<>( w, h, compositeView, initial, converter );
		viewer.getDisplayCanvas().addOverlayRenderer( new LogoPainter() );
		viewer.requestRepaint();
	}

}
