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
import io.scif.config.SCIFIOConfig;
import io.scif.config.SCIFIOConfig.ImgMode;
import io.scif.img.ImgIOException;
import io.scif.img.ImgOpener;

import java.io.File;

import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

/**
 * Opens a file with SCIFIO's ImgOpener as an ImgLib2 Img.
 */
public class Example1b
{
	// within this method we define <T> to be a RealType and a NativeType which means the
	// Type is able to map the data into an java basic type array
	public < T extends RealType< T > & NativeType< T > > Example1b()
		throws ImgIOException
	{
		// define the file to open
		File file = new File( "DrosophilaWing.tif" );
		String path = file.getAbsolutePath();

		// create the ImgOpener
		ImgOpener imgOpener = new ImgOpener();

		// open with ImgOpener. The type (e.g. ArrayImg, PlanarImg, CellImg) is
		// automatically determined. For a small image that fits in memory, this
		// should open as an ArrayImg.
		Img< T > image = ( Img< T > ) imgOpener.openImgs( path ).get( 0 );

		// display it via ImgLib using ImageJ
		ImageJFunctions.show( image );

		// create the SCIFIOConfig. This gives us configuration control over how
		// the ImgOpener will open its datasets.
		SCIFIOConfig config = new SCIFIOConfig();

		// If we know what type of Img we want, we can encourage their use through
		// an SCIFIOConfig instance. CellImgs dynamically load image regions and are
		// useful when an image won't fit in memory
		config.imgOpenerSetImgModes( ImgMode.CELL );

		// open with ImgOpener as a CellImg
		Img< T > imageCell = ( Img< T > ) imgOpener.openImgs( path, config ).get( 0 );

		// display it via ImgLib using ImageJ. The Img type only affects how the
		// underlying data is accessed, so these images should look identical.
		ImageJFunctions.show( imageCell );
	}

	public static void main( String[] args ) throws ImgIOException
	{
		// open an ImageJ window
		new ImageJ();

		// run the example
		new Example1b();
	}
}
