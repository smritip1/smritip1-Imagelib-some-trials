import ij.ImageJ;

import io.scif.img.IO;
import io.scif.img.ImgIOException;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.real.FloatType;

/**
 * Opens a file with ImageJ and threshold it based on a pre-defined threshold value
 * All the pixels in the input image which are less than or equal to 180 are set to 0 (black) and
 * the pixels above 180 are set to 255 (white)
 * 
 * */
public class TryEx {

	public TryEx() 
	{
		// open with ImgOpener
		Img< FloatType > img = IO.openImgs( "DrosophilaWing.tif", new ArrayImgFactory<>( new FloatType() ) ).get( 0 );
		
		ImageJFunctions.show( img );
		
		// Image Threshold function
		Img< FloatType > imgThresh = imageThreshold( img, new CellImgFactory<>( new FloatType(), 20 ) );

		// show the image
		//ImageJFunctions.show( img );
		ImageJFunctions.show( imgThresh );
		
	}
	
	public < T extends Type< T > > Img< T > imageThreshold( final Img< T > input,
		    final ImgFactory< T > imgFactory )
	{
		    // create a new Image with the same properties as input
			Img< T > output = input.factory().create( input );

			// create a cursor that automatically localizes itself on every move
		    Cursor< T > cursorInput = input.localizingCursor();
		    RandomAccess< T > randomAccess = output.randomAccess();
			
			// define threshold
			float threshold = 180;

			// iterate over the input pixels
			while ( cursorInput.hasNext())
			{
				// move both cursors forward by one pixel
				cursorInput.fwd();
				// set the output cursor to the position of the input cursor
			    randomAccess.setPosition( cursorInput );
		        
				// get the value of the next pixel in the input image
				FloatType thisPixel = (FloatType) cursorInput.next();
				
				// check if the value of pixel is above the threshold
				if ( thisPixel.get() > threshold )
				{
					//set the current pixel to 255 in the input image
					thisPixel.set( 255 );
					// set the value of this pixel of the output image, every Type supports T.set( T type )
				    randomAccess.get().set(cursorInput.get() );
				}
				else
				{
					//set the current pixel to 0 in the input image
					thisPixel.set( 0 );
					// set the value of this pixel of the output image, every Type supports T.set( T type )
				    randomAccess.get().set( cursorInput.get() );
				}
			}
					
				// return the copy
				return output;
    }
			
	public static void main(String[] args) 
	{
		// open an ImageJ window
		new ImageJ();

		// run the example
		new TryEx();

	}

}
